package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Transformation driver that uses instead of {@link JSONObject#fromObject(Object)} to read the JSON payload.
 *
 * @since 3.0.4
 * @see JsonlibTransformationDriver
 * @config json-object-transformation-driver
 */
@XStreamAlias("json-object-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver, supports a top level JSON Object", since = "3.0.4",
    tag = "json,xml,transformation")
@DisplayOrder(order= {"rootName", "arrayName", "elementName", "objectName"})
public class JsonObjectTransformationDriver extends JsonlibTransformationDriver {

  /**
   * Parse a JSON object from a string.
   *
   * @param input The JSON string.
   *
   * @return The JSON object.
   *
   * @throws JSONException Thrown if the string isn't a valid JSON object.
   */
  @Override
  protected JSON parse(final String input) throws JSONException {
    return parseObject(input);
  }
}
