package com.adaptris.core.json.streaming;

import javax.validation.Valid;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.transform.json.TransformationDirection;
import com.adaptris.core.transform.json.TransformationDriver;
import com.adaptris.stax.XmlInputFactoryBuilder;
import com.adaptris.stax.XmlOutputFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link TransformationDriver} implementation that allows customisation of a
 * {@link JsonStreamingConfigBuilder}.
 *
 * @config advanced-streaming-transformation-driver
 *
 */
@XStreamAlias("advanced-streaming-transformation-driver")
@NoArgsConstructor
@DisplayOrder(order = {"config"})
@ComponentProfile(summary = "streaming xml/json driver that allows customisations",
    tag = "json,xml", since = "3.11.0")
public class AdvancedStreamingTransformationDriver extends DefaultStreamingTransformationDriver {
  /**
   * Configuration that dictates the behaviour of the underlying streaming instances.
   *
   */
  @Getter
  @Valid
  @Setter
  private JsonStreamingConfigBuilder config;

  public AdvancedStreamingTransformationDriver withConfig(JsonStreamingConfigBuilder b) {
    setConfig(b);
    return this;
  }

  private JsonStreamingConfigBuilder config() {
    return ObjectUtils.defaultIfNull(getConfig(), new JsonStreamingConfigBuilder());
  }

  @Override
  protected XMLInputFactory inputBuilder(TransformationDirection dir) {
    if (dir == TransformationDirection.JSON_TO_XML) {
      ((XmlInputFactoryBuilder) new JsonStreamingInputFactory().withConfig(config())).build();
    }
    return super.inputBuilder(dir);
  }


  @Override
  protected XMLOutputFactory outputBuilder(TransformationDirection dir) {
    if (dir == TransformationDirection.XML_TO_JSON) {
      return ((XmlOutputFactoryBuilder) new JsonStreamingOutputFactory().withConfig(config()))
          .build();
    }
    return super.outputBuilder(dir);
  }
}
