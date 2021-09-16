package com.adaptris.core.json.streaming;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonException;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.jsfr.json.Collector;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferGson;
import org.jsfr.json.JsonSurferJackson;
import org.jsfr.json.JsonSurferJsonSimple;
import org.jsfr.json.ValueBox;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.services.path.json.JsonPathServiceImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * This service allows you to search JSON content and the results are
 * then set back into the message. The advantage of this implementation
 * is that it doesn't need to parse the entire JSON document and so is
 * able to handle arbitrarily large documents.
 * <p>
 * The searching works in much the same way as XPath, for more
 * information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a>
 * documentation and the
 * <a href="https://github.com/jsurfer/JsonSurfer">JSON Surfer</a>
 * documentation.
 * </p>
 * For example, if you have a message with the following payload:
 *
 * <pre>
 * {@code
{
"store": {
"book": [ {
"category": "reference",
"author": "Nigel Rees",
"title": "Sayings of the Century",
"price": 8.95
}, {
"category": "fiction",
"author": "Evelyn Waugh",
"title": "Sword of Honour",
"price": 12.99
}, {
"category": "fiction",
"author": "Herman Melville",
"title": "Moby Dick",
"isbn": "0-553-21311-3",
"price": 8.99
}, {
"category": "fiction",
"author": "J. R. R. Tolkien",
"title": "The Lord of the Rings",
"isbn": "0-395-19395-8",
"price": 22.99
} ],
"bicycle": {
"color": "red",
"price": 19.95
}
},
"expensive": 10
}
 * }
 * </pre>
 *
 * You could configure 2 target destinations, each one creating a new metadata item with the results of the specified search, like
 * this;
 *
 * <pre>
 * {@code
<json-path-streaming-service>
<json-path-execution>
<source class="constant-data-input-parameter">
<value>$.store.book[0].title</value>
</source>
<target class="metadata-data-output-parameter">
<metadata-key>metadata-key-1</metadata-key>
</target>
<suppress-path-not-found>true</suppress-path-not-found>
</json-path-execution>
<json-path-execution>
<source class="constant-data-input-parameter">
<value>$.store.book[1].title</value>
</source>
<target class="metadata-data-output-parameter">
<metadata-key>metadata-key-2</metadata-key>
</target>
</json-path-execution>
</json-path-streaming-service>
}
 * </pre>
 *
 * The first target above searches for the first book title, the second target searches for the second book title.
 * Each target-destination will be executed in the order they are configured and therefore with the two targets shown here, your
 * message, after the
 * service has run, will include two new metadata items;
 *
 * <ul>
 * <li>metadata-key-1 = "Sayings of the Century"</li>
 * <li>metadata-key-2 = "Sword of Honour"</li>
 * </ul>
 * </p>
 * <p>
 * Any results returned by this service will normally include the json brackets wrapping the returned value. However you can
 * configure this
 * service to unwrap the result for you, such that a value returned as "[myValue]" will now be returned as "myValue".
 * <br/>
 * The default value is false, but to override simply configure the "unwrap";
 *
 * <pre>
 * {@code
<json-path-service>
<unwrap-json>true</unwrap-json>
...
</json-path-service>
 * }
 * </pre>
 * </p>
 *
 * @author aanderson
 * @config json-path-streaming-service
 */
@XStreamAlias("json-path-streaming-service")
@AdapterComponent
@ComponentProfile(summary = "Extract a value from a large JSON document", tag = "service,transform,json,metadata,streaming,large")
@DisplayOrder(order = {"surfer", "source", "executions", "unwrapJson"})
@NoArgsConstructor
public class JsonPathStreamingService extends JsonPathServiceImpl
{
  /**
   * The source for executing the jsonpath against.
   */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private DataInputParameter<InputStream> source = new PayloadStreamInputParameter();

  /**
   * The JSON surfer implementation to use.
   */
  @Getter
  @Setter
  @NonNull
  @NotNull
  private Surfer surfer;

  private transient JsonSurfer jsonSurfer;

  public JsonPathStreamingService(DataInputParameter<InputStream> source, Execution... executions) {
    this(source, new ArrayList<>(Arrays.asList(executions)));
  }

  public JsonPathStreamingService(DataInputParameter<InputStream> source, List<Execution> executions) {
    super();
    setSource(source);
    setExecutions(executions);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {

      final InputStream rawJson = source.extract(message);

      final Collector collector = jsonSurfer.collector(rawJson);
      final Map<Execution, ValueBox<Collection<Object>>> valueBoxes = new LinkedHashMap<>();

      for (final Execution execution : executions) {
        final DataInputParameter<String> source = execution.getSource();
        final String jsonPath = source.extract(message);

        ValueBox<Collection<Object>> v = collector.collectAll(jsonPath);
        valueBoxes.put(execution, v);
      }

      collector.exec();

      for (final Execution execution : valueBoxes.keySet()) {
        final DataOutputParameter<String> target = execution.getTarget();
        final ValueBox<Collection<Object>> valueBox = valueBoxes.get(execution);

        Collection<Object> objects = valueBox.get();
        if (objects.size() == 0) {
          if (!suppressPathNotFound(execution)) {
            String jsonPath = execution.getSource().extract(message);
            throw new JsonException("Path [" + jsonPath + "] not found");
          }
          continue;
        }
        Object json = objects.size() == 1 ? objects.toArray()[0] : objects;
        String jsonString = toString(json, execution);
        String unwrapped = unwrap(jsonString, unwrapJson());
        target.insert(unwrapped, message);
      }

    } catch (Throwable e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() {
    jsonSurfer = surfer().getInstance();
  }

  private Surfer surfer() {
    return ObjectUtils.defaultIfNull(surfer, Surfer.SIMPLE);
  }

  public enum Surfer {
    GSON {
      @Override
      JsonSurfer getInstance() {
        return JsonSurferGson.INSTANCE;
      }
    },
    JACKSON {
      @Override
      JsonSurfer getInstance() {
        return JsonSurferJackson.INSTANCE;
      }
    },
    SIMPLE {
      @Override
      JsonSurfer getInstance() {
        return JsonSurferJsonSimple.INSTANCE;
      }
      // fastjson has a jersey service provider.
      // },
      // FAST {
      // @Override
      // JsonSurfer getInstance() {
      // return JsonSurferFastJson.INSTANCE;
      // }
    };
    abstract JsonSurfer getInstance();
  }
}
