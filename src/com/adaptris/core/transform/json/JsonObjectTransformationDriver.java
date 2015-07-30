package com.adaptris.core.transform.json;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Transformation driver that uses instead of {@link JSONObject#fromObject(Object)} to read the JSON
 * payload.
 * 
 * @since 3.0.4
 */
@XStreamAlias("json-object-transformation-driver")
public class JsonObjectTransformationDriver extends JsonTransformationDriverImpl {

  @Override
  protected JSON parse(String input) throws JSONException {
    return JSONObject.fromObject(input);
  }
}
