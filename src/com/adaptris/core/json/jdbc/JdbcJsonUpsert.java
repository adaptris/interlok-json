package com.adaptris.core.json.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.adaptris.annotation.InputFieldDefault;

public abstract class JdbcJsonUpsert extends JdbcJsonInsert {

  static String DEFAULT_ID_FIELD = "id";

  @InputFieldDefault(value = "id")
  private String jsonIdField;

  public JdbcJsonUpsert() {
    super();
  }


  /**
   * @return the idPath
   */
  public String getJsonIdField() {
    return jsonIdField;
  }


  /**
   * @param elem the JSON field that is the ID, defaults to {@code id} if not specified.
   */
  public void setJsonIdField(String elem) {
    this.jsonIdField = elem;
  }

  public JdbcJsonUpsert withJsonId(String elem) {
    setJsonIdField(elem);
    return this;
  }

  protected String jsonID() {
    return getJsonIdField() != null ? getJsonIdField() : DEFAULT_ID_FIELD;
  }

  protected class UpdateWrapper {
    protected List<String> columns;
    protected String statement;

    UpdateWrapper(Map<String, String> json) {
      columns = new ArrayList<>(json.keySet());
      columns.remove(jsonID());
      StringBuilder statementBuilder = new StringBuilder(String.format("UPDATE %s SET", getTable()));
      // Add all the updates.
      for (Iterator<String> i = columns.iterator(); i.hasNext();) {
        String col = i.next();
        statementBuilder.append(String.format(" %s=? ", col));
        if (i.hasNext()) {
          statementBuilder.append(",");
        }
      }
      // Add the where clause
      statementBuilder.append(String.format(" WHERE %s=?", jsonID()));
      statement = statementBuilder.toString();
    }

    protected PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      // Set all the updates
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        statement.setObject(paramIndex, obj.get(key));
      }
      // Set the WHERE.
      statement.setObject(columns.size() + 1, obj.get(jsonID()));
      return statement;
    }
  }

  protected class SelectWrapper {
    protected String statement;

    SelectWrapper(Map<String, String> json) {
      statement = String.format("SELECT %s FROM %s WHERE %s = ?", jsonID(), getTable(), jsonID());
    }

    protected PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      statement.setObject(1, obj.get(jsonID()));
      return statement;
    }
  }

}
