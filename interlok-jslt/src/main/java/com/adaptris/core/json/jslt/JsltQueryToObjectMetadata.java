package com.adaptris.core.json.jslt;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;


/**
 * Evaluate a JSLT expression and store the resulting {@code JsonNode} as object metadata against
 * the specified key.
 *
 * @config jslt-to-object-metadata
 */
@XStreamAlias("jslt-to-object-metadata")
@ComponentProfile(
    summary = "Execute a JSLT query and store the resulting JsonNode as object metadata against the specified key.",
    tag = "jslt,transform,json", since = "3.12.0")
@NoArgsConstructor
public class JsltQueryToObjectMetadata extends JsltQueryImpl {

  @Override
  public void evaluate(final ObjectMapper mapper, AdaptrisMessage msg) throws Exception {
    Expression expr = compile(msg);
    String key = metadataKey(msg);
    JsonNode input = JsltQuery.getJsonNode(msg);
    JsonNode output = expr.apply(input);
    msg.addObjectHeader(key, output);
  }

}
