package com.adaptris.core.transform.json;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.transform.TransformServiceExample;

/**
 * Unit tests for {@link JsonSchemaService}.
 *
 * @author Ashley Anderson
 */
public class JsonSchemaServiceTest extends /* ServiceCase */TransformServiceExample {

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
	private static final String SCHEMA_URL = "file:///com/adaptris/core/transform/json/test_schema.json";

	/**
	 * Valid JSON.
	 */
	private static final String VALID_JSON = "{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }";

	/**
	 * The service being tested.
	 */
	private JsonSchemaService service;

	/**
	 * The test Adaptris message.
	 */
	private AdaptrisMessage message;

	/**
	 * Setup the test environment.
	 */
	@Override
	@Before
	public void setUp() {
		service = new JsonSchemaService();
		final FileDataInputParameter schemaUrl = new FileDataInputParameter();
		schemaUrl.setUrl(SCHEMA_URL);
		service.setSchemaUrl(schemaUrl);
		message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
	}

	/**
	 * Test for success.
	 *
	 * @throws Exception
	 *           Unexpected test failure.
	 */
	@Test
	public void testSuccess() throws Exception {
		execute(service, message);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected Object retrieveObjectForSampleConfig() {
		return service;
	}
}
