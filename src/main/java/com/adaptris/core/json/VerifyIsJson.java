package com.adaptris.core.json;

import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Verify that the payload is considered json.
 *
 * <p>
 * Verifies that the message payload is in fact json and throws an exception if it is not
 * </p>
 *
 * @config verify-is-json
 */
@XStreamAlias("verify-is-json")
@AdapterComponent
@ComponentProfile(summary = "Verify that the payload is json using the configured deserializer",
    tag = "service,json", since = "3.11.0")
@DisplayOrder(order = {"deserializer"})
@NoArgsConstructor
public class VerifyIsJson extends ServiceImp {
  /**
   * Configure a {@link JsonDeserializer} to check that the message is in fact JSON.
   * <p>
   * Note that {@link BasicJsonDeserializer} can coerce invalid json into validity (e.g. {@code {
   * "key": value}} is coerced into {@code {"key":"value"}} under the covers which means that it
   * isn't really valid json and might cause failures elsewhere...
   * </p>
   * <p>
   * This defaults to {@link JacksonJsonDeserializer} for strictness.
   * </p>
   *
   */
  @AdvancedConfig
  @Valid
  @Getter
  @Setter
  @InputFieldDefault(value = "jackson-json-deserializer")
  private JsonDeserializer deserializer;

  private static final JsonDeserializer DEFAULT_DESERIALIZER = new JacksonJsonDeserializer();

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      deserializer().deserialize(msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public VerifyIsJson withDeserializer(JsonDeserializer s) {
    setDeserializer(s);
    return this;
  }

  private JsonDeserializer deserializer() {
    return ObjectUtils.defaultIfNull(getDeserializer(), DEFAULT_DESERIALIZER);
  }

}
