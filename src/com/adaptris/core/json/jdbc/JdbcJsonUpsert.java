package com.adaptris.core.json.jdbc;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.jdbc.JdbcMapUpsert;

/**
 * 
 * Base behaviour for upsert JSON directly into a db.
 *
 */
public abstract class JdbcJsonUpsert extends JdbcMapUpsert {

  @InputFieldDefault(value = "id")
  @Deprecated
  @AdvancedConfig
  private String jsonIdField;

  public JdbcJsonUpsert() {
    super();
  }


  @Override
  protected void initJdbcService() throws CoreException {
    super.initJdbcService();
    if (getJsonIdField() != null) {
      log.warn("json-id-field is deprecated; use id-field instead");
    }
  }

  /**
   * @return the idPath
   * @deprecated since 3.6.5 use {@link #getIdField()} instead
   */
  @Deprecated
  public String getJsonIdField() {
    return jsonIdField;
  }


  /**
   * @param elem the JSON field that is the ID, defaults to {@code id} if not specified.
   * @deprecated since 3.6.5 use {@link #setIdField(String)} instead
   */
  @Deprecated
  public void setJsonIdField(String elem) {
    this.jsonIdField = elem;
  }

  @Override
  protected String idField() {
    if (getJsonIdField() != null) {
      return getJsonIdField();
    }
    return super.idField();
  }

}
