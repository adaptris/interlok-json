package com.adaptris.core.json.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

public abstract class JdbcJsonInsert extends JdbcService {
  @NotBlank
  private String table;

  public JdbcJsonInsert() {
    super();
  }

  /**
   * @return the table
   */
  public String getTable() {
    return table;
  }

  /**
   * @param s the table to insert on.
   */
  public void setTable(String s) {
    this.table = s;
  }


  @Override
  protected void closeJdbcService() {}

  @Override
  protected void initJdbcService() throws CoreException {
    try {
      Args.notBlank(getTable(), "table-name");
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void prepareService() throws CoreException {}

  @Override
  protected void startService() throws CoreException {}

  @Override
  protected void stopService() {}



  protected class StatementWrapper {
    protected List<String> columns;
    protected String statement;

    StatementWrapper(Map<String, String> json) {
      columns = new ArrayList<>(json.keySet());
      statement = String.format("INSERT into %s (%s) VALUES (%s)", getTable(), createString(true), createString(false));
    }

    private String createString(boolean columnsNotQuestionMarks) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> i = columns.iterator(); i.hasNext();) {
        String s = i.next();
        sb.append(columnsNotQuestionMarks ? s : "?");
        if (i.hasNext()) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    protected void addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        statement.setObject(paramIndex, obj.get(key));
      }
      return;
    }
  }
}
