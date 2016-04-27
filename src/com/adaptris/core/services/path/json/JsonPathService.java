package com.adaptris.core.services.path.json;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.config.DataInputParameter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This service allows you to search JSON content and the results are then set back into the message.
 * <p>
 * The searching works in much the same way as XPath, for more information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a> documentation.
 * </p>
 * <p>
 * By configuring the "source" and "target-destination" ({@link DataDestination}) you can specify where the JSON content
 * is sourced from and where the results of the search should be set.
 * </p>
 * <p>
 * You may configure multiple target destinations. This allows you to essentially perform multiple searches, rather than configuring
 * multiple instances of this service.
 * </br>
 * For example, if you have a message with the following payload;
 * <pre>
 * {@code
{
    "store": {
        "book": [{
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
        }],
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
<target-destination class="json-metadata-destination">
  <configured-json-path class="constant-json-path">
    <json-path>$.store.book[0].title</json-path>
  </configured-json-path>
  <key>metadata-key-1</key>
</target-destination>
<target-destination class="json-metadata-destination">
  <configured-json-path class="constant-json-path">
    <json-path>$.store.book[1].title</json-path>
  </configured-json-path>
  <key>metadata-key-2</key>
</target-destination>
 * }
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
 * @license BASIC
 */

@XStreamAlias("json-path-service")
@AdapterComponent
@ComponentProfile(summary = "Extract a value from a JSON document", tag = "service,transform,json,metadata")
public class JsonPathService extends ServiceImp {
  
  @NotNull
  @AutoPopulated
  private DataInputParameter<String> source;
  @Deprecated
  private DataInputParameter<String> sourceDestination;
  
  @XStreamImplicit(itemFieldName="json-path-execution")
  @NotNull
  @Valid
  @AutoPopulated
  private List<Execution> executions;
  
  private Boolean unwrapJson;
  
  public JsonPathService() {
    setSource(new StringPayloadDataInputParameter());
    setExecutions(new ArrayList<Execution>());
  }
  
  static {
    Configuration.setDefaults(new Configuration.Defaults() {
        private final JsonProvider jsonProvider = new JsonSmartJsonProvider();
        private final MappingProvider mappingProvider = new JacksonMappingProvider();
        private final Set<Option> options = EnumSet.noneOf(Option.class);

        public JsonProvider jsonProvider() {
            return jsonProvider;
        }

        @Override
        public MappingProvider mappingProvider() {
            return mappingProvider;
        }

        @Override
        public Set<Option> options() {
            return options;
        }
    });
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      Object parsedJsonContent = Configuration.defaultConfiguration().jsonProvider().parse(sourceToUse().extract(message));
      for (Execution execution : this.getExecutions()) {
        execution.getTarget().insert(this.unwrap(JsonPath.read(parsedJsonContent, execution.getSource().extract(message)).toString()), message);
      }
    } catch (InterlokException ex) {
      throw new ServiceException(ex);
    }
  }
  
  /*
   * Do we need to strip the square brackets off of a value?
   */
  private String unwrap(String jsonValue) {
    if (unwrapJson()) {
      if((jsonValue.startsWith("[")) && (jsonValue.endsWith("]")))
        return jsonValue.substring(1, jsonValue.length() - 1);
    }
    return jsonValue;
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {

  }

  /**
   * 
   * @deprecated since 3.2.0 use {@link #getSource()} instead.
   */
  @Deprecated
  public DataInputParameter<String> getSourceDestination() {
    return sourceDestination;
  }

  /**
   * 
   * @deprecated since 3.2.0 use {@link #getSource()} instead.
   */
  @Deprecated
  public void setSourceDestination(DataInputParameter<String> sourceDestination) {
    log.warn("source-destination deprecated; use source instead");
    this.sourceDestination = Args.notNull(sourceDestination, "sourceDestination");
  }


  public DataInputParameter<String> getSource() {
    return source;
  }

  public void setSource(DataInputParameter<String> s) {
    this.source = Args.notNull(s, "source");
  }

  private DataInputParameter<String> sourceToUse() {
    DataInputParameter<String> result = getSource();
    if (getSourceDestination() != null) {
      result = getSourceDestination();
    }
    return result;
  }

  public List<Execution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<Execution> executions) {
    this.executions = executions;
  }
  
  boolean unwrapJson() {
    return (this.getUnwrapJson() == null ? false : this.getUnwrapJson().booleanValue());
  }

  public Boolean getUnwrapJson() {
    return unwrapJson;
  }

  public void setUnwrapJson(Boolean unwrapJson) {
    this.unwrapJson = unwrapJson;
  }

}
