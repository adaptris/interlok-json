package com.adaptris.core.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility methods.
 * 
 *
 */
public class JsonUtil {

  /**
   * Turn a flat JSON object into a map.
   * <p>
   * Nested JSON values are rendered as a string using {@code JsonNode#asText()}.
   * </p>
   * 
   * @param msg the AdaptrisMessage
   * @return a Map of strings.
   */
  public static Map<String, String> mapifyJson(final AdaptrisMessage msg) throws IOException {
    Map<String, String> result = new LinkedHashMap<>();
    try (BufferedReader buf = new BufferedReader(msg.getReader())) {
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = mapper.readTree(parser);
        for (String key : makeIterable(node.fieldNames())) {
          JsonNode field = node.get(key);
          if (field.isValueNode()) {
            result.put(key, field.asText());
          } else {
            result.put(key, field.toString());
          }
        }
      } else {
        throw new IOException("Message did not start with '{', cannot parse.");
      }

    }
    return result;
  }


  private static Iterable<String> makeIterable(final Iterator<String> itr) {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return itr;
      }
    };
  }

}
