package com.adaptris.core.services.routing.json;

import java.util.List;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.routing.SyntaxIdentifier;
import com.adaptris.core.services.routing.SyntaxIdentifierImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Implementation of {@link SyntaxIdentifier} which handles JSON paths.
 * 
 * @config routing-json-path-syntax-identifier
 */
@XStreamAlias("routing-json-path-syntax-identifier")
@DisplayOrder(order = {"destination", "patterns"})
public class JsonPathSyntaxIdentifier extends SyntaxIdentifierImpl {

  public JsonPathSyntaxIdentifier() {
    super();
  }

  public JsonPathSyntaxIdentifier(List<String> jsonPaths, String dest) {
    this();
    setDestination(dest);
    setPatterns(jsonPaths);
  }

  @Override
  public boolean isThisSyntax(String message) throws ServiceException {
    try {
      final Object json = createJsonObject(message);
      if (json == null){
        return false;
      }
      for (String jsonPath : getPatterns()) {
        String result;
        try {
          result = JsonPath.read(json.toString(), jsonPath).toString();
        } catch (PathNotFoundException ex){
          return false;
        }
        if (result != null){
          return true;
        }
      }
    } catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
    return false;
  }

  private Object createJsonObject(String message){
    try {
      final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
      return jsonParser.parse(message);
    } catch (ParseException e) {
      return null;
    }
  }

}
