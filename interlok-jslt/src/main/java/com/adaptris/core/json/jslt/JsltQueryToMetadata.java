package com.adaptris.core.json.jslt;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Evaluate a JSLT expression rendering the result as a string and store as metadata.
 *
 * @config jslt-to-metadata
 */
@XStreamAlias("jslt-to-metadata")
@ComponentProfile(
    summary = "Execute a JSLT expression rendering the result as a string and store as metadata.",
    tag = "jslt,transform,json", since = "3.12.0")
@NoArgsConstructor
public class JsltQueryToMetadata extends JsltQueryImpl {

  @Override
  public void evaluate(final ObjectMapper mapper, AdaptrisMessage msg) throws Exception {
    Expression expr = compile(msg);
    String key = metadataKey(msg);
    JsonNode input = JsltQuery.getJsonNode(msg);
    JsonNode output = expr.apply(input);
    msg.addMessageHeader(key, mapper.writeValueAsString(output));
  }

}
