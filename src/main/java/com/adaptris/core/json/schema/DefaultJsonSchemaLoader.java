package com.adaptris.core.json.schema;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;

@XStreamAlias("default-json-schema-loader")
public class DefaultJsonSchemaLoader implements JsonSchemaLoader {

  @Override
  public Schema loadSchema(JSONObject rawSchema, AdaptrisMessage input) throws JSONException {
    return SchemaLoader.load(rawSchema);
  }
}
