package com.adaptris.core.json.jslt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.util.Args;
import com.fasterxml.jackson.databind.JsonNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Wrapper from handling multiple types of variables for inclusion into a JSLT transform.
 *
 * @config jslt-variables
 */
@NoArgsConstructor
@XStreamAlias("jslt-variables")
public class JsltVariables implements JsltVariableBuilder {

  /**
   * A list of variables that you want to insert into the transform.
   *
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  @Valid
  @XStreamImplicit
  private List<JsltVariableBuilder> variableBuilders = new ArrayList<>();

  @Override
  public Map<String, JsonNode> build(AdaptrisMessage msg) {
    Args.notNull(getVariableBuilders(), "variable-builders");
    // Map<String, JsonNode> result = new HashMap<>();
    // for (JsltVariableBuilder v : getVariableBuilders()) {
    // result.putAll(v.build(msg));
    // }
    // return Collections.unmodifiableMap(result);
    //
    return Collections.unmodifiableMap(getVariableBuilders().stream()
            .flatMap((b) -> b.build(msg).entrySet().stream())
            .collect(Collectors.toMap((v) -> v.getKey(), (v) -> v.getValue())));
  }

  public JsltVariables withVariableBuilders(List<JsltVariableBuilder> list) {
    setVariableBuilders(list);
    return this;
  }

  public JsltVariables withVariableBuilders(JsltVariableBuilder... list) {
    return withVariableBuilders(new ArrayList<>(Arrays.asList(list)));
  }

}
