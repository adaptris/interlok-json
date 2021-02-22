package com.adaptris.core.json.jslt;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Builds a map of variables for JSLT from a fixed set of configuration.
 * <p>
 * Takes the configured {@link KeyValuePairSet} and turns them into a map of {@code TextNode}
 * objects.
 * </p>
 *
 * @config jslt-fixed-variables
 */
@XStreamAlias("jslt-fixed-variables")
@NoArgsConstructor
public class JsltFixedVariables implements JsltVariableBuilder {


  /**
   * The fixed variables you wish to pass as part of the JLST Transform.
   *
   */
  @Getter
  @Setter
  @Valid
  private KeyValuePairSet variables = new KeyValuePairSet();


  private KeyValuePairSet variables() {
    return ObjectUtils.defaultIfNull(getVariables(), new KeyValuePairSet());
  }

  @Override
  public Map<String, JsonNode> build(AdaptrisMessage msg) {
    return variables().stream()
        .collect(Collectors.toMap((v) -> v.getKey(), (v) -> TextNode.valueOf(v.getValue())));
  }

  public JsltFixedVariables withVariables(KeyValuePair... kvps) {
    return withVariables(new KeyValuePairSet(Arrays.asList(kvps)));
  }

  public JsltFixedVariables withVariables(KeyValuePairSet kvps) {
    setVariables(kvps);
    return this;
  }
}
