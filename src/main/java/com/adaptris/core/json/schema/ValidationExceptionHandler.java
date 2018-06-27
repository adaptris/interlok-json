package com.adaptris.core.json.schema;

import org.everit.json.schema.ValidationException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

public interface ValidationExceptionHandler {

  void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException;
}
