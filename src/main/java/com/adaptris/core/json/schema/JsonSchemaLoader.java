package com.adaptris.core.json.schema;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import org.everit.json.schema.Schema;
import org.json.JSONException;
import org.json.JSONObject;

public interface JsonSchemaLoader {

  Schema loadSchema(JSONObject rawSchema, AdaptrisMessage input) throws JSONException;
}
