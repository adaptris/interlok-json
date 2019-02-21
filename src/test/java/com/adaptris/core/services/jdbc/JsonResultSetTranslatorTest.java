package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.EnumSet;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.json.jdbc.JsonResultSetTranslator;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * Tests for JDBC to JSON translator {@link JsonResultSetTranslator}.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
@SuppressWarnings("deprecation")
public class JsonResultSetTranslatorTest {

  private JSONObject expected;

  @Before
  public void setUp() throws Exception {
    expected = new JSONObject(
        "{\"result\":[{\"firstName\":\"John\",\"lastName\":\"Doe\"},{\"firstName\":\"Anna\",\"lastName\":\"Smith\"},{\"firstName\":\"Peter\",\"lastName\":\"Jones\"}]}");
  }


  /**
   * Test the simple, valid path through the translation from a JDBC result to JSON.
   *
   * @throws Exception
   *           Unexpected; should not happen.
   */
  @Test
  public void testTranslate() throws Exception {
    final JsonResultSetTranslator jsonTranslator = new JsonResultSetTranslator();
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    execute(jsonTranslator, createJdbcResult(), message);
    ReadContext ctx = createContext(message);
    assertNotNull(ctx.read("$.result"));
    assertNotNull(ctx.read("$.result.[0]"));
    assertEquals("Anna", ctx.read("$.result.[1].firstName"));
    assertEquals(expected.toString(), new String(message.getPayload()));
  }

  @Test
  public void testUniqueId() throws Exception {
    final JsonResultSetTranslator jsonTranslator = new JsonResultSetTranslator();
    assertNull(jsonTranslator.getUniqueId());
    jsonTranslator.setUniqueId("XXX");
    assertEquals("XXX", jsonTranslator.getUniqueId());
  }

  protected static ReadContext createContext(AdaptrisMessage msg) throws IOException {
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    return JsonPath.parse(msg.getInputStream(), jsonConfig);
  }

  protected static void execute(ResultSetTranslator jsonTranslator, JdbcResult result, AdaptrisMessage msg)
      throws Exception {
    try (JdbcResult r = result) {
      LifecycleHelper.prepare(jsonTranslator);
      LifecycleHelper.init(jsonTranslator);
      LifecycleHelper.start(jsonTranslator);
      jsonTranslator.translate(r, msg);
    }
    finally {
      LifecycleHelper.stop(jsonTranslator);
      LifecycleHelper.close(jsonTranslator);
    }
  }

  protected static JdbcResult createJdbcResult() throws Exception {
    JdbcResult result = new JdbcResult();

    final JdbcResultRow row_1 = new JdbcResultRow();
    row_1.setFieldValue("firstName", "John", Types.VARCHAR);
    row_1.setFieldValue("lastName", "Doe", Types.VARCHAR);

    final JdbcResultRow row_2 = new JdbcResultRow();
    row_2.setFieldValue("firstName", "Anna", Types.VARCHAR);
    row_2.setFieldValue("lastName", "Smith", Types.VARCHAR);

    final JdbcResultRow row_3 = new JdbcResultRow();
    row_3.setFieldValue("firstName", "Peter", Types.VARCHAR);
    row_3.setFieldValue("lastName", "Jones", Types.VARCHAR);

    final JdbcResultSet mock1 = mock(JdbcResultSet.class);
    when(mock1.getRows()).thenReturn(Arrays.asList(row_1, row_2, row_3));
    final JdbcResultSet mock2 = mock(JdbcResultSet.class);
    when(mock2.getRows()).thenReturn(Arrays.asList(row_1, row_2, row_3));
    result.setHasResultSet(true);
    result.setResultSets(Arrays.asList(mock1, mock2));
    return result;
  }
}
