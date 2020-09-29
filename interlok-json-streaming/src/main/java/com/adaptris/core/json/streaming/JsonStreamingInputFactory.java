package com.adaptris.core.json.streaming;

import javax.xml.stream.XMLInputFactory;
import com.adaptris.stax.XmlInputFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import lombok.NoArgsConstructor;

/**
 * {@link com.adaptris.stax.XmlInputFactoryBuilder} implementation that allows us to read JSON via standard {@code javax.xml.stream}
 * interfaces.
 *
 * @config json-streaming-input
 * @since 3.8.3
 */
@XStreamAlias("json-streaming-input")
@NoArgsConstructor
public class JsonStreamingInputFactory extends JsonStreamBuilderImpl implements XmlInputFactoryBuilder {

  @Override
  public XMLInputFactory build() {
    return new JsonXMLInputFactory(config().build());
  }
}
