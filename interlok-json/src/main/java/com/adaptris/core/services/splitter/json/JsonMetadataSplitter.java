package com.adaptris.core.services.splitter.json;

import java.io.IOException;
import java.util.Iterator;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.json.JsonToMetadata;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Split a JSON array and immediately create metadata from the split payload.
 * 
 * <p>
 * This is simply a convenience splitter implementation that does a combination of {@link LargeJsonArraySplitter} with
 * {@link JsonToMetadata} in a single step; note that the split message is always empty.
 * </p>
 *
 *
 * @config json-metadata-splitter
 */
@XStreamAlias("json-metadata-splitter")
@ComponentProfile(summary = "Split a JSON Array into individual JSON objects, immediately turning it into metadata",
    since = "3.8.2", tag = "json,splitting")
public class JsonMetadataSplitter extends LargeJsonArraySplitter {

  @Override
  protected JsonSplitGenerator createSplitter(GeneratorConfig cfg) {
    return new MyJsonSplitGenerator(cfg);
  }

  private class MyJsonSplitGenerator extends JsonSplitGenerator {

    public MyJsonSplitGenerator(GeneratorConfig cfg) {
      super(cfg);
    }

    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      final AdaptrisMessage splitMessage = newMessage();
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        final ObjectNode objectNode = mapper.readTree(parser);
        final Iterator<String> fields = objectNode.fieldNames();
        while (fields.hasNext()) {
          final String field = fields.next();
          final String value = objectNode.get(field).textValue();
          splitMessage.addMetadata(field, value);
        }
        return addMetadata(splitMessage);
      }
      return null;
    }
  }
}
