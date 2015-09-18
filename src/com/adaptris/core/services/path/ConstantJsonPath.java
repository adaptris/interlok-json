package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A constant configured Json path.
 * </p>
 * <p>
 * Allows you to configure a static value for the Json path in your Interlok configuration.
 * </p>
 * @author amcgrath
 * @config json-metadata-destination
 * @license BASIC
 */
@XStreamAlias("constant-json-path")
public class ConstantJsonPath implements ConfiguredJsonPath {
  
  private String jsonPath;
  
  public ConstantJsonPath() {
    this.setJsonPath("");
  }
  
  public ConstantJsonPath(String jsonPath) {
    this.setJsonPath(jsonPath);
  }

  public String getJsonPath() {
    return jsonPath;
  }

  public void setJsonPath(String jsonPath) {
    this.jsonPath = jsonPath;
  }

  @Override
  public String getConfiguredJsonPath(AdaptrisMessage message) {
    return this.getJsonPath();
  }

}
