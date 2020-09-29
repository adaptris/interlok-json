/*
 * Copyright 2017 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.core.json.jq;

import static com.adaptris.core.MetadataCollection.asMap;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;

/**
 * Transform a JSON document using JQ style syntax.
 * 
 * 
 * @config json-jq-transform
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Execute a JQ style query", tag = "service,json,transform,jq")
@XStreamAlias("json-jq-transform")
public class JsonJqTransform extends ServiceImp {

  private static final String SCOPE_NAME = "metadata";

  @NotNull
  @Valid
  private DataInputParameter<String> querySource;

  @Valid
  @AdvancedConfig
  private MetadataFilter metadataFilter;

  public JsonJqTransform() {
    super();
    // Explicitly here for XStream; because who knows, the compiler may one
    // day "decide to change" the default behaviour.
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    ObjectMapper mapper = new ObjectMapper();
    try (Reader reader = msg.getReader();
        Writer w = msg.getWriter();
        JsonGenerator generator = mapper.getFactory().createGenerator(w).useDefaultPrettyPrinter()) {
      JsonQuery q = JsonQuery.compile(querySource.extract(msg));
      JsonNode jsonNode = mapper.readTree(reader);
      List<JsonNode> result = q.apply(createScope(mapper, msg), jsonNode);
      if (result.size() == 1) {
        generator.writeObject(result.get(0));
      }
      else {
        generator.writeObject(result);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    // Nothing to do
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getQuerySource(), "querySource");
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {
    // Nothing to do
  }

  public DataInputParameter<String> getQuerySource() {
    return querySource;
  }

  /**
   * 
   * @param query the query-source.
   */
  public void setQuerySource(DataInputParameter<String> query) {
    this.querySource = Args.notNull(query, "querySource");
  }

  public JsonJqTransform withQuerySource(DataInputParameter<String> query) {
    setQuerySource(query);
    return this;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Filter metadata to pass through to your query.
   * <p>
   * The metadata will be passed in as a {@code Scope} object bound to {@code metadata} which allows you to reference it as
   * {@code $metadata.myMetadataKey} within your query
   * </p>
   * 
   * @param f the filter, default is {@code remove-all-metadata-filter} if not specified.
   */
  public void setMetadataFilter(MetadataFilter f) {
    this.metadataFilter = f;
  }

  public JsonJqTransform withMetadataFilter(MetadataFilter f) {
    setMetadataFilter(f);
    return this;
  }

  private MetadataFilter metadataFilter() {
    return getMetadataFilter() != null ? getMetadataFilter() : new RemoveAllMetadataFilter();
  }

  private Scope createScope(ObjectMapper mapper, AdaptrisMessage msg) {
    Map<String, String> filtered = asMap(metadataFilter().filter(msg));
    Scope scope = new Scope(null);
    scope.loadFunctions(Thread.currentThread().getContextClassLoader());
    scope.setValue(SCOPE_NAME, mapper.valueToTree(filtered));
    return scope;
  }
}
