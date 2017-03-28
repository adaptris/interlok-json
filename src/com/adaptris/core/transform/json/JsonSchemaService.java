package com.adaptris.core.transform.json;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
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

  @NotNull
  @Valid
  private DataInputParameter<String> schemaUrl;

  public JsonSchemaService() {

  }

  public JsonSchemaService(DataInputParameter<String> url) {
    this();
    setSchemaUrl(url);
  }

  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {
      /* retrieve the schema */
      final String schemaString = schemaUrl.extract(message);
      final JSONObject rawSchema = new JSONObject(schemaString);
      final Schema schema = SchemaLoader.load(rawSchema);

      /* either validate a single JSON object or an array of JSON objects (or fail) */
      final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(schema);
      jsonSchemaValidator.validate(asJSON(message.getContent()));

    }
    catch (final ValidationException e) {
      for (final ValidationException ve : e.getCausingExceptions()) {
        log.error(ve.getMessage());
      }
      throw ExceptionHelper.wrapServiceException(e);
    }
    catch (InterlokException | JSONException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Object asJSON(String input) throws JSONException {
    Object result = null;
    try {
      result = new JSONObject(new JSONTokener(input));
    }
    catch (final JSONException e) {
      result = new JSONArray(new JSONTokener(input));
    }
    return result;
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
    if (schemaUrl == null) {
      throw new CoreException("SchemaUrl is null");
    }
  }

  /**
   * Set the URL of the schema to use for JSON validation.
   *
   * @param schemaUrl The URL of the schema to use for validation.
   */
  public void setSchemaUrl(final DataInputParameter<String> schemaUrl) {
    this.schemaUrl = Args.notNull(schemaUrl, "schemaUrl");
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
