package com.adaptris.core.json.jslt;

import java.io.Reader;
import java.util.Optional;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Evaluate a JSLT expresson and apply it.
 *
 */
public interface JsltQuery {

  String INPUT_NODE_METADATA_KEY = JsltQuery.class.getCanonicalName();

  final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

  /**
   * Evaluate the expression against the message.
   *
   * @param msg the {@link AdaptrisMessage}
   * @param mapper the ObjectMapper instance to use.
   */
  void evaluate(ObjectMapper mapper, AdaptrisMessage msg) throws Exception;

  /**
   * Evaluate the expression against the message.
   *
   * @implNote The default implementation simply uses {@link #DEFAULT_OBJECT_MAPPER} as the object
   *           mapper.
   * @param msg the {@link AdaptrisMessage}
   * @see #evaluate(ObjectMapper, AdaptrisMessage)
   */
  default void evaluate(AdaptrisMessage msg) throws Exception {
    evaluate(DEFAULT_OBJECT_MAPPER, msg);
  }

  /**
   * Turn an {@code AdaptrisMessage} object into a {@code JsonNode}.
   * <p>
   * It first checks to see if {@value #INPUT_NODE_METADATA_KEY} exists in object metadata and uses
   * that if it does. Otherwise it delegates to {@link #jacksonify(AdaptrisMessage)}.
   *
   * @param msg the {@link AdaptrisMessage}
   * @return a {@code JsonNode}
   * @throws Exception
   */
  static JsonNode getJsonNode(AdaptrisMessage msg) throws Exception {
    return Optional.ofNullable((JsonNode) msg.getObjectHeaders().get(INPUT_NODE_METADATA_KEY))
        .orElse(jacksonify(msg));
  }

  /**
   * Turn an {@code AdaptrisMessage} object into a {@code JsonNode}.
   * <p>
   * Simply delegates to {@link #jacksonify(ObjectMapper, AdaptrisMessage)} using
   * {@link #DEFAULT_OBJECT_MAPPER}.
   * </p>
   *
   * @param msg the {@link AdaptrisMessage}
   * @return a {@code JsonNode}
   *
   */
  static JsonNode jacksonify(AdaptrisMessage msg) throws Exception {
    return jacksonify(DEFAULT_OBJECT_MAPPER, msg);
  }

  /**
   * Turn an {@code AdaptrisMessage} object into a {@code JsonNode}.
   *
   * @param mapper the {@code ObjectMapper}.
   * @param msg the {@link AdaptrisMessage}
   * @return a {@code JsonNode}
   */
  static JsonNode jacksonify(ObjectMapper mapper, AdaptrisMessage msg) throws Exception {
    try (Reader msgIn = msg.getReader()) {
      return mapper.readTree(msgIn);
    }
  }
}
