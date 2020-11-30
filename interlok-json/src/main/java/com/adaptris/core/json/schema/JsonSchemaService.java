package com.adaptris.core.json.schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.json.BasicJsonDeserializer;
import com.adaptris.core.json.JacksonJsonDeserializer;
import com.adaptris.core.json.JsonDeserializer;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * JSON schema validation service.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 * @config json-schema-service
 */
@XStreamAlias("json-schema-service")
@AdapterComponent
@ComponentProfile(summary = "Validate a JSON document against a schema", tag = "service,json,schema,validation,metadata")
@DisplayOrder(order = {"schemaUrl", "deserializer", "onValidationException", "jsonSchemaLoader",})
public class JsonSchemaService extends ServiceImp {

  /**
   * The URL of the schema to use for JSON validation.
   */
  @NotNull
  @Valid
  @Getter
  @Setter
  @NonNull
  private DataInputParameter<String> schemaUrl;

  /**
   * What to do when a schema validation is encountered.
   *
   */
  @AdvancedConfig
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  @InputFieldDefault(value = "throw an exception")
  private ValidationExceptionHandler onValidationException;

  /**
   * How we load the schema from the URL you have specified
   *
   */
  @AdvancedConfig
  @NotNull
  @Valid
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private JsonSchemaLoader jsonSchemaLoader;

  /**
   * Configure a {@link JsonDeserializer} if you want to explicitly dictate the behaviour when
   * attempting to convert to JSON.
   * <p>
   * Note that {@link BasicJsonDeserializer} can coerce invalid json into validity (e.g. {@code {
   * "key": value}} is coerced into {@code {"key":"value"}} under the covers which means that your
   * input might pass schema validation but isn't really valid json.
   * </p>
   * <p>
   * Switch to {@link JacksonJsonDeserializer} if you want a stricter interpretation of the json
   * data before validating it against a schema. This defaults to {@link BasicJsonDeserializer} for
   * backwards compatibility reasons.
   * </p>
   *
   */
  @AdvancedConfig
  @Valid
  @Getter
  @Setter
  @InputFieldDefault(value = "basic-json-deserializer")
  private JsonDeserializer deserializer;

  // Since Json Schema only supports org.json objects, we have to use
  // BasicJsonDeserializer before we can use the json schema validator.
  private static final JsonDeserializer SCHEMA_JSONER = new BasicJsonDeserializer();

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
      Schema schema = buildSchema(message);
      schema.validate(asJSON(message));
    }
    catch (final ValidationException e) {
      getOnValidationException().handle(e, message);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Schema buildSchema(AdaptrisMessage msg) throws Exception {
    String schemaString = getSchemaUrl().extract(msg);
    // attempt to deserialize the string, to check if it's valid json.
    deserializer().deserialize(schemaString);
    return getJsonSchemaLoader().loadSchema(new JSONObject(schemaString));
  }

  private Object asJSON(AdaptrisMessage msg) throws Exception {
    // Don't care what the result is, just that it doesn't choke
    // on invalid json.
    deserializer().deserialize(msg);
    // Now use the one we really need to use.
    return SCHEMA_JSONER.deserialize(msg);
  }

  private JsonDeserializer deserializer() {
    return ObjectUtils.defaultIfNull(getDeserializer(), SCHEMA_JSONER);
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getSchemaUrl(), "schema-url");
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
  }

}
