package com.adaptris.core.services.path.json;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.json.JsonPathExecution;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
@DisplayOrder(order = {"source", "executions", "unwrapJson", "suppressPathNotFound"})
public class JsonPathService extends ServiceImp {

  private static boolean warningLogged = false;
  @NotNull
  @AutoPopulated
  private DataInputParameter<String> source = new StringPayloadDataInputParameter();

  @Deprecated
  private DataInputParameter<String> sourceDestination;

  //@XStreamImplicit(itemFieldName = "json-path-execution")
  @NotNull
  @Valid
  @AutoPopulated
  private List<JsonPathExecution> executions = new ArrayList<>();

  protected transient Configuration jsonConfig;

  @InputFieldDefault(value = "false")
  private Boolean unwrapJson;


  public JsonPathService() {
    super();
  }

  public JsonPathService(DataInputParameter<String> source, List<JsonPathExecution> executions) {
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

      final DataInputParameter<String> src = sourceDestination != null ? sourceDestination : source;
      final String rawJson = src.extract(message);
      ReadContext context = JsonPath.parse(rawJson, jsonConfig);

      for (final JsonPathExecution execution : executions) {
        execute(execution, context, message);
      }
    } catch (InterlokException e) {
      throw ExceptionHelper.wrapServiceException(e);
    } catch (RuntimeException e) {
      // So, most of json path throws RTE so we turn it into a checked exception.
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void execute(JsonPathExecution execution, ReadContext context, AdaptrisMessage msg) throws InterlokException {
    try {
      final DataInputParameter<String> source = execution.getSource();
      final DataOutputParameter<String> target = execution.getTarget();

      final String jsonPath = source.extract(msg);
      final String jsonString = unwrap(context.read(jsonPath).toString());
      target.insert(jsonString, msg);
    } catch (PathNotFoundException e) {
      if (!execution.suppressPathNotFound()) {
        throw ExceptionHelper.wrapServiceException(e);
      }
    }
  }


  /**
   * Strip (if necessary) the leading/trailing [] from the JSON.
   *
   * @param json
   *        The JSON string.
   */
  private String unwrap(final String json) {
    /* Do we need to strip the square brackets off of a value? */
    if (unwrapJson()) {
      if (json.startsWith("[") && json.endsWith("]")) {
        return json.substring(1, json.length() - 1);
      }
    }
    return json;
  }

  @Override
  public void prepare() throws CoreException {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }

  @Override
  protected void closeService() {
    /* unused/empty method */
  }

  @Override
  protected void initService() throws CoreException {
    if (!warningLogged && getSourceDestination() != null) {
      log.warn("source-destination deprecated; use source instead");
      warningLogged = true;
    }
  }

  /**
   * @return The source destination.
   *
   * @deprecated since 3.2.0 use {@link #getSource()} instead.
   */
  @Deprecated
  public DataInputParameter<String> getSourceDestination() {
    return sourceDestination;
  }

  /**
   * @param sourceDestination
   *        The source destination.
   *
   * @deprecated since 3.2.0 use {@link #setSource(DataInputParameter)} instead.
   */
  @Deprecated
  public void setSourceDestination(final DataInputParameter<String> sourceDestination) {
    if (!warningLogged) {
      log.warn("source-destination deprecated; use source instead");
      warningLogged = true;
    }
    this.sourceDestination = Args.notNull(sourceDestination, "sourceDestination");
  }

  /**
   * Get the source.
   *
   * @return The source.
   */
  public DataInputParameter<String> getSource() {
    return source;
  }

  /**
   * Set the source.
   *
   * @param source
   *        The source.
   */
  public void setSource(final DataInputParameter<String> source) {
    this.source = Args.notNull(source, "source");
  }

  /**
   * Get the list of execution.
   *
   * @return The list of executions.
   */
  public List<JsonPathExecution> getExecutions() {
    return executions;
  }

  /**
   * Set the list of executions.
   *
   * @param executions
   *        The list of executions.
   */
  public void setExecutions(final List<JsonPathExecution> executions) {
    this.executions = executions;
  }

  /**
   * Get whether the JSON should be unwrapped.
   *
   * @return Whether the JSON should be unwrapped.
   */
  public Boolean getUnwrapJson() {
    return unwrapJson;
  }

  /**
   * Set whether the JSON should be unwrapped.
   *
   * @param unwrapJson
   *        Whether the JSON should be unwrapped; default is false.
   */
  public void setUnwrapJson(final Boolean unwrapJson) {
    this.unwrapJson = unwrapJson;
  }

  boolean unwrapJson() {
    return getUnwrapJson() != null ? getUnwrapJson().booleanValue() : false;
  }
}
