package com.adaptris.core.transform.json;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Transformation driver that uses {@link JSONArray#fromObject(Object)} instead of
 * {@link JSONObject#fromObject(Object)}.
 * 
 * @since 3.0.4
 * @config json-array-transformation-driver
 */
@XStreamAlias("json-array-transformation-driver")
public class JsonArrayTransformationDriver extends JsonTransformationDriverImpl {


  public JsonArrayTransformationDriver() {}

  @Override
  protected JSONArray parse(String input) throws JSONException {
    return JSONArray.fromObject(input);
  }

}


