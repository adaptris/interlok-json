package com.adaptris.core.json.jslt;

import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Builds a map of variables for JSLT from normal message metadata.
 * <p>
 * Note that since metadata is considered internally as Strings, then all the resulting
 * {@code JsonNode} will be a {@code TextNode} using {@code TextNode.valueOf(String)}.
 * </p>
 *
 * @config jslt-metadata-variable
 */
@XStreamAlias("jslt-metadata-variable")
@NoArgsConstructor
public class JsltMetadataVariables implements JsltVariableBuilder {

  /**
   * The filter you wish to apply to metadata values.
   * <p>
   * The default if not configured is {@link RemoveAllMetadataFilter}.
   * </p>
   *
   */
  @Getter
  @Setter
  @Valid
  private MetadataFilter filter;


  private MetadataFilter filter() {
    return ObjectUtils.defaultIfNull(getFilter(), new RemoveAllMetadataFilter());
  }

  @Override
  public Map<String, JsonNode> build(AdaptrisMessage msg) {
    return filter().filter(msg).stream()
        .collect(Collectors.toMap((e) -> e.getKey(), (e) -> TextNode.valueOf(e.getValue())));
  }

  public JsltMetadataVariables withFilter(MetadataFilter f) {
    setFilter(f);
    return this;
  }

}
