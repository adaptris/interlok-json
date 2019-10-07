package com.adaptris.core.json.schema;

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
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
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
 * @config json-schema-service
 */
@XStreamAlias("json-schema-service")
@AdapterComponent
@ComponentProfile(summary = "Validate a JSON document against a schema", tag = "service,json,schema,validation,metadata")
public class JsonSchemaService extends ServiceImp {

  @NotNull
  @Valid
  private DataInputParameter<String> schemaUrl;

  @AdvancedConfig
  @NotNull
  @AutoPopulated
  private ValidationExceptionHandler onValidationException;

  @AdvancedConfig
  @NotNull
  @Valid
  @AutoPopulated
  private JsonSchemaLoader jsonSchemaLoader;

  public JsonSchemaService() {
    setOnValidationException(new DefaultValidationExceptionHandler());
    setJsonSchemaLoader(new DefaultJsonSchemaLoader());
  }

  public JsonSchemaService(DataInputParameter<String> url) {
    this();
    setSchemaUrl(url);
  }

  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {
      /* either validate a single JSON object or an array of JSON objects (or fail) */
      JSONObject rawSchema = new JSONObject(schemaUrl.extract(message));
      final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(getJsonSchemaLoader().loadSchema(rawSchema));
      jsonSchemaValidator.validate(asJSON(message.getContent()));
    }
    catch (final ValidationException e) {
      getOnValidationException().handle(e, message);
    }
    catch (NullPointerException | InterlokException | JSONException e) {
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

  public ValidationExceptionHandler getOnValidationException() {
    return onValidationException;
  }

  /**
   * Specify what to do when a schema validation is encountered.
   * 
   * @param v
   */
  public void setOnValidationException(ValidationExceptionHandler v) {
    this.onValidationException = v;
  }

  /**
   * Set the JSON schema loader
   * @param jsonSchemaLoader the JSON schema loader
   */
  public void setJsonSchemaLoader(JsonSchemaLoader jsonSchemaLoader) {
    this.jsonSchemaLoader = jsonSchemaLoader;
  }

  /**
   * Get the JSON schema loader
   * @return the JSON schema loader
   */
  public JsonSchemaLoader getJsonSchemaLoader() {
    return jsonSchemaLoader;
  }
}
