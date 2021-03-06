package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Transformation driver that uses {@link JSONArray#fromObject(Object)} instead of {@link JSONObject#fromObject(Object)}.
 *
 * @see JsonlibTransformationDriver
 * @since 3.0.4
 * @config json-array-transformation-driver
 */
@XStreamAlias("json-array-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver, supports top level JSON arrays", since = "3.0.4",
    tag = "json,xml,transformation")
@DisplayOrder(order= {"rootName", "arrayName", "elementName", "objectName"})
public class JsonArrayTransformationDriver extends JsonlibTransformationDriver {

	/**
	 * Parse a JSON array from a string.
	 *
	 * @param input
	 *          The JSON string.
	 *
	 * @return The JSON array.
	 *
	 * @throws JSONException
	 *           Thrown if the string isn't a valid JSON array.
	 */
	@Override
	protected JSONArray parse(final String input) throws JSONException {
		return parseArray(input);
	}
}
