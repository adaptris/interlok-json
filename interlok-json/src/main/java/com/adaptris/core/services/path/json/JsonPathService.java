package com.adaptris.core.services.path.json;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * This service allows you to search JSON content and the results are then set back into the message.
 * <p>
 * The searching works in much the same way as XPath, for more information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a> documentation.
 * </p>
 * </br>
 * For example, if you have a message with the following payload;
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
 <json-path-service>
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
 </json-path-service>
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
 * @author amcgrath
 * @config json-path-service
 */
@XStreamAlias("json-path-service")
@AdapterComponent
@ComponentProfile(summary = "Extract a value from a JSON document", tag = "service,transform,json,metadata")
@DisplayOrder(order = {"source", "executions", "unwrapJson"})
@NoArgsConstructor
public class JsonPathService extends JsonPathServiceImpl {

  /**
   * The source for executing the jsonpath against.
   *
   */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private DataInputParameter<String> source = new StringPayloadDataInputParameter();

  private transient Configuration jsonConfig;

  public JsonPathService(DataInputParameter<String> source, Execution... executions) {
    this(source, new ArrayList<>(Arrays.asList(executions)));
  }

  public JsonPathService(DataInputParameter<String> source, List<Execution> executions) {
    this();
    setSource(source);
    setExecutions(executions);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {

      final String rawJson = source.extract(message);
      ReadContext context = JsonPath.parse(rawJson, jsonConfig);

      for (final Execution execution : executions) {
        execute(execution, context, message);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void execute(Execution execution, ReadContext context, AdaptrisMessage msg) throws Exception {
    try {
      final DataInputParameter<String> source = execution.getSource();
      final DataOutputParameter<String> target = execution.getTarget();

      final String jsonPath = source.extract(msg);
      Object node = context.read(jsonPath);
      final String jsonString = unwrap(toString(node, execution), unwrapJson());
      target.insert(jsonString, msg);
    } catch (PathNotFoundException e) {
      if (!suppressPathNotFound(execution)) {
        throw ExceptionHelper.wrapServiceException(e);
      }
    }
  }

  @Override
  public void prepare() throws CoreException {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }
}
