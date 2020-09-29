package com.adaptris.core.json.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.adaptris.core.util.JdbcUtil;

public abstract class UpsertJsonCase extends JdbcJsonInsertCase {

  protected static final String UTC_0 = "1970-01-01";
  protected static final String SMITH = "smith";
  protected static final String ID_ELEMENT_VALUE = "firstname";
  protected static final String CAROL = "carol";
  protected static final String DOB = "2017-01-03";


  protected static void populateDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      s.execute(String.format("INSERT INTO %s (firstname, lastname, dob) VALUES ('%s', '%s' ,'%s')", TABLE_NAME, CAROL, SMITH,
          UTC_0));
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void checkDob(String firstname, String dob) throws Exception {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = createConnection();
      s = c.createStatement();
      rs = s.executeQuery(String.format("SELECT * FROM %s WHERE firstname='%s'", TABLE_NAME, firstname));
      if (rs.next()) {
        assertEquals(dob, rs.getString("dob"));
      } else {
        fail("No Match for firstname: " + firstname);
      }
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }
}
