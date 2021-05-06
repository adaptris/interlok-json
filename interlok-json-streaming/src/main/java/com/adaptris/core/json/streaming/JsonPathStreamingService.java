package com.adaptris.core.json.streaming;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.Execution;
import com.adaptris.core.services.path.json.JsonPathService;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.jsfr.json.Collector;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferFastJson;
import org.jsfr.json.JsonSurferGson;
import org.jsfr.json.JsonSurferJackson;
import org.jsfr.json.JsonSurferJsonSimple;
import org.jsfr.json.ValueBox;

import javax.json.JsonException;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("json-path-streaming-service")
@AdapterComponent
@ComponentProfile(summary = "Extract a value from a large JSON document", tag = "service,transform,json,metadata,streaming,large")
@DisplayOrder(order = {"surfer", "source", "executions", "unwrapJson"})
@NoArgsConstructor
public class JsonPathStreamingService extends JsonPathService {

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

      final DataInputParameter<InputStream> src = source;
      final InputStream rawJson = src.extract(message);

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
    return (Surfer)ObjectUtils.defaultIfNull(surfer, Surfer.SIMPLE);
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
    },
    FAST {
      @Override
      JsonSurfer getInstance() {
        return JsonSurferFastJson.INSTANCE;
      }
    };
    abstract JsonSurfer getInstance();
  }
}
