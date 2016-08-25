package com.adaptris.core.transform.json;

import java.net.URL;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;

/**
 * Unit tests for {@link JsonSchemaService}.
 *
 * @author Ashley Anderson
 */
public class JsonSchemaServiceTest extends ServiceCase {

	/**
	 * Default constructor.
	 */
	public JsonSchemaServiceTest() {
		super("JSON schema validation service.");
	}

	/**
	 * Non-default constructor.
	 *
	 * @param name
	 *          Test case name.
	 */
	public JsonSchemaServiceTest(final String name) {
		super(name);
	}

	/**
	 * Reference to the test schema.
	 */
	private static final URL SCHEMA_URL = JsonSchemaServiceTest.class.getResource("test_schema.json");
	/**
	 * Valid JSON.
	 */
	private static final String VALID_JSON = "{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }";

	/**
	 * Test for success.
	 *
	 * @throws Exception
	 *           Unexpected test failure.
	 */
	@Test
	public void testSuccess() throws Exception {
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
		final JsonSchemaService service = new JsonSchemaService();
		service.setSchema(SCHEMA_URL);
		execute(service, message);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected Object retrieveObjectForSampleConfig() {
		return null;
	}
}
