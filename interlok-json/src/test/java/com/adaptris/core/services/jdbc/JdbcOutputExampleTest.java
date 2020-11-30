package com.adaptris.core.services.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.adaptris.core.Service;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.json.jdbc.JdbcJsonArrayOutput;
import com.adaptris.core.json.jdbc.JdbcJsonOutput;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JdbcOutputExampleTest extends ExampleServiceCase {
  private static final String HYPHEN = "-";
  public static final String BASE_DIR_KEY = "JsonJDBCServiceExamples.baseDir";

  private enum ResultSetBuilder {
    JSON_OBJECT {
      @Override
      public ResultSetTranslator build() {
        return new JdbcJsonOutput();
      }

    },
    JSON_ARRAY {
      @Override
      public ResultSetTranslator build() {
        return new JdbcJsonArrayOutput();
      }
    };

    public abstract ResultSetTranslator build();
  }
  public JdbcOutputExampleTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected final List<Service> retrieveObjectsForSampleConfig() {
    ArrayList<Service> result = new ArrayList<>();
    List<JdbcDataQueryService> services = buildExamples();
    for (JdbcDataQueryService s : services) {
      s.setConnection(build());
      result.add(s);
    }
    return result;
  }

  protected List<JdbcDataQueryService> buildExamples() {
    ArrayList<JdbcDataQueryService> result = new ArrayList<>();
    for (ResultSetBuilder e : ResultSetBuilder.values()) {
      JdbcDataQueryService service = new JdbcDataQueryService(new ConfiguredSQLStatement("SELECT * FROM mytable"));
      service.setResultSetTranslator(e.build());
      result.add(service);
    }
    return result;
  }

  private DatabaseConnection build() {
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setUsername("my_db_username");
    connection.setPassword("plain or encoded password");
    connection.setConnectionProperties(new KeyValuePairSet(
        Arrays.asList(new KeyValuePair("dontTrackOpenResources", "true"), new KeyValuePair("autoReconnect", "true"))));
    return connection;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return object.getClass().getSimpleName() + HYPHEN
        + ((JdbcDataQueryService) object).getResultSetTranslator().getClass().getName();
  }
}
