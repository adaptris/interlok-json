package com.adaptris.core.transform.json;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default Transformation Driver that uses both {@link JSONObject#fromObject(Object)} and
 * {@link JSONArray#fromObject(Object)} to parse the input.
 * 
 * @config default-transformation-driver
 * @author gdries
 */
@XStreamAlias("default-transformation-driver")
public class DefaultJsonTransformationDriver extends JsonObjectTransformationDriver {
  @Override
  protected JSON parse(String input) throws JSONException {
    JSON result = null;
    try {
      result = parseObject(input);
    } catch (JSONException e) {
      result = parseArray(input);
    }
    return result;
  }

  private JSONObject parseObject(String input) throws JSONException {
    return JSONObject.fromObject(input);
  }

  private JSONArray parseArray(String input) throws JSONException {
    return JSONArray.fromObject(input);
  }
}
