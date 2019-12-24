package com.adaptris.core.json.schema;

import java.util.Map;
import java.util.TreeMap;
import org.everit.json.schema.ValidationException;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link ValidationExceptionHandler}
 * <p>
 * This creates a ServiceException from the ValidationException, but will include all the
 * {@code ValidationException#getCausingExceptions()} messages as part of the message.
 * </p>
 * 
 * @author lchan
 * @config json-schema-validation-exception-default
 */
@XStreamAlias("json-schema-validation-exception-default")
@ComponentProfile(summary = "Throw an exception if validation fails during schema validation", tag = "json,validation")
public class DefaultValidationExceptionHandler extends ValidationExceptionHandlerImpl {


  @Override
  public void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException {
    throw ExceptionHelper.wrapServiceException(buildExceptionMessage(exc), exc);
  }

  protected String buildExceptionMessage(ValidationException exc) {
    String prefix = exc.getMessage() + ": ";
    Map<Integer, String> map = new TreeMap<>();
    int count = 0;
    for (ValidationException ve : exc.getCausingExceptions()) {
      log.error(ve.getMessage());
      map.put(++count, ve.getMessage());
    }
    return prefix + map;
  }
}
