package com.adaptris.core.transform.json;

import com.adaptris.annotation.InputFieldHint;

/**
 * Direction enum; JSON <-> XML.
 */
public enum TransformationDirection {
	/**
	 * JSON to XML.
	 */
	@InputFieldHint(friendly = "JSON to XML") JSON_TO_XML,

	/**
	 * XML to JSON.
	 */
	@InputFieldHint(friendly = "XML to JSON") XML_TO_JSON;
}
