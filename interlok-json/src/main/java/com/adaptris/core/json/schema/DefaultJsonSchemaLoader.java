package com.adaptris.core.json.schema;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

/**
 * {@link JsonSchemaLoader} implementation uses the default loader settings.
 *
 * @author mwarman
 *
 */
@XStreamAlias("default-json-schema-loader")
public class DefaultJsonSchemaLoader implements JsonSchemaLoader {

  @Override
  public Schema loadSchema(JSONObject rawSchema) {
    return SchemaLoader.load(rawSchema);
  }
}
