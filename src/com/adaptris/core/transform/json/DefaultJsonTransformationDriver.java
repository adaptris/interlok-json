package com.adaptris.core.transform.json;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Default Transformation Driver that uses both {@link JSONObject#fromObject(Object)} and {@link JSONArray#fromObject(Object)} to
 * parse the input.
 * <p>
 * This uses the {@code net.sf.json} package, which in some situations, can be very sensitive to whitespace, and output may not be
 * as you expect. Accordingly, when you are generating the XML to be rendered as JSON make sure that you use
 * {@code xsl:strip-space elements="*"}.
 * </p>
 * 
 * @config default-transformation-driver
 * @author gdries
 */
@XStreamAlias("default-transformation-driver")
public class DefaultJsonTransformationDriver extends JsonTransformationDriverImpl {

	@Override
	protected JSON parse(final String input) throws JSONException {
		JSON result = null;
		try {
			result = parseObject(input);
    }
    catch (final JSONException e) {
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
