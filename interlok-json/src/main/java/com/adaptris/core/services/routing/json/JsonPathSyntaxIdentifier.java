package com.adaptris.core.services.routing.json;

import java.util.EnumSet;
import java.util.List;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.routing.SyntaxIdentifier;
import com.adaptris.core.services.routing.SyntaxIdentifierImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link SyntaxIdentifier} which handles JSON paths.
 *
 * @config routing-json-path-syntax-identifier
 */
@XStreamAlias("routing-json-path-syntax-identifier")
@DisplayOrder(order = {"destination", "patterns"})
@ComponentProfile(summary = "Syntax Identifier which handles JSON Paths", tag = "json,routing")
public class JsonPathSyntaxIdentifier extends SyntaxIdentifierImpl {

  private transient Configuration jsonConfig;

  public JsonPathSyntaxIdentifier() {
    super();
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }

  public JsonPathSyntaxIdentifier(List<String> jsonPaths, String dest) {
    this();
    setDestination(dest);
    setPatterns(jsonPaths);
  }

  @Override
  public boolean isThisSyntax(String message) throws ServiceException {
    try {
      ReadContext context = JsonPath.parse(message, jsonConfig);
      for (String jsonPath : getPatterns()) {
        try {
          Object o = context.read(jsonPath);
          // If you're using a function to try and select, you get a list, but it might be 0
          if (o instanceof List && ((List<?>) o).size() == 0) {
            return false;
          }
        } catch (PathNotFoundException ex){
          return false;
        }
      }
    } catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
    return true;
  }
}
