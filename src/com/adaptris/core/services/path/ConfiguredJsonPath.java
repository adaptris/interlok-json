package com.adaptris.core.services.path;

import com.adaptris.core.AdaptrisMessage;

public interface ConfiguredJsonPath {
  
  public String getConfiguredJsonPath(AdaptrisMessage message);

}
