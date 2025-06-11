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
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Function;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.adaptris.core.MetadataCollection.asMap;

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
  private DataInputParameter<String> queryTarget;

  @Valid
  @AdvancedConfig
  private DataOutputParameter<String> outputTarget;

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
    final List<JsonNode> result = new ArrayList<>();
    try {
      try (Reader reader = buildReader(msg)) {
        JsonQuery q = JsonQuery.compile(querySource.extract(msg), Version.LATEST);
        JsonNode jsonNode = mapper.readTree(reader);

        q.apply(createScope(mapper, msg), jsonNode, out -> result.add(out));
      }
      Object _result = result.size() == 1 ? result.get(0) : result;
      writeResult(msg, _result, mapper);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Reader buildReader(AdaptrisMessage msg) throws InterlokException, IOException {
    return queryTarget != null ? new StringReader(queryTarget.extract(msg)) : msg.getReader();
  }

  private void writeResult(AdaptrisMessage msg, Object result, ObjectMapper mapper) throws InterlokException, IOException {
    try (Writer w = outputTarget != null ? new StringWriter() : msg.getWriter();
      JsonGenerator generator = mapper.getFactory().createGenerator(w).useDefaultPrettyPrinter()) {
      generator.writeObject(result);
      if (outputTarget != null) {
        outputTarget.insert(w.toString(), msg);
      }
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


  public DataInputParameter<String> getQueryTarget() {
    return queryTarget;
  }

  public void setQueryTarget(DataInputParameter<String> queryTarget) {
    this.queryTarget = queryTarget;
  }

  public DataOutputParameter<String> getOutputTarget() {
    return outputTarget;
  }

  public void setOutputTarget(DataOutputParameter<String> outputTarget) {
    this.outputTarget = outputTarget;
  }

  public JsonJqTransform withQuerySource(DataInputParameter<String> query) {
    setQuerySource(query);
    return this;
  }

  public JsonJqTransform withQueryTarget(DataInputParameter<String> target) {
    setQueryTarget(target);
    return this;
  }

  public JsonJqTransform withOutputTarget(DataOutputParameter<String> outputTarget) {
    setOutputTarget(outputTarget);
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
    Scope scope = Scope.newEmptyScope();
    BuiltinFunctionLoader functionLoader = BuiltinFunctionLoader.getInstance();
    Map<String, Function> functions = functionLoader.listFunctions(Thread.currentThread().getContextClassLoader(), Version.LATEST, scope);
    for (String name : functions.keySet()) {
      scope.addFunction(name, functions.get(name));
    }
    scope.setValue(SCOPE_NAME, mapper.valueToTree(filtered));
    return scope;
  }
}
