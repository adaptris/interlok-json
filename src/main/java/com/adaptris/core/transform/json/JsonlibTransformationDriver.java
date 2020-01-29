package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Transformation Driver that uses both {@link JSONObject#fromObject(Object)} and {@link JSONArray#fromObject(Object)} to parse the
 * input.
 * <p>
 * <strong>In some processing scenarios, this driver is inherently CPU bound because of how it processes XML elements<strong>. In
 * most use-cases you can use {@link SimpleJsonTransformationDriver} instead. You can also opt to use the
 * {@code com.adaptris:interlok-json-streaming} package instead to transform to and from XML
 * </p>
 * <p>
 * This uses the {@code net.sf.json} package, which in some situations, can be very sensitive to whitespace, and output may not be
 * as you expect. Accordingly, when you are generating the XML to be rendered as JSON make sure that you use
 * {@code xsl:strip-space elements="*"}.
 * </p>
 * 
 * @config jsonlib-transformation-driver
 */
@XStreamAlias("jsonlib-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver based on net.sf:json-lib, supports top level JSON arrays",
    tag = "json,xml,transformation", since = "3.10.0")
@DisplayOrder(order = {"rootName", "arrayName", "elementName", "objectName"})
public class JsonlibTransformationDriver extends JsonTransformationDriverImpl {

  @Override
  protected JSON parse(final String input) throws JSONException {
    JSON result = null;
    try {
      result = parseObject(input);
    } catch (final JSONException e) {
      result = parseArray(input);
    }
    return result;
  }

  protected static JSONObject parseObject(final String input) throws JSONException {
    return JSONObject.fromObject(input);
  }

  protected static JSONArray parseArray(final String input) throws JSONException {
    return JSONArray.fromObject(input);
  }
}
