package com.adaptris.core.transform.json;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Transformation driver that uses {@link JSONArray#fromObject(Object)} instead of {@link JSONObject#fromObject(Object)}.
 *
 * @see DefaultJsonTransformationDriver
 * @since 3.0.4
 * @config json-array-transformation-driver
 */
@XStreamAlias("json-array-transformation-driver")
public class JsonArrayTransformationDriver extends DefaultJsonTransformationDriver {

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
