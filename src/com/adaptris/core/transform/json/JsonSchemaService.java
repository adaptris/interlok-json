package com.adaptris.core.transform.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
	private final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();

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

		/*
		 * try {
		 * String shiftrContent = this.getMappingSpec().extract(message);
		 * shiftrContent = this.applyMetadataSubstitution(message, shiftrContent);
		 * 
		 * final List<Object> chainrSpecJSON = JsonUtils.jsonToList(shiftrContent, defaultIfEmpty(message.getContentEncoding(), "UTF-8"));
		 * final Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
		 * final Object inputJSON = JsonUtils.jsonToObject(this.getSourceJson().extract(message));
		 * final Object transformedOutput = chainr.transform(inputJSON);
		 * getTargetJson().insert(JsonUtils.toJsonString(transformedOutput), message);
		 * } catch (final Exception ex) {
		 * throw new ServiceException(ex);
		 * }
		 */
	}

	/**
	 * Unused method.
	 */
	@Override
	public void prepare() throws CoreException {
		// unused/empty method
	}

	/**
	 * Unused method.
	 */
	@Override
	protected void closeService() {
		// unused/empty method
	}

	/**
	 * Unused method.
	 */
	@Override
	protected void initService() throws CoreException {
		// unused/empty method
	}

	/**
	 * Set the schema to use for JSON validation.
	 *
	 * @param schemaUrl
	 *          The URL to the schema.
	 */
	public void setSchema(final URL schemaUrl) {
		try (final InputStream is = schemaUrl.openStream()) {
			final JSONObject rawSchema = new JSONObject(new JSONTokener(is));
			jsonSchemaValidator.setSchema(SchemaLoader.load(rawSchema));
		} catch (final IOException e) {
			log.error("Could not access JSON schema URL : " + schemaUrl, e);
		}
	}
}
