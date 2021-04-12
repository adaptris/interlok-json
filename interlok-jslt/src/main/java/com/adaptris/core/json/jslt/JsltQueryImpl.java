package com.adaptris.core.json.jslt;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.util.Args;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abstract class containing config for {@link JsltQueryToMetadata} and
 * {@link JsltQueryToObjectMetadata}.
 *
 */
@NoArgsConstructor
public abstract class JsltQueryImpl implements JsltQuery {

  /**
   * The JSLT expression.
   *
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldHint(expression = true)
  private String expression;

  /**
   * The metadata key (object or standard).
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldHint(expression = true)
  private String key;

  public <T extends JsltQueryImpl> T withExpression(String s) {
    setExpression(s);
    return (T) this;
  }

  public <T extends JsltQueryImpl> T withKey(String s) {
    setKey(s);
    return (T) this;
  }

  /**
   * Resolve and compile the expression.
   *
   */
  protected Expression compile(AdaptrisMessage msg) throws Exception {
    // little bit redundant, but is JsltQueryImpl going to contain a list of extension functions?
    // Or caching? undecided, we'll just have to see.
    String expr = msg.resolve(Args.notBlank(getExpression(), "query"));
    return Parser.compileString(expr);
  }

  protected String metadataKey(AdaptrisMessage msg) {
    return msg.resolve(Args.notBlank(getKey(), "key"));
  }
}
