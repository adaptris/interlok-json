package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;

/**
 * Tests for JDBC to JSON translator {@link JsonResultSetTranslator}.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
public class JsonResultSetTranslatorTest {

	private JSONObject expected;

	private final JdbcResult result = new JdbcResult();

	/**
	 * Initialise the unit test environment.
	 *
	 * @throws Exception
	 *           If initialisation could not occur.
	 */
	@Before
	public void setUp() throws Exception {
		expected = new JSONObject(
				"{\"result\":[{\"firstName\":\"John\",\"lastName\":\"Doe\"},{\"firstName\":\"Anna\",\"lastName\":\"Smith\"},{\"firstName\":\"Peter\",\"lastName\":\"Jones\"}]}");

		final List<String> fieldNames = Arrays.asList("firstName", "lastName");

		final JdbcResultRow row_1 = new JdbcResultRow();
		row_1.setFieldNames(fieldNames);
		row_1.setFieldValues(Arrays.asList("John", "Doe"));

		final JdbcResultRow row_2 = new JdbcResultRow();
		row_2.setFieldNames(fieldNames);
		row_2.setFieldValues(Arrays.asList("Anna", "Smith"));

		final JdbcResultRow row_3 = new JdbcResultRow();
		row_3.setFieldNames(fieldNames);
		row_3.setFieldValues(Arrays.asList("Peter", "Jones"));

		@SuppressWarnings("resource")
		final JdbcResultSet mockResultSet = mock(JdbcResultSet.class);
		when(mockResultSet.getRows()).thenReturn(Arrays.asList(row_1, row_2, row_3));

		result.setResultSets(Arrays.asList(mockResultSet));

	}

	/**
	 * Test the simple, valid path through the translation from a JDBC result to JSON.
	 *
	 * @throws Exception
	 *           Unexpected; should not happen.
	 */
	@Test
	public void testTranslate() {
		final JsonResultSetTranslator jsonTranslator = new JsonResultSetTranslator();
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();

		jsonTranslator.translate(result, message);

		assertEquals(expected.toString(), new String(message.getPayload()));
	}

}
