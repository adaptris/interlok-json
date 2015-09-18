package com.adaptris.core.services.path;

import java.util.EnumSet;
import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public abstract class AbstractDestination implements Destination {
    
  private ConfiguredJsonPath configuredJsonPath;
  
  static {
    Configuration.setDefaults(new Configuration.Defaults() {
        private final JsonProvider jsonProvider = new JacksonJsonProvider();
        private final MappingProvider mappingProvider = new JacksonMappingProvider();
        private final Set<Option> options = EnumSet.noneOf(Option.class);

        public JsonProvider jsonProvider() {
            return jsonProvider;
        }

        @Override
        public MappingProvider mappingProvider() {
            return mappingProvider;
        }

        @Override
        public Set<Option> options() {
            return options;
        }
    });
  }
  
  @Override
  public void execute(AdaptrisMessage message, String jsonContent) {
    this.setContent(message, 
        JsonPath.parse(jsonContent).read(
            this.getConfiguredJsonPath().getConfiguredJsonPath(message)).toString());
  }

  public ConfiguredJsonPath getConfiguredJsonPath() {
    return configuredJsonPath;
  }

  public void setConfiguredJsonPath(ConfiguredJsonPath configuredJsonPath) {
    this.configuredJsonPath = configuredJsonPath;
  }

}
