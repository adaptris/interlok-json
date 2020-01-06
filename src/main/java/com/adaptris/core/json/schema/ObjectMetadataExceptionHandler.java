package com.adaptris.core.json.schema;

import org.everit.json.schema.ValidationException;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ValidationExceptionHandler} that stores the actual exception as object metadata.
 * 
 * @config json-schema-validation-exception-as-object-metadata
 */
@XStreamAlias("json-schema-validation-exception-as-object-metadata")
@ComponentProfile(summary = "Store schema violations as object metadata", tag = "json,validation")
public class ObjectMetadataExceptionHandler extends DefaultValidationExceptionHandler {

  @AutoPopulated
  @InputFieldDefault(value = "ObjectMetadataExceptionHandler")
  private String objectMetadataKey;

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean throwException;

  public ObjectMetadataExceptionHandler() {
    setObjectMetadataKey(this.getClass().getSimpleName());
  }

  public ObjectMetadataExceptionHandler(Boolean b, String key) {
    this();
    setThrowException(b);
    setObjectMetadataKey(key);
  }

  @Override
  public void handle(ValidationException exc, AdaptrisMessage msg) throws ServiceException {
    msg.addObjectHeader(getObjectMetadataKey(), exc);
    if (throwException()) {
      throw ExceptionHelper.wrapServiceException(buildExceptionMessage(exc), exc);
    }
  }

  public String getObjectMetadataKey() {
    return objectMetadataKey;
  }

  public void setObjectMetadataKey(String objectMetadataKey) {
    this.objectMetadataKey = objectMetadataKey;
  }

  public Boolean getThrowException() {
    return throwException;
  }

  /**
   * After adding the ValidationException as object metadata throw an exception.
   * 
   * @param b true to throw an exception; false otherwise (default true).
   */
  public void setThrowException(Boolean b) {
    this.throwException = b;
  }

  boolean throwException() {
    return getThrowException() != null ? getThrowException().booleanValue() : true;
  }
}
