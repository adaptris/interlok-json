package com.adaptris.core.json.schema;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private transient ObjectMapper mapper = new ObjectMapper();

  public JsonSchemaService() {
    setOnValidationException(new DefaultValidationExceptionHandler());
  }

  public JsonSchemaService(DataInputParameter<String> url) {
    this();
    setSchemaUrl(url);
  }

  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {
      /* either validate a single JSON object or an array of JSON objects (or fail) */
      final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(loadSchema(message));
      jsonSchemaValidator.validate(asJSON(message.getContent()));
    }
    catch (final ValidationException e) {
      getOnValidationException().handle(e, message);
    }
    catch (NullPointerException | InterlokException | JSONException | IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Object asJSON(String input) throws JSONException, JsonParseException, JsonMappingException, IOException {
    Object result = mapper.readValue(input, Object.class);
    return JSONObject.wrap(result);
  }

  private Schema loadSchema(AdaptrisMessage input) throws JSONException, InterlokException {
    JSONObject rawSchema = new JSONObject(schemaUrl.extract(input));
    return SchemaLoader.load(rawSchema);
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
}
