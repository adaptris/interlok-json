package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;

public interface Destination {
  
  public String getContent(AdaptrisMessage message);
  
  public void setContent(AdaptrisMessage message, String content);
  
  public void execute(AdaptrisMessage message, String jsonContent);

}
