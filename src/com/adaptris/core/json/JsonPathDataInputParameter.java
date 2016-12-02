/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.json;

import java.util.EnumSet;
import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataInputParameter} is extracts data via JsonPath.
 * 
 * <p>
 * If the path resolves to a JSON object or JSON Array, then this is simply converted into a String.
 * </p>
 * 
 * @config json-path-data-input-parameter
 * 
 */
@XStreamAlias("json-path-data-input-parameter")
public class JsonPathDataInputParameter implements DataInputParameter<String> {

  private transient Configuration jsonConfig;

  @NotBlank
  private String path;

  public JsonPathDataInputParameter() {
  }

  public JsonPathDataInputParameter(String v) {
    this();
    setPath(v);
  }

  @Override
  public String extract(InterlokMessage m) throws InterlokException {
    String result = null;
    try {
      if (jsonConfig == null) {
        jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
      }
      ReadContext context = JsonPath.parse(m.getInputStream(), jsonConfig);
      Object o = context.read(getPath());
      System.err.println(o.getClass());
      if (Map.class.isAssignableFrom(o.getClass())) {
        result = new ObjectMapper().writeValueAsString((Map) o);
      }
      else {
        result = o.toString();
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    return result;
  }

  public String getPath() {
    return path;
  }

  /**
   * The path to resolve.
   * 
   * @param v the path.
   */
  public void setPath(String v) {
    this.path = Args.notBlank(v, "path");
  }

}
