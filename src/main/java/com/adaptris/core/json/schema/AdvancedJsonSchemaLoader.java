package com.adaptris.core.json.schema;

import org.apache.commons.lang3.BooleanUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("advanced-json-schema-loader")
public class AdvancedJsonSchemaLoader implements JsonSchemaLoader {


  @InputFieldDefault(value = "false")
  private Boolean classPathAwareClient;

  @Override
  public Schema loadSchema(JSONObject rawSchema, AdaptrisMessage input) throws JSONException {
    SchemaLoader.SchemaLoaderBuilder builder = SchemaLoader.builder()
        .schemaJson(rawSchema);
    if(classPathAwareClient()) {
      builder.schemaClient(SchemaClient.classPathAwareClient());
    }
    SchemaLoader schemaLoader = builder.build();
    return schemaLoader.load().build();
  }

  public void setClassPathAwareClient(Boolean classPathAwareClient) {
    this.classPathAwareClient = classPathAwareClient;
  }

  public Boolean getClassPathAwareClient() {
    return classPathAwareClient;
  }

  private boolean classPathAwareClient() {
    return BooleanUtils.toBooleanDefaultIfNull(getClassPathAwareClient(), false);
  }

  AdvancedJsonSchemaLoader withClassPathAwareClient(Boolean classPathAwareClient) {
    setClassPathAwareClient(classPathAwareClient);
    return this;
  }
}
