package com.adaptris.core.transform.json;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

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
	 * Reference to the schema to use for validation.
	 */
	@NotNull
	@Valid
	private DataInputParameter<String> schemaUrl = new FileDataInputParameter();

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException {
		try {
			/* retrieve the schema */
			final String schemaString = schemaUrl.extract(message);
			final JSONObject rawSchema = new JSONObject(schemaString);
			final Schema schema = SchemaLoader.load(rawSchema);

			/* parse the JSON from the message body */
			final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			final Object object = jsonParser.parse(message.getInputStream());

			/* either validate a single JSON object or an array of JSON objects (or fail) */
			final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(schema);
			if (object instanceof JSONObject) {
				jsonSchemaValidator.validate((JSONObject)object);
			} else if (object instanceof JSONArray) {
				final JSONArray array = (JSONArray)object;
				for (final Object o : array) {
					jsonSchemaValidator.validate((JSONObject)o);
				}
			} else {
				log.warn("Message payload was not JSON; could not be parsed to JSONObject (" + object.getClass() + ").");
			}

		} catch (final ValidationException | ParseException | IOException | InterlokException e) {
			log.warn("JSON is not valid!", e);
			if (e instanceof ValidationException) {
				for (final ValidationException ve : ((ValidationException)e).getCausingExceptions()) {
					log.debug(ve.getMessage());
				}
			}
			throw new ServiceException(e);
		}
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	protected void closeService() {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	protected void initService() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Set the URL of the schema to use for JSON validation.
	 *
	 * @param schemaUrl
	 *          The URL of the schema to use for validation.
	 */
	public void setSchemaUrl(final DataInputParameter<String> schemaUrl) {
		this.schemaUrl = Args.notNull(schemaUrl, "Schema");
	}

	/**
	 * Get the URL of the schema to use for JSON validation.
	 *
	 * @return The URL of the schema to use for validation.
	 */
	public DataInputParameter<String> getSchemaUrl() {
		return schemaUrl;
	}
}
