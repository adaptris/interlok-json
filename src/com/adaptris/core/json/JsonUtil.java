package com.adaptris.core.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.text.NullConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility methods.
 * 
 *
 */
public abstract class JsonUtil {
  
  /**
   * Turn a flat JSON object into a map.
   * 
   * @param msg the AdaptrisMessage
   * @return a Map of strings.
   * @see #mapifyJson(AdaptrisMessage, NullConverter)
   */
  public static Map<String, String> mapifyJson(final AdaptrisMessage msg) throws IOException {
    return mapifyJson(msg, null);
  }

  /**
   * Turn a flat JSON object into a map.
   * <p>
   * Nested JSON values are rendered as a string using {@code JsonNode#toString()}.
   * </p>
   * 
   * @param msg the AdaptrisMessage
   * @param nc the {@link NullConverter} implementation to use in the event of a {@code NullNode}; if null then
   *        {@code NullNode#asText()} is used (which returns "null" as opposed to {@code null}).
   * @return a Map of strings.
   */
  public static Map<String, String> mapifyJson(final AdaptrisMessage msg, NullConverter nc) throws IOException {
    Map<String, String> result = new LinkedHashMap<>();
    try (BufferedReader buf = new BufferedReader(msg.getReader())) {
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = mapper.readTree(parser);
        for (String key : makeIterable(node.fieldNames())) {
          result.put(key, toString(node.get(key), nc));
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
  
  private static String toString(JsonNode node, NullConverter nc) {
    if (node instanceof NullNode) {
      return nc != null ? nc.convert((String) null) : node.asText();
    }
    if (node.isValueNode()) {
      return node.asText();
    }
    return node.toString();
  }
}
