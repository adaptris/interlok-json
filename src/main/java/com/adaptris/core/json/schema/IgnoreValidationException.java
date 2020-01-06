package com.adaptris.core.json.schema;

import org.everit.json.schema.ValidationException;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Noop implementation of {@link ValidationExceptionHandler}
 * 
 * @config json-schema-validation-exception-ignore
 */
@XStreamAlias("json-schema-validation-exception-ignore")
@ComponentProfile(summary = "Do nothing if validation fails during schema validation", tag = "json,validation")
public class IgnoreValidationException extends ValidationExceptionHandlerImpl {


  @Override
  public void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException {
  }

}
