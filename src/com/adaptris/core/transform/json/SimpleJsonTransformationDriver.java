package com.adaptris.core.transform.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSON converter.
 *
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert between JSON and XML.
 * </p>
 *
 * @config simple-transformation-driver
 *
 * @author gdries
 */
@XStreamAlias("simple-transformation-driver")
public class SimpleJsonTransformationDriver implements TransformationDriver {

	private static final String ELEMENT_NAME_ARRAY = "array-item";
	private static final String ELEMENT_NAME_JSON = "json";

	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	private String jsonTag = ELEMENT_NAME_JSON;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String transform(final String input, final TransformationDirection direction) throws ServiceException {
		switch (direction) {
			case JSON_TO_XML:
				return jsonToXML(input);

			case XML_TO_JSON:
				return xmlToJSON(input);

			default:
				throw new IllegalArgumentException("direction");
		}
	}

	/**
	 * Convert XML to JSON.
	 *
	 * @param input
	 *          XML to convert.
	 *
	 * @return The converted JSON.
	 *
	 * @throws ServiceException
	 *           Thrown if there was a problem converting from XML to JSON.
	 */
	private String xmlToJSON(final String input) throws ServiceException {
		try {

			final JSONObject xmlJsonObject = XML.toJSONObject(input);
			final JSONObject tagJsonObject = xmlJsonObject.getJSONObject(jsonTag);
			return tagJsonObject.toString();

		} catch (final JSONException e) {
			throw new ServiceException("Exception while converting XML to JSON", e);
		}
	}

	/**
	 * Convert JSON to XML.
	 *
	 * @param input
	 *          The JSON to convert.
	 *
	 * @return The converted XML.
	 *
	 * @throws ServiceException
	 *           Thrown if there was a problem converting from JSON to XML.
	 */
	private String jsonToXML(final String input) throws ServiceException {
		try {

			final JSONObject jsonObject = toJSONObject(input);
			return XML.toString(jsonObject, jsonTag);

		} catch (final JSONException e) {
			throw new ServiceException("Exception while converting JSON to XML", e);
		}
	}

	/**
	 * Convert a String to a JSONObject.
	 *
	 * @param input
	 *          The JSON string.
	 *
	 * @return The JSON object.
	 */
	private JSONObject toJSONObject(final String input) {
		JSONObject result = null;
		try {

			result = new JSONObject(new JSONTokener(input));

		} catch (final JSONException e) {
			log.debug("Exception [{}], attempting re-process as JSON Array", e.getMessage());
			result = new JSONObject();
			result.put(ELEMENT_NAME_ARRAY, new JSONArray(new JSONTokener(input)));
		}
		return result;
	}

	/**
	 * Get the JSON tag.
	 *
	 * @return The JSON tag.
	 */
	public String getJsonTag() {
		return jsonTag;
	}

	/**
	 * Set the JSON tag.
	 *
	 * @param jsonTag
	 *          The JSON tag.
	 */
	public void setJsonTag(final String jsonTag) {
		this.jsonTag = jsonTag;
	}
}
