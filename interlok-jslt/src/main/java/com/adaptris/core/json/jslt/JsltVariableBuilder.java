package com.adaptris.core.json.jslt;

import java.util.Map;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Wraps various {@link AdaptrisMessage} items as variables for JSLT.
 *
 */
public interface JsltVariableBuilder {

  Map<String, JsonNode> build(AdaptrisMessage msg);
}
