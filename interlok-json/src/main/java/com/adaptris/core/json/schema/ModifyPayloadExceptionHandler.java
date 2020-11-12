package com.adaptris.core.json.schema;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.everit.json.schema.ValidationException;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ValidationExceptionHandler} that stores the violations as part of the payload.
 * <p>
 * This implementation modifies the existing payload so that it is rewritten to be something like
 * </p>
 * <pre>
 * {@code
    { "original" : { // the original message },
      "schema-violations" : [ // list of validation failures ]
    }
   }
 * </pre>
 *
 * @config json-schema-validation-exception-into-message
 */
@XStreamAlias("json-schema-validation-exception-into-message")
@ComponentProfile(summary = "Store schema violations as part of the JSON payload", tag = "json,validation")
public class ModifyPayloadExceptionHandler extends ValidationExceptionHandlerImpl {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean throwException;

  private transient ObjectMapper mapper = new ObjectMapper();

  public ModifyPayloadExceptionHandler() {
  }

  public ModifyPayloadExceptionHandler(Boolean throwException) {
    this();
    setThrowException(throwException);
  }

  @Override
  public void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException {
    try {
      Object json = jsonify(msg.getContent());
      Set<String> violations = new LinkedHashSet<>();
      violations.add(exc.getMessage());
      for (String ve : exc.getAllMessages()) {
        violations.add(ve);
      }
      Map<String, Object> newMsg = new HashMap<>();
      newMsg.put("original", json);
      newMsg.put("schema-violations", violations);
      try (Writer w = msg.getWriter()) {
        mapper.writeValue(w, newMsg);
      }
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    if (throwException()) {
      throw ExceptionHelper.wrapServiceException(exc);
    }
  }

  public Boolean getThrowException() {
    return throwException;
  }

  /**
   * After adding the ValidationException to the payload throw an exception.
   *
   * @param b true to throw an exception; false otherwise (default false).
   */
  public void setThrowException(Boolean b) {
    this.throwException = b;
  }

  boolean throwException() {
    return getThrowException() != null ? getThrowException().booleanValue() : false;
  }

  private Object jsonify(String input) throws JsonParseException, JsonMappingException, IOException {
    return mapper.readValue(input, Object.class);
  }
}
