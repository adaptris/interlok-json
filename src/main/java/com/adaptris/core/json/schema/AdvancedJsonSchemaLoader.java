package com.adaptris.core.json.schema;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;

@XStreamAlias("advanced-json-schema-loader")
public class AdvancedJsonSchemaLoader implements JsonSchemaLoader {


  @NotNull
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
    this.classPathAwareClient = Args.notNull(classPathAwareClient, "classPathAwareClient");
  }

  public Boolean getClassPathAwareClient() {
    return classPathAwareClient;
  }

  private boolean classPathAwareClient() {
    return getClassPathAwareClient() != null ? getClassPathAwareClient() : false;
  }

  AdvancedJsonSchemaLoader withClassPathAwareClient(Boolean classPathAwareClient) {
    setClassPathAwareClient(classPathAwareClient);
    return this;
  }
}
