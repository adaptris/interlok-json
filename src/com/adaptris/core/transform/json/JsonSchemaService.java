package com.adaptris.core.transform.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JSON schema validation service.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
@XStreamAlias("json-schema-service")
@AdapterComponent
@ComponentProfile(summary = "Validate a JSON document against a schema", tag = "service,json,schema,validation,metadata")
public class JsonSchemaService extends ServiceImp {

	/**
	 * Reference to JSON validator wrapper.
	 */
	private final JsonSchemaValidator jsonSchemaValidator;

	/**
	 * Default constructor for JSON schema validation service.
	 */
	public JsonSchemaService() {
		jsonSchemaValidator = new JsonSchemaValidator();
	}

	/**
	 * Overloaded constructor, which gives the URL to the schema to use for validation.
	 *
	 * @param schemaUrl
	 *          The URL of the schema to use for validation.
	 */
	public JsonSchemaService(final URL schemaUrl) {
		jsonSchemaValidator = new JsonSchemaValidator(getSchema(schemaUrl));
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException {

		try {
			final String j = new String(message.getPayload());
			final JSONObject json = new JSONObject(j);
			jsonSchemaValidator.validate(json);
		} catch (final ValidationException e) {
			log.warn("JSON is not valid!", e);
			for (final ValidationException ve : e.getCausingExceptions()) {
				log.debug(ve.getMessage());
			}
			throw new ServiceException(e);
		}
	}

	/**
	 * Unused method.
	 */
	@Override
	public void prepare() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method.
	 */
	@Override
	protected void closeService() {
		/* unused/empty method */
	}

	/**
	 * Unused method.
	 */
	@Override
	protected void initService() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Set the schema to use for JSON validation.
	 *
	 * @param schemaUrl
	 *          The URL of the schema to use for validation.
	 */
	public void setSchema(final URL schemaUrl) {
		jsonSchemaValidator.setSchema(getSchema(schemaUrl));
	}

	/**
	 * Get the schema from wherever it is.
	 *
	 * @param schemaUrl
	 *          The URL of the schema to use for validation.
	 *
	 * @return The schema to use for validation.
	 */
	private Schema getSchema(final URL schemaUrl) {
		try (final InputStream is = schemaUrl.openStream()) {
			final JSONObject rawSchema = new JSONObject(new JSONTokener(is));
			return SchemaLoader.load(rawSchema);
		} catch (final IOException e) {
			log.error("Could not access JSON schema URL : " + schemaUrl, e);
			return null;
		}
	}
}
