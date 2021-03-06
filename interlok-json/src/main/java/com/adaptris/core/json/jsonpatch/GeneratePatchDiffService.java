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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.types.MessageWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Generate a JSON patch diff between two documents
 * 
 * <p>
 * JSON Patch defines a JSON document structure for expressing a sequence of operations to apply to
 * a JavaScript Object Notation (JSON) document. This service generates the JSON that expresses the
 * differences between 2 JSON documents as a sequence of operations.
 * </p>
 * <p>
 * For a given {@code diff-source} of {@code {"a": 0,"b": [1,2]}} and a {@code diff-target} of
 * {@code {"b": [1,2,0]}} then the patch transformation generated and stored against {@code output}
 * should be {@code [{"op":"move","from":"/a","path":"/b/2"}]}.
 * </p>
 * <p>
 * Note that <a href="https://github.com/flipkart-incubator/zjsonpatch">zjsonpatch</a> operates on
 * JsonNode objects; this means keeping the both node trees in memory, so behaviour will be
 * size/memory dependent.
 * </p>
 * 
 * @config json-patch-generate-diff
 * 
 */
@XStreamAlias("json-patch-generate-diff")
@AdapterComponent
@ComponentProfile(summary = "Generate a JSON patch diff between two documents",
    tag = "service,json,jsonpatch", since = "3.9.0")
@DisplayOrder(order = {"diffSource", "diffTarget", "output", "flags"})
public class GeneratePatchDiffService extends JsonPatchService {

  @NotNull(message = "diff-source may not be null")
  @Valid
  private MessageWrapper<InputStream> diffSource;
  @NotNull(message = "diff-target may not be null")
  @Valid
  private MessageWrapper<InputStream> diffTarget;
  @AdvancedConfig
  @AutoPopulated
  @NotNull
  @InputFieldDefault(value = "as per com.flipkart.zjsonpatch.DiffFlags.defaults()")
  private List<PatchDiffFlag> flags;


  public GeneratePatchDiffService() {
    setFlags(new ArrayList<PatchDiffFlag>());
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      JsonNode source = readAndClose(getDiffSource().wrap(msg));
      JsonNode target = readAndClose(getDiffTarget().wrap(msg));
      JsonNode patch = JsonDiff.asJson(source, target, flags());
      writeAndClose(patch, output().wrap(msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  @Override
  public void prepare() throws CoreException {
    Args.notNull(getDiffSource(), "diffSource");
    Args.notNull(getDiffTarget(), "diffTarget");
    Args.notNull(getFlags(), "flags");
  }


  public MessageWrapper<InputStream> getDiffSource() {
    return diffSource;
  }


  /**
   * Specify the JSON document that will be transformed.
   * 
   * @param diffSource the source
   */
  public void setDiffSource(MessageWrapper<InputStream> diffSource) {
    this.diffSource = Args.notNull(diffSource, "diffSource");
  }

  public GeneratePatchDiffService withDiffSource(MessageWrapper<InputStream> diffSource) {
    setDiffSource(diffSource);
    return this;
  }

  public MessageWrapper<InputStream> getDiffTarget() {
    return diffTarget;
  }


  /**
   * Specify the target JSON document you want the patch to transform to.
   * 
   * @param diffTarget the target JSON document
   */
  public void setDiffTarget(MessageWrapper<InputStream> diffTarget) {
    this.diffTarget = Args.notNull(diffTarget, "diffTarget");
  }

  public GeneratePatchDiffService withDiffTarget(MessageWrapper<InputStream> diffTarget) {
    setDiffTarget(diffTarget);
    return this;
  }

  public List<PatchDiffFlag> getFlags() {
    return flags;
  }


  /**
   * Specify any additional flags for the diff operation.
   * 
   * @param flags the flags.
   */
  public void setFlags(List<PatchDiffFlag> flags) {
    this.flags = Args.notNull(flags, "flags");
  }

  public GeneratePatchDiffService withFlags(List<PatchDiffFlag> flags) {
    setFlags(flags);
    return this;
  }

  public GeneratePatchDiffService withFlags(PatchDiffFlag... flags) {
    return withFlags(new ArrayList(Arrays.asList(flags)));
  }

  EnumSet<DiffFlags> flags() {
    if (getFlags().size() == 0) {
      return DiffFlags.defaults().clone();
    }
    EnumSet<DiffFlags> diffFlags = EnumSet.noneOf(DiffFlags.class);
    getFlags().forEach((e) -> { diffFlags.add(e.actualValue());});
    return diffFlags;
  }
}
