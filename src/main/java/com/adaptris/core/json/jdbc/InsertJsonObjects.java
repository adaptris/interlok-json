package com.adaptris.core.json.jdbc;

import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;

public abstract class InsertJsonObjects extends InsertJsonObject {

  @AdvancedConfig
  @InputFieldDefault(value = "JSON_ARRAY")
  private JsonStyle jsonStyle;


  public InsertJsonObjects() {
    super();
  }

  /**
   * Specify how the payload is parsed to provide JSON objects.
   * 
   * @param p the provider; default is JSON_ARRAY.
   */
  public void setJsonStyle(JsonStyle p) {
    jsonStyle = p;
  }

  public JsonStyle getJsonStyle() {
    return jsonStyle;
  }

  protected JsonStyle jsonStyle() {
    return ObjectUtils.defaultIfNull(getJsonStyle(), JsonStyle.JSON_ARRAY);
  }

}
