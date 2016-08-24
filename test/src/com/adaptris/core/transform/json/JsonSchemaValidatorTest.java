package com.adaptris.core.transform.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;
import org.junit.Test;

/**
 * Unit tests for {@link JsonSchemaValidator}.
 *
 * @author Ashley Anderson
 */
public class JsonSchemaValidatorTest {

	/**
	 * Valid test schema.
	 */
	private static final URL VALID_SCHEMA = JsonSchemaValidatorTest.class.getResource("test_schema.json");

	/**
	 * Valid JSON.
	 */
	private static final JSONObject VALID_JSON = new JSONObject("{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }");

	/**
	 * Invalid JSON.
	 */
	private static final JSONObject INVALID_JSON = new JSONObject("{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }");

	/**
	 * Test for success with valid JSON; set schema using constructor and JSON using set method.
	 *
	 * @throws IOException
	 *           If the test fails.
	 */
	@Test
	public void testSuccessValid1() {
		final JsonSchemaValidator jsv = new JsonSchemaValidator(VALID_SCHEMA);
		jsv.setJson(VALID_JSON);
		assertTrue(jsv.isValid());
	}

	/**
	 * Test for success with valid JSON; set JSON using constructor and schema using set method.
	 *
	 * @throws IOException
	 *           If the test fails.
	 */
	@Test
	public void testSuccessValid2() {
		final JsonSchemaValidator jsv = new JsonSchemaValidator(VALID_JSON);
		jsv.setSchema(VALID_SCHEMA);
		assertTrue(jsv.isValid());
	}

	/**
	 * Test for success with valid JSON; set schema and JSON using constructor.
	 *
	 * @throws IOException
	 *           If the test fails.
	 */
	@Test
	public void testSuccessValid3() {
		assertTrue(new JsonSchemaValidator(VALID_SCHEMA, VALID_JSON).isValid());
	}

	/**
	 * Test for expected failure with invalid JSON. {@link JsonSchemaValidator#isValid()} should return false if the JSON doesn't adhere to the schema.
	 *
	 * @throws IOException
	 *           If the test fails.
	 */
	@Test
	public void testInvalidJson() {
		assertFalse(new JsonSchemaValidator(VALID_SCHEMA, INVALID_JSON).isValid());
	}

	/**
	 * Test for expected failure with null schema/JSON. {@link JsonSchemaValidator#isValid()} should return false.
	 *
	 * @throws IOException
	 *           If the test fails.
	 */
	@Test
	public void testNull() {
		final JsonSchemaValidator jsv = new JsonSchemaValidator();
		jsv.setSchema(VALID_SCHEMA);
		assertFalse(jsv.isValid());
	}
}
