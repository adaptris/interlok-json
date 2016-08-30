package com.adaptris.core.transform.json;

import java.io.IOException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON schema validator. Currently makes use of (depends on) json-schema (everit.org).
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
public class JsonSchemaValidator {

	/**
	 * Default logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaValidator.class.getName());

	/**
	 * The JSON schema to use for validation.
	 */
	private Schema schema;

	/**
	 * The JSON to validate against the schema.
	 */
	private JSONObject json;

	/**
	 * Default constructor.
	 */
	public JsonSchemaValidator() {
		/* empty constructor */
	}

	/**
	 * Create a new JSON schema validation instance, using the schema at the given URL.
	 *
	 * @param schema
	 *          The schema to use for validation.
	 *
	 * @throws IOException
	 *           If the URL isn't valid, or doesn't resolve to a JSON schema.
	 */
	public JsonSchemaValidator(final Schema schema) {
		setSchema(schema);
	}

	/**
	 * Create a new JSON schema validation instance for the given JSON.
	 *
	 * @param json
	 *          The JSON to validate.
	 */
	public JsonSchemaValidator(final JSONObject json) {
		setJson(json);
	}

	/**
	 * Create a new JSON schema validation instance for the given JSON against the schema at the given URL.
	 *
	 * @param schema
	 *          The schema to use for validation.
	 * @param json
	 *          The JSON to validate.
	 *
	 * @throws IOException
	 *           If the URL isn't valid, or doesn't resolve to a JSON schema.
	 */
	public JsonSchemaValidator(final Schema schema, final JSONObject json) {
		setSchema(schema);
		setJson(json);
	}

	/**
	 * Set the schema to use for validation.
	 *
	 * @param schema
	 *          The schema to use for validation.
	 *
	 * @throws IOException
	 *           If the URL isn't valid, or doesn't resolve to a JSON schema.
	 */
	public void setSchema(final Schema schema) {
		this.schema = schema;
	}

	/**
	 * Validate the given JSON against the schema.
	 *
	 * @param json
	 *          The JSON to validate.
	 *
	 * @throws ValidationException
	 *           If the JSON is invalid against this schema; use {@link ValidationException#getCausingExceptions()} to identify the cause(s).
	 */
	public void validate(@SuppressWarnings("hiding") final JSONObject json) throws ValidationException {
		schema.validate(json);
	}

	/**
	 * Set the JSON that needs to be validated against the schema.
	 *
	 * @param json
	 *          The JSON to validate.
	 */
	public void setJson(final JSONObject json) {
		this.json = json;
	}

	/**
	 * Validate the JSON against the schema.
	 *
	 * @return True if the JSON is valid when compared with the schema, false otherwise.
	 */
	public boolean isValid() {
		try {
			validate(json);
			return true;
		} catch (final ValidationException e) {
			LOGGER.warn("JSON is not valid!", e);
			for (final ValidationException ve : e.getCausingExceptions()) {
				LOGGER.debug(ve.getMessage());
			}
			return false;
		}
	}
}
