package com.adaptris.core.json.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ValidationExceptionHandlerImpl implements ValidationExceptionHandler {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ValidationExceptionHandlerImpl() {

  }

}
