package com.adaptris.core.json.streaming;

import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public abstract class JsonStreamBuilderImpl {

  /**
   * Configuration that dictates the behaviour of the underlying streaming instances.
   *
   */
  @Getter
  @Setter
  @Valid
  @AdvancedConfig
  private JsonStreamingConfigBuilder config;

  public <T extends JsonStreamBuilderImpl> T withConfig(JsonStreamingConfigBuilder b) {
    setConfig(b);
    return (T) this;
  }

  public JsonStreamingConfigBuilder config() {
    return ObjectUtils.defaultIfNull(getConfig(), new JsonStreamingConfigBuilder());
  }
}
