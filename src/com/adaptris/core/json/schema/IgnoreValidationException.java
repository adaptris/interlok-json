package com.adaptris.core.json.schema;

import org.everit.json.schema.ValidationException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Noop implementation of {@link ValidationExceptionHandler}
 * 
 * @config json-schema-validation-exception-ignore
 */
@XStreamAlias("json-schema-validation-exception-ignore")
public class IgnoreValidationException extends ValidationExceptionHandlerImpl {


  @Override
  public void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException {
  }

}
