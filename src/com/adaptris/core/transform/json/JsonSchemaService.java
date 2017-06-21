package com.adaptris.core.transform.json;

import com.adaptris.interlok.config.DataInputParameter;

/**
 * 
 *
 * @deprecated use com.adaptris.core.json.schema.JsonSchemaService instead
 */
// No XStreamAlias here, just in case people use it via the class name...
@Deprecated
public class JsonSchemaService extends com.adaptris.core.json.schema.JsonSchemaService {
  private static transient boolean warningLogged;

  public JsonSchemaService() {
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.json.schema.JsonSchemaService.class.getName());
      warningLogged = true;
    }
  }

  public JsonSchemaService(DataInputParameter<String> url) {
    this();
    setSchemaUrl(url);
  }
}
