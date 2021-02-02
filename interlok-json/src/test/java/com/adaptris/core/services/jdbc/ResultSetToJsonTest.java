package com.adaptris.core.services.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.json.jdbc.JdbcJsonLinesOutput;
import com.adaptris.core.json.jdbc.JdbcJsonOutput;
import com.adaptris.core.services.jdbc.StyledResultTranslatorImp.ColumnStyle;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.jdbc.JdbcResult;
import com.jayway.jsonpath.ReadContext;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.createContext;
import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.createJdbcResult;
import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.createJdbcResultSingle;
import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class ResultSetToJsonTest {

  protected static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String JDBC_URL = "jdbc:derby:memory:ResultSetToJsonTest;create=true";

  protected static final String TABLE_NAME = "json_people";

  protected static final String SELECT_WITH_ALIAS = "SELECT firstname AS first_name, lastname as last_name from JSON_PEOPLE";
  protected static final String SELECT = "SELECT firstname, lastname from JSON_PEOPLE";


  @Test
  public void testTranslate() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(jsonTranslator, createJdbcResult(), message);

    ReadContext ctx = createContext(message);
    assertNotNull(ctx.read("$.[0]"));
    assertNotNull(ctx.read("$.[1]"));
    assertEquals("Anna", ctx.read("$.[1].firstName"));
  }

  @Test
  public void testTranslateLines() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonLinesOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(jsonTranslator, createJdbcResult(), message);

    assertEquals(3, message.getContent().split("\n").length);

    ReadContext ctx = createContext(message);
    assertNotNull(ctx.read("$.[0]"));
    assertNotNull(ctx.read("$.[1]"));
    assertEquals("Anna", ctx.read("$.[1].firstName"));
  }

  @Test
  public void testTranslateLinesSingle() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonLinesOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(jsonTranslator, createJdbcResultSingle(), message);

    assertEquals(1, message.getContent().split("\n").length);
    assertFalse(message.getContent().contains("["));

    ReadContext ctx = createContext(message);
    assertEquals("John", ctx.read("$.firstName"));
  }

  @Test
  public void testTranslate_NoResults() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    execute(jsonTranslator, new JdbcResult(), message);
    ReadContext ctx = createContext(message);
    try {
      ctx.read("$.[0]");
      fail();
    }
    catch (RuntimeException expected) {
    }
  }

  @Test
  public void testTranslate_IOException() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonOutput();
    AdaptrisMessage message = new BrokenAdaptrisMessage();
    try {
      execute(jsonTranslator, createJdbcResult(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testOutputWithAlias() throws Exception {
    createDatabase();
    JdbcDataQueryService service = new JdbcDataQueryService();
    service.setConnection(new JdbcConnection(JDBC_URL, JDBC_DRIVER));
    service.setStatementCreator(new ConfiguredSQLStatement(SELECT_WITH_ALIAS));
    service.setResultSetTranslator(new JdbcJsonOutput().withColumnStyle(ColumnStyle.LowerCase));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ExampleServiceCase.execute(service, msg);
    ReadContext ctx = createContext(msg);
    assertEquals("john", ctx.read("$.[0].first_name"));
    assertEquals("smith", ctx.read("$.[0].last_name"));
  }

  @Test
  public void testOutputNoAlias() throws Exception {
    createDatabase();
    JdbcDataQueryService service = new JdbcDataQueryService();
    service.setConnection(new JdbcConnection(JDBC_URL, JDBC_DRIVER));
    service.setStatementCreator(new ConfiguredSQLStatement(SELECT));
    service.setResultSetTranslator(new JdbcJsonOutput());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ExampleServiceCase.execute(service, msg);
    ReadContext ctx = createContext(msg);
    assertEquals("john", ctx.read("$.[0].FIRSTNAME"));
    assertEquals("smith", ctx.read("$.[0].LASTNAME"));
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
      s.execute("INSERT INTO json_people values ('john', 'smith', '1970-01-01')");
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

}
