package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This class allows you to configure a metadata key, which will be used to retrieve the Json path expression. 
 * </p>
 * @author amcgrath
 * @config json-metadata-destination
 * @license BASIC
 */

@XStreamAlias("metadata-json-path")
public class MetadataJsonPath implements ConfiguredJsonPath {
  
  private static final String DEFAULT_METADATA_KEY = "jsonPathMetadata";

  private String metadataKey;
  
  public MetadataJsonPath() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  public MetadataJsonPath(String metadataKey) {
    this.setMetadataKey(metadataKey);
  }

  @Override
  public String getConfiguredJsonPath(AdaptrisMessage message) {
    return message.getMetadataValue(this.getMetadataKey());
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
