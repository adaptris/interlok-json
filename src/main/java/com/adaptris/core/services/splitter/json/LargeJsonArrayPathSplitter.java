package com.adaptris.core.services.splitter.json;

import java.io.BufferedReader;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.CloseableIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Split an arbitrarily large JSON array.
 *
 * <p>
 * This allows you to split via simple element traversal, so the path cannot be assumed to be an XPath.
 * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
 * {@link JsonParser} and navigates based on {@link JsonToken#START_OBJECT} events only.
 * </p>
 *
 * <p>
 * Note: tested with an 85Mb file containing an array of >15k JSON objects
 * </p>
 *
 * @config large-json-array-path-splitter
 */
@XStreamAlias("large-json-array-path-splitter")
@DisplayOrder(order = {"path", "encoding", "bufferSize"})
@ComponentProfile(summary = "Split an arbitrarily large JSON array, with use of simple element traversal.", since = "3.8.4",
    tag = "json,splitting")
public class LargeJsonArrayPathSplitter extends LargeJsonArraySplitter {

  @NotBlank
  @InputFieldHint(expression = true)
  private String path;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean suppressPathNotFound;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean suppressPathNotAnArray;


  public LargeJsonArrayPathSplitter() {

  }

  public String getPath() {
    return path;
  }

  /**
   * Set the xpath-alike path to the element on which you want to split.
   * <p>
   * Note that this is only a pseudo-xpath evaluator as it only allows simple element traversal and not any XPath functions.
   * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
   * {@link JsonParser} and navigates based on {@link JsonToken#START_OBJECT} events only.
   * </p>
   *
   * @param path the path.
   */
  public void setPath(String path) {
    this.path = Args.notBlank(path, "path");
  }

  public Boolean getSuppressPathNotFound() {
    return suppressPathNotFound;
  }

  public void setSuppressPathNotFound(Boolean suppressPathNotFound) {
    this.suppressPathNotFound = suppressPathNotFound;
  }

  boolean suppressPathNotFound(){
    return BooleanUtils.toBooleanDefaultIfNull(getSuppressPathNotFound(), false);
  }

  public Boolean getSuppressPathNotAnArray() {
    return suppressPathNotAnArray;
  }

  public void setSuppressPathNotAnArray(Boolean suppressPathNotAnArray) {
    this.suppressPathNotAnArray = suppressPathNotAnArray;
  }

  boolean suppressPathNotAnArray(){
    return BooleanUtils.toBooleanDefaultIfNull(getSuppressPathNotAnArray(), false);
  }


  public LargeJsonArrayPathSplitter withPath(String path){
    setPath(path);
    return this;
  }

  public LargeJsonArrayPathSplitter withSuppressPathNotFound(boolean suppressPathNotFound){
    setSuppressPathNotFound(suppressPathNotFound);
    return this;
  }

  public LargeJsonArrayPathSplitter withSuppressPathNotAnArray(boolean suppressPathNotAnArray){
    setSuppressPathNotAnArray(suppressPathNotAnArray);
    return this;
  }


  @Override
  public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
    try {
      String thePath = msg.resolve(getPath());
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      return new PathJsonSplitGenerator(
          new GeneratorConfig().withJsonParser(parser).withObjectMapper(mapper).withOriginalMessage(msg).withReader(buf),
          new PathGeneratorConfig().withPath(thePath).withSuppressPathNotFound(suppressPathNotFound()).withSuppressPathNotAnArray(suppressPathNotAnArray()));
    } catch (Exception e) {
      throw new CoreException(e);
    }
  }

  protected class PathGeneratorConfig {
    String path;
    private boolean suppressPathNotFound;
    private boolean suppressPathNotAnArray;

    PathGeneratorConfig withPath(String path) {
      this.path = path;
      return this;
    }

    PathGeneratorConfig withSuppressPathNotFound(boolean suppressPathNotFound) {
      this.suppressPathNotFound = suppressPathNotFound;
      return this;
    }

    PathGeneratorConfig withSuppressPathNotAnArray(boolean suppressPathNotAnArray) {
      this.suppressPathNotAnArray = suppressPathNotAnArray;
      return this;
    }
  }

  private class PathJsonSplitGenerator extends JsonSplitGenerator {

    PathJsonSplitGenerator(GeneratorConfig cfg, PathGeneratorConfig pathGeneratorConfig) throws Exception {
      super(cfg);
      String thePath = pathGeneratorConfig.path;
      if (thePath.startsWith("/")) {
        thePath = thePath.substring(1);
      }
      String[] elements = thePath.split("/");
      boolean found = false;
      for (String s : elements) {
        found = nextMatching(s);
      }
      if (found) {
        if(parser.nextToken() != JsonToken.START_ARRAY){
          if (!pathGeneratorConfig.suppressPathNotAnArray) {
            throw new CoreException("Path result should be an array.");
          }
          parser.close();
        }
      } else {
        if (!pathGeneratorConfig.suppressPathNotFound) {
          throw new CoreException("Could not traverse to " + pathGeneratorConfig.path);
        }
        parser.close();
      }
    }

    @Override
    public boolean hasNext() {
      if(parser.isClosed()){
        return false;
      }
      return super.hasNext();
    }

    private boolean nextMatching(String elementName) throws Exception {
      while (parser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = parser.getCurrentName();
        if(elementName.equals(fieldName)){
          return true;
        }
      }
      return false;
    }

  }


}
