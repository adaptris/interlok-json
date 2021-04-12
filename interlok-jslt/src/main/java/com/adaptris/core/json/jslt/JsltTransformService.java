package com.adaptris.core.json.jslt;

import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.NullCacheImplementation;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import com.adaptris.interlok.util.ResourceLocator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Execute a JSLT Transform, using the result as the payload
 *
 * @config jslt-transform-service
 *
 */
@XStreamAlias("jslt-transform-service")
@AdapterComponent
@ComponentProfile(summary = "Execute a JSLT Transform, using the result as the payload",
    tag = "jslt,transform,json", since = "3.12.0", recommended = {CacheConnection.class})
@DisplayOrder(order = {"url", "variables", "connection"})
@NoArgsConstructor
public class JsltTransformService extends ServiceImp implements ConnectedService {

  private static final String UTF_8 = StandardCharsets.UTF_8.name();
  /**
   * Set the URL that contains the transformation you wish to execute.
   * <p>
   * Although named as a URL it can resolve the JSLT file from either the classpath or filesystem
   * depending on the value here.
   * <ul>
   * <li><strong>/Users/interlok/work/runtime/my.jslt</strong> - either file or classpath</li>
   * <li><strong>my.jslt - either file or classpath</strong></li>
   * <li><strong>file:////Users/interlok/path/to/my.jslt</strong> - considered an absolute local
   * file URL, but classpath will be checked as a last resort for
   * /Users/interlok/path/to/my.jslt</li>
   * <li><strong>file:///C:/Users/interlok/path/to/my.jslt</strong> - explicit absolute URL (windows
   * style)</li>
   * <li><strong>https://myserver.com/path/to/my.jslt</strong> - explicit absolute URL supported by
   * the JVM</li>
   * </ul>
   * </p>
   * <p>
   * This also supports the {@code %message{}} syntax to resolve metadata.
   * </p>
   *
   * @see ResourceLocator
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldHint(expression = true)
  private String url;

  /**
   * Any additional variables you want to pass into the JSLT transformation.
   *
   * <p>
   * Check <a href=
   * "https://github.com/schibsted/jslt/blob/master/docs/api.md#passing-values-to-jslt">passing
   * values to jslt</a> for what this means. Conceptually it is very similar to
   * {@code XmlTransformParameters} if you have dynamic values that you need to provide to your JSLT
   * mapping.
   * </p>
   */
  @Getter
  @Setter
  @Valid
  @NotNull
  @NonNull
  private JsltVariables variables = new JsltVariables();

  /**
   * The connection that wraps any underlying caching required.
   * <p>
   * When using the cache note that the JSLT Expression is not serializable so distributed caches
   * may not work as you expect; in this instance the cache key will be the URL as resolved during
   * first execution.
   * </p>
   * <p>
   * This defaults to a normal {@link CacheConnection} with a {@link NullCacheImplementation} if not
   * explicit configured.
   * </p>
   */
  @Getter
  @Setter
  @Valid
  private AdaptrisConnection connection;


  private transient AdaptrisConnection cacheConnection;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      ObjectMapper mapper = JsltQuery.DEFAULT_OBJECT_MAPPER;
      String urlToUse = msg.resolve(getUrl());
      JsonNode input = JsltQuery.jacksonify(mapper, msg);
      try (Writer msgOut = msg.getWriter(UTF_8);
          JsonGenerator generator = mapper.getFactory().createGenerator(msgOut)) {
        Expression transformer = getTransformer(urlToUse);
        JsonNode output = transformer.apply(getVariables().build(msg), input);
        generator.writeTree(output);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Expression getTransformer(String url) throws Exception {
    Cache cache = cacheConnection.retrieveConnection(CacheConnection.class).retrieveCache();
    Expression transformer = (Expression) cache.get(url);
    if (transformer == null) {
      transformer = downloadAndCompile(url);
      cache.put(url, transformer);
    }
    return transformer;
  }


  private Expression downloadAndCompile(String urlToUse) throws Exception {
    URL _url = ResourceLocator.toURL(urlToUse);
    Expression transformer = null;
    try (InputStreamReader jsltReader = new InputStreamReader(_url.openStream(), UTF_8)) {
      transformer = Parser.compile(urlToUse, jsltReader, Collections.EMPTY_LIST);
    }
    return transformer;
  }

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getUrl(), "transform-url");
    cacheConnection = defaultIfNull();
    LifecycleHelper.prepare(cacheConnection);
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(cacheConnection);
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(cacheConnection);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(cacheConnection);
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(cacheConnection);
  }

  public JsltTransformService withUrl(String url) {
    setUrl(url);
    return this;
  }

  public JsltTransformService withConnection(AdaptrisConnection c) {
    setConnection(c);
    return this;
  }

  public JsltTransformService withVariables(JsltVariables v) {
    setVariables(v);
    return this;
  }


  private AdaptrisConnection defaultIfNull() {
    return ObjectUtils.defaultIfNull(getConnection(),
        new CacheConnection().withCacheInstance(new NullCacheImplementation()));
  }
}
