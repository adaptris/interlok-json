package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Transformation driver that uses instead of {@link JSONObject#fromObject(Object)} to read the JSON payload.
 *
 * @since 3.0.4
 * @see DefaultJsonTransformationDriver
 * @config json-object-transformation-driver
 */
@XStreamAlias("json-object-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver, supports a top level JSON Object", since = "3.0.4",
    tag = "json,xml,transformation")
public class JsonObjectTransformationDriver extends DefaultJsonTransformationDriver {

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
	@Override
	protected JSON parse(final String input) throws JSONException {
		return parseObject(input);
	}
}
