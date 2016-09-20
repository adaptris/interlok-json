package com.adaptris.core.transform.json;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Default Transformation Driver that uses both {@link JSONObject#fromObject(Object)} and
 * {@link JSONArray#fromObject(Object)} to parse the input.
 *
 * @config default-transformation-driver
 * @author gdries
 */
@XStreamAlias("default-transformation-driver")
public class DefaultJsonTransformationDriver extends JsonTransformationDriverImpl {

	/**
	 * Parse the JSON string.
	 *
	 * @param input
	 *          The JSON string.
	 *
	 * @return The JSON.
	 *
	 * @throws JSONException
	 *           Thrown if the string isn't a valid JSON.
	 */
	@Override
	protected JSON parse(final String input) throws JSONException {
		JSON result = null;
		try {
			result = parseObject(input);
		} catch (@SuppressWarnings("unused") final JSONException e) {
			result = parseArray(input);
		}
		return result;
	}

	/**
	 * Parse a JSON object from a string.
	 *
	 * @param input
	 *          The JSON string.
	 *
	 * @return The JSON object.
	 *
	 * @throws JSONException
	 *           Thrown if the string isn't a valid JSON object.
	 */
	protected static JSONObject parseObject(final String input) throws JSONException {
		return JSONObject.fromObject(input);
	}

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
	protected static JSONArray parseArray(final String input) throws JSONException {
		return JSONArray.fromObject(input);
	}
}
