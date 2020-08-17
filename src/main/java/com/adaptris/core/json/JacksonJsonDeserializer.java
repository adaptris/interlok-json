package com.adaptris.core.json;

import com.adaptris.annotation.ComponentProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Implementation based using the {@code com.fasterxml.jackson.core:jackson-databind} library.
 *
 * @config jackson-json-deserializer
 */
@XStreamAlias("jackson-json-deserializer")
@ComponentProfile(
    summary = "Convert an AdaptrisMessage using 'com.fasterxml.jackson.core:jackson-databind'")
@NoArgsConstructor
public class JacksonJsonDeserializer implements JsonDeserializer<JsonNode> {
  private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

  @Override
  public JsonNode deserialize(String s) throws Exception {
    return defaultObjectMapper.readTree(s);
  }

}
