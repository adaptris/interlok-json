package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Configure this class when using {@link JsonPathService} to define where the JSON content can be found or the resulting data should be set.
 * </p>
 * <p>
 * This implementation of {@link Destination} if used as the source destination will grab the JSON content from the {@link AdaptrisMessage}s
 * payload.  If you use this implementation as the target destination
 * then the result from the json-path-service will be set back into the {@link AdaptrisMessage} payload.
 * </p>
 * 
 * @author amcgrath
 * @config json-metadata-destination
 * @license BASIC
 */

@XStreamAlias("json-payload-destination")
public class PayloadDestination extends AbstractDestination {

  @Override
  public String getContent(AdaptrisMessage message) {
    return message.getStringPayload();
  }

  @Override
  public void setContent(AdaptrisMessage message, String content) {
    message.setStringPayload(content, message.getCharEncoding());
  }

}
