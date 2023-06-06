package com.adaptris.core.json.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.jdbc.JdbcMapInsert;
import com.adaptris.core.services.jdbc.JdbcOutputExampleTest;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public abstract class JdbcJsonInsertCase extends ExampleServiceCase {


  protected static final String ARRAY_CONTENT =
      " [" + "   { \"firstname\":\"alice\", \"lastname\":\"smith\", \"dob\":\"2017-01-01\" },"
          + "   { \"firstname\":\"bob\", \"lastname\":\"smith\", \"dob\":\"2017-01-02\" },"
          + "   { \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }" + " ]";

  protected static final String OBJECT_CONTENT = "{ \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";
  protected static final String INVALID_COLUMN_ARRAY =
      "[{ \"$firstname\":\"carol\", \"$lastname\":\"smith\", \"$dob\":\"2017-01-03\" }]";

  protected static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String JDBC_URL = "jdbc:derby:memory:JSON_DB;create=true";

  protected static final String TABLE_NAME = "json_people";


  public JdbcJsonInsertCase() {
    if (PROPERTIES.getProperty(JdbcOutputExampleTest.BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(JdbcOutputExampleTest.BASE_DIR_KEY));
    }
  }



  @Test
  public void testService_Init() throws Exception {
    JdbcMapInsert service = createService();
    try {
      LifecycleHelper.init(service);
      fail();
    } catch (CoreException expected) {

    }
    service.setTable("hello");
    LifecycleHelper.init(service);
  }


  protected static void doAssert(int expectedCount) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement(String.format("SELECT * FROM %s", TABLE_NAME));
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        assertEquals("smith", rs.getString("lastname"));
      }
      assertEquals(expectedCount, count);
      JdbcUtil.closeQuietly(rs);
    } finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }


  protected static Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(JDBC_DRIVER);
    c = DriverManager.getConnection(JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  protected static void createDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      executeQuietly(s, String.format("DROP TABLE %s", TABLE_NAME));
      s.execute(String.format("CREATE TABLE %s (firstname VARCHAR(128) NOT NULL, lastname VARCHAR(128) NOT NULL, dob VARCHAR(128))",
          TABLE_NAME));
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void executeQuietly(Statement s, String sql) {
    try {
      s.execute(sql);
    } catch (Exception e) {
      ;
    }
  }

  @Override
  protected JdbcMapInsert retrieveObjectForSampleConfig() {
    return configureForExamples(createService().withTable("myTable"));
  }

  protected abstract JdbcMapInsert createService();

  protected static <T extends JdbcMapInsert> T configureForTests(T t) {
    JdbcMapInsert service = t;
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(JDBC_URL);
    connection.setDriverImp(JDBC_DRIVER);
    service.setConnection(connection);
    service.setTable(TABLE_NAME);
    return t;
  }

  protected static <T> T configureForExamples(T t) {
    JdbcMapInsert service = (JdbcMapInsert) t;
    service.setTable("myTable");
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    return t;
  }

}
