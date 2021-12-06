package com.adaptris.core.transform.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSON converter.
 *
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert between JSON and XML.
 * When converting <strong>to XML</strong>, it will add a root element called {@code json} (this is configurable via
 * {@link #setJsonTag(String)}) as the required XML root element. When converting <strong>from XML</strong> then it expects the same
 * tag as the root element of the XML.
 * </p>
 * <p>
 * If your input is a relatively JSON object, then this is the transformation driver to use.
 * The key differentiator is that where the output <strong>should be</strong>
 * a JSON array with a single element; it will not be supported by this driver implementation. You can still use it, but you will
 * have to execute a {@link JsonTransformService} afterwards to change the cardinality.
 * </p>
 * <p>
 * If the input is a JSON array, then {@code json-array} (not configurable) will be added as the root element, that wraps the JSON
 * array.
 * </p>
 * 
 * @config simple-transformation-driver
 *
 * @author gdries
 */
@XStreamAlias("simple-transformation-driver")
@ComponentProfile(summary = "Simple JSON/XML Transformation driver", tag = "json,xml,transformation")
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

	private String xmlToJSON(final String input) throws ServiceException {
		try {

			final JSONObject xmlJsonObject = XML.toJSONObject(input);
			final JSONObject tagJsonObject = xmlJsonObject.getJSONObject(jsonTag);
			return tagJsonObject.toString();

		} catch (final JSONException e) {
			throw new ServiceException("Exception while converting XML to JSON", e);
		}
	}

	private String jsonToXML(final String input) throws ServiceException {
		try {

			final JSONObject jsonObject = toJSONObject(input);
			return XML.toString(jsonObject, jsonTag);

		} catch (final JSONException e) {
			throw new ServiceException("Exception while converting JSON to XML", e);
		}
	}

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
