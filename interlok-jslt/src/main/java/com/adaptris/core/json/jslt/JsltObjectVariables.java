package com.adaptris.core.json.jslt;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Builds a map of variables for JSLT from object metadata.
 * <p>
 * This allows you to use object metadata that is already of the type {@code JsonNode}; possibly via
 * a previous execution of {@link JsltMetadataService} with a {@link JsltQueryToObjectMetadata}. If
 * the key exists, but is not a JsonNode then an exception is likely.
 * </p>
 *
 * @config jslt-object-variable
 */
@XStreamAlias("jslt-object-variable")
@NoArgsConstructor
public class JsltObjectVariables implements JsltVariableBuilder {


  /**
   * The filter you wish to apply to for object metadata
   * <p>
   * The regular expression should only match object metadata that is of the type {@code JsonNode}.
   * If not specified then no object metadata is included.
   * </p>
   *
   */
  @Getter
  @Setter
  private String objectMetadataKeyRegexp;

  @Override
  public Map<String, JsonNode> build(AdaptrisMessage msg) {
    if (isEmpty(getObjectMetadataKeyRegexp())) {
      return Collections.EMPTY_MAP;
    }
    Pattern pattern = Pattern.compile(getObjectMetadataKeyRegexp());
    Map<Object, Object> objMetadata = msg.getObjectHeaders();
    return objMetadata.entrySet().stream()
        .filter((e) -> pattern.matcher(e.getKey().toString()).matches())
        .collect(Collectors.toMap((e) -> e.getKey().toString(), (e) -> (JsonNode) e.getValue()));
  }

  public JsltObjectVariables withObjectMetadataKeyRegexp(String regexp) {
    setObjectMetadataKeyRegexp(regexp);
    return this;
  }
}
