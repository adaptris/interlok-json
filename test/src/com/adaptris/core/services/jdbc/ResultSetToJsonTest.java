package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.json.jdbc.JdbcJsonOutput;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;


public class ResultSetToJsonTest {

	private final JdbcResult result = new JdbcResult();

	@Before
	public void setUp() throws Exception {
		final List<String> fieldNames = Arrays.asList("firstName", "lastName");

		final JdbcResultRow row_1 = new JdbcResultRow();
		row_1.setFieldNames(fieldNames);
		row_1.setFieldValues(Arrays.asList((Object)"John", (Object)"Doe"));

		final JdbcResultRow row_2 = new JdbcResultRow();
		row_2.setFieldNames(fieldNames);
		row_2.setFieldValues(Arrays.asList((Object)"Anna", (Object)"Smith"));

		final JdbcResultRow row_3 = new JdbcResultRow();
		row_3.setFieldNames(fieldNames);
		row_3.setFieldValues(Arrays.asList((Object)"Peter", (Object)"Jones"));

    final JdbcResultSet mock1 = mock(JdbcResultSet.class);
    when(mock1.getRows()).thenReturn(Arrays.asList(row_1, row_2, row_3));
    final JdbcResultSet mock2 = mock(JdbcResultSet.class);
    when(mock2.getRows()).thenReturn(Arrays.asList(row_1, row_2, row_3));

    result.setResultSets(Arrays.asList(mock1, mock2));
	}


	@Test
  public void testTranslate() throws Exception {
    JdbcJsonOutput jsonTranslator = new JdbcJsonOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();

		jsonTranslator.translate(result, message);
    System.out.println(message.getContent());
    ReadContext ctx = createContext(message);
    assertNotNull(ctx.read("$.[0].result[0]"));
    assertNotNull(ctx.read("$.[1].result[0]"));
    assertEquals("Anna", ctx.read("$.[0].result[1].firstName"));
	}

  private ReadContext createContext(AdaptrisMessage msg) throws IOException {
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    return JsonPath.parse(msg.getInputStream(), jsonConfig);
  }
}
