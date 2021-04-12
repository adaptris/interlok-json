package com.adaptris.core.json.jslt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.util.Args;
import com.fasterxml.jackson.databind.JsonNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Execute a list of JSLT queries turning the result of each query into an item of metadata.
 *
 *
 * @config jslt-metadata-service
 *
 */
@XStreamAlias("jslt-metadata-service")
@AdapterComponent
@ComponentProfile(
    summary = "Execute a list of JSLT queries turning the result of each query into an item of metadata.",
    tag = "jslt,transform,json", since = "3.12.0")
@DisplayOrder(order = {"queries"})
@NoArgsConstructor
public class JsltMetadataService extends ServiceImp {

  private static final String UTF_8 = StandardCharsets.UTF_8.name();

  /**
   * The queries to execute.
   *
   * @see JsltQuery
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  @XStreamImplicit
  private List<JsltQuery> queries = new ArrayList<>();

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      JsonNode input = JsltQuery.jacksonify(msg);
      msg.addObjectHeader(JsltQuery.INPUT_NODE_METADATA_KEY, input);
      for (JsltQuery q : getQueries()) {
        q.evaluate(msg);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getQueries(), "queries");
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public JsltMetadataService withQueries(JsltQuery... queries) {
    return withQueries(new ArrayList<>(Arrays.asList(queries)));
  }

  public JsltMetadataService withQueries(List<JsltQuery> queries) {
    setQueries(queries);
    return this;
  }

}
