package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Configure this class when using {@link JsonPathService} to define where the JSON content can be found or the resulting data should be set.
 * </p>
 * <p>
 * This implementation of {@link Destination} if used as the source destination will grab the JSON content from the {@link AdaptrisMessage}s
 * metadata list.  Simply supply the metadata key where your JSON content can be found.  If you use this implementation as the target destination
 * then the result from the json-path-service will be set back into that metadata key.
 * </p>
 * <p>
 * Finally you can configure where the json-path expression can be found.  Currently there are 2 options;
 * <ul>
 *   <li>{@link ConstantJsonPath}</li>
 *   <li>{@link MetadataJsonPath}</li>
 * </ul>
 * </p>
 * 
 * @author amcgrath
 * @config json-metadata-destination
 * @license BASIC
 */

@XStreamAlias("json-metadata-destination")
public class MetadataDestination extends AbstractDestination {
  
  private static final String DEFAULT_METADATA_KEY = "metadata-key";
  
  private String key;
  
  public MetadataDestination() {
    this.setKey(DEFAULT_METADATA_KEY);
  }

  @Override
  public String getContent(AdaptrisMessage message) {
    return message.getMetadataValue(this.getKey());
  }

  @Override
  public void setContent(AdaptrisMessage message, String content) {
    message.addMetadata(this.getKey(), content);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

}
