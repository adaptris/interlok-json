package com.adaptris.core.json.schema;

import org.everit.json.schema.Schema;
import org.json.JSONObject;

/**
 * Interface which allows implementations to control how the JSON {@linkplain Schema} is loaded.
 */
public interface JsonSchemaLoader {

  /**
   * Provide a JSON {@linkplain Schema} based on the {@linkplain JSONObject} parameter.
   *
   * @param rawSchema {@linkplain JSONObject} to load as {@linkplain Schema}.
   * @return {@linkplain Schema} based on {@linkplain JSONObject}.
   */
  Schema loadSchema(JSONObject rawSchema);
}
