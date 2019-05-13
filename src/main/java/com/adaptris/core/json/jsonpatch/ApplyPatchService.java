/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.json.jsonpatch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.types.InterlokMessage.MessageWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.flipkart.zjsonpatch.JsonPatch;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Apply a JSON patch diff.
 * 
 * <p>
 * JSON Patch defines a JSON document structure for expressing a sequence of operations to apply to
 * a JavaScript Object Notation (JSON) document.
 * </p>
 * <p>
 * For a given {@code source} of {@code {"a": 0,"b": [1,2]}} and a {@code patch-source} of
 * {@code [{"op":"move","from":"/a","path":"/b/2"}]} then when executing this service you would
 * expect to get {@code {"b": [1,2,0]}} to be stored against the configured {@code output}
 * parameter.
 * </p>
 * <p>
 * Note that <a href="https://github.com/flipkart-incubator/zjsonpatch">zjsonpatch</a> operates on
 * JsonNode objects; this means keeping the node trees in memory, so behaviour will be size/memory
 * dependent.
 * </p>
 * 
 * @config json-patch-apply
 * 
 */
@XStreamAlias("json-patch-apply")
@AdapterComponent
@ComponentProfile(summary = "Apply a JSON patch diff to a document",
    tag = "service,json,jsonpatch", since = "3.9.0")
@DisplayOrder(order = {"source", "patchSource", "output", "flags"})
public class ApplyPatchService extends JsonPatchService {
  @NotNull(message = "patch-source may not be null")
  @Valid
  private MessageWrapper<InputStream> patchSource;
  @InputFieldDefault(value = "PayloadStreamInputParameter")
  @Valid
  private MessageWrapper<InputStream> source;
  @AdvancedConfig
  @AutoPopulated
  @InputFieldDefault(value = "as per com.flipkart.zjsonpatch.CompatibilityFlags.defaults()")
  @InputFieldHint(style = "com.flipkart.zjsonpatch.CompatibilityFlags")
  private List<CompatibilityFlags> flags;

  public ApplyPatchService() {

  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      JsonNode documentSource = readAndClose(sourceDocument().wrap(msg));
      JsonNode patchDocument = readAndClose(getPatchSource().wrap(msg));
      JsonNode patchOutput = JsonPatch.apply(patchDocument, documentSource, flags());
      writeAndClose(patchOutput, output().wrap(msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getPatchSource(), "patchSource");
  }


  public MessageWrapper<InputStream> getPatchSource() {
    return patchSource;
  }

  /**
   * Set the source of the JSON patch transformation.
   * 
   * @param patchSource the patch source.
   */
  public void setPatchSource(MessageWrapper<InputStream> patchSource) {
    this.patchSource = patchSource;
  }

  public MessageWrapper<InputStream> getSource() {
    return source;
  }

  /**
   * Set the source document that will have the JSON Patch applied to it.
   * 
   * @param source the source; default is {@link PayloadStreamInputParameter}.
   */
  public void setSource(MessageWrapper<InputStream> source) {
    this.source = source;
  }

  public ApplyPatchService withPatchSource(MessageWrapper<InputStream> source) {
    setPatchSource(source);
    return this;
  }

  public ApplyPatchService withSource(MessageWrapper<InputStream> source) {
    setSource(source);
    return this;
  }

  MessageWrapper<InputStream> sourceDocument() {
    return ObjectUtils.defaultIfNull(getSource(), new PayloadStreamInputParameter());
  }

  public List<CompatibilityFlags> getFlags() {
    return flags;
  }

  /**
   * Specify any additional flags for the patch operation.
   * 
   * @param flags the flags.
   */
  public void setFlags(List<CompatibilityFlags> flags) {
    this.flags = flags;
  }

  public ApplyPatchService withFlags(List<CompatibilityFlags> flags) {
    setFlags(flags);
    return this;
  }

  public ApplyPatchService withFlags(CompatibilityFlags... flags) {
    return withFlags(new ArrayList(Arrays.asList(flags)));
  }

  EnumSet<CompatibilityFlags> flags() {
    if (getFlags() == null) {
      return CompatibilityFlags.defaults().clone();
    }
    EnumSet<CompatibilityFlags> patchFlags = EnumSet.noneOf(CompatibilityFlags.class);
    patchFlags.addAll(this.flags);
    return patchFlags;
  }
}
