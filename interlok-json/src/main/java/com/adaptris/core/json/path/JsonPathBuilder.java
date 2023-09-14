package com.adaptris.core.json.path;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.security.PathBuilder;
import com.adaptris.core.util.Args;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import lombok.Getter;
import lombok.Setter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * 
 * @author jwickham
 * 
 *         Imp that expects a JSON path to be provided.
 *
 */

@XStreamAlias("json-path-builder")
@AdapterComponent
@ComponentProfile(summary = "json builder to extract and insert", tag = "service,security,path", since = "4.8.9")
@DisplayOrder(order = { "json-paths" })
public class JsonPathBuilder implements PathBuilder {

  private static final String JSON_PATH_NOT_FOUND_EXCEPTION_MESSAGE = "No results found for JSON path [%s]";
  private static final String JSON_INVALID_PATH_EXCEPTION_MESSAGE = "Invalid Json path [%s]";
  private static final String JSON_NON_OBJECT_PATH_EXCEPTION_MESSAGE = "Please ensure your path [%s] points to a single JSON object";
  

  public JsonPathBuilder() {
    this.setPaths(new ArrayList<String>());
  }

  @Getter
  @Setter
  @NotNull
  @XStreamImplicit(itemFieldName = "json-paths")
  @InputFieldHint(expression = true)
  private List<String> paths;

  private Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
      .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getPaths(), "json-paths");
  }
  
  @Override
  public Map<String, String> extract(AdaptrisMessage msg) throws ServiceException {
    String jsonString = msg.getContent();
    ReadContext context = JsonPath.parse(jsonString, jsonConfig);
    Map<String, String> pathKeyValuePairs = new LinkedHashMap<>();
    for (String jsonPath : this.getPaths()) {
      String jsonPathToExecute = msg == null ? jsonPath : msg.resolve(jsonPath);
      try {
        Object result = context.read(jsonPathToExecute);
        if (Map.class.isAssignableFrom(result.getClass()) || List.class.isAssignableFrom(result.getClass())) {
          throw new ServiceException(String.format(JSON_NON_OBJECT_PATH_EXCEPTION_MESSAGE, jsonPathToExecute));
        }
        pathKeyValuePairs.put(jsonPathToExecute, result.toString());
      } catch (PathNotFoundException e) {
        throw new ServiceException(String.format(JSON_PATH_NOT_FOUND_EXCEPTION_MESSAGE, jsonPathToExecute));
      } catch (InvalidPathException e) {
        throw new ServiceException(String.format(JSON_INVALID_PATH_EXCEPTION_MESSAGE, jsonPathToExecute));
      }
    }
    
    return pathKeyValuePairs;
  }

  @Override
  public void insert(AdaptrisMessage msg, Map<String, String> pathKeyValuePairs) throws ServiceException {
    String jsonString = msg.getContent();
    DocumentContext jsonDoc = JsonPath.using(jsonConfig).parse(jsonString);
    for (Map.Entry<String, String> entry : pathKeyValuePairs.entrySet()) {
      String jsonKey = entry.getKey();
      DocumentContext jsonValue = JsonPath.using(jsonConfig).parse(entry.getValue());
      jsonDoc.set(jsonKey, jsonValue.json());
    }
    msg.setContent(jsonDoc.jsonString(), msg.getContentEncoding());
  }
}