package com.adaptris.core.json.streaming;

import javax.xml.stream.XMLOutputFactory;
import com.adaptris.stax.XmlOutputFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import lombok.NoArgsConstructor;

/**
 * {@link com.adaptris.stax.XmlOutputFactoryBuilder} implementation that allows us to write JSON via standard
 * {@code javax.xml.stream} interfaces.
 *
 * @config json-streaming-output
 * @since 3.8.3
 */
@XStreamAlias("json-streaming-output")
@NoArgsConstructor
public class JsonStreamingOutputFactory extends JsonStreamBuilderImpl implements XmlOutputFactoryBuilder {

  @Override
  public XMLOutputFactory build() {
    return new JsonXMLOutputFactory(config().build());
  }

}
