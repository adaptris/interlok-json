package com.adaptris.core.transform.json;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.ServiceException;

import net.sf.json.JSON;
import net.sf.json.JSONException;

/**
 * Transformation Driver using the {@code net.sf.json} package.
 */
public abstract class JsonTransformationDriverImpl implements TransformationDriver {

	/*
	 * These fields exists to make XStream serialize the driver properly. Since the driver
	 * uses an XMLSerializer instance, exposing only SOME of its settings, we don't want the
	 * serializer itself to be generated into the XML. There are some issues with the
	 * 'expandedProperties' field that cause it to not (de)serialize correctly.
	 */
	@AdvancedConfig
	private String arrayName;
	@AdvancedConfig
	private String elementName;
	@AdvancedConfig
	private String objectName;
	@AdvancedConfig
	private String rootName;
	@AdvancedConfig
	private Boolean forceTopLevelObject;
	@AdvancedConfig
	private Boolean skipWhitespace;
	@AdvancedConfig
	private Boolean trimSpaces;
	@AdvancedConfig
	private Boolean typeHintsCompatibility;
	@AdvancedConfig
	private Boolean typeHintsEnabled;

	private static final boolean DEFAULT_FORCE_TOP_LEVEL_OBJECT;
	private static final boolean DEFAULT_SKIP_WHITE_SPACE;
	private static final boolean DEFAULT_TRIM_SPACES;
	private static final boolean DEFAULT_TYPE_HINTS_COMPAT;
	private static final boolean DEFAULT_TYPE_HINTS_ENABLED;

	private static final String DEFAULT_ARRAYNAME;
	private static final String DEFAULT_ELEMENTNAME;
	private static final String DEFAULT_OBJECTNAME;
	private static final String DEFAULT_ROOTNAME;

	static {
		final XMLSerializer serializer = new XMLSerializer();
		DEFAULT_ARRAYNAME = serializer.getArrayName();
		DEFAULT_ELEMENTNAME = serializer.getElementName();
		DEFAULT_OBJECTNAME = serializer.getObjectName();
		DEFAULT_ROOTNAME = serializer.getRootName();

		DEFAULT_FORCE_TOP_LEVEL_OBJECT = serializer.isForceTopLevelObject();
		DEFAULT_SKIP_WHITE_SPACE = serializer.isSkipWhitespace();
		DEFAULT_TRIM_SPACES = serializer.isTrimSpaces();
		DEFAULT_TYPE_HINTS_COMPAT = serializer.isTypeHintsCompatibility();
		DEFAULT_TYPE_HINTS_ENABLED = serializer.isTypeHintsEnabled();
	}

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
	 *          The XML to convert.
	 *
	 * @return The converted JSON.
	 *
	 * @throws ServiceException
	 *           Thrown if there was a problem converting from XML to JSON.
	 */
  protected String xmlToJSON(final String input) throws ServiceException {
		try {

			return getSerializer().read(input).toString();

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

			final JSON object = parse(input);
			return getSerializer().write(object);

		} catch (final JSONException e) {
			throw new ServiceException("Exception while converting JSON to XML", e);
		}
	}

	/**
	 * Parse the string to JSON.
	 *
	 * @param input
	 *          The string to parse.
	 *
	 * @return The JSON.
	 *
	 * @throws JSONException
	 *           Thrown if the string could not be parsed to JSON.
	 */
	protected abstract JSON parse(String input) throws JSONException;

	/**
	 * Create an XML serializer with the data from the member variables.
	 *
	 * @return The newly created/populated XML serializer.
	 */
	private XMLSerializer getSerializer() {
		final XMLSerializer serializer = new XMLSerializer();
    serializer.setArrayName(arrayName());
		serializer.setElementName(elementName());
		serializer.setObjectName(objectName());
		serializer.setRootName(rootName());
		serializer.setForceTopLevelObject(forceTopLevelObject());
		serializer.setSkipWhitespace(skipWhitespace());
		serializer.setTrimSpaces(trimSpaces());
		serializer.setTypeHintsCompatibility(typeHintsCompatibility());
		serializer.setTypeHintsEnabled(typeHintsEnabled());
		return serializer;
	}

	/**
	 * Get the array name.
	 *
	 * @return The array name.
	 */
	public String getArrayName() {
		return arrayName;
	}

	/**
	 * Get the array name, or default value if null.
	 *
	 * @return The array name, or default value.
	 */
  String arrayName() {
		return arrayName != null ? arrayName : DEFAULT_ARRAYNAME;
	}

	/**
	 * Set the array name.
	 *
	 * @param arrayName
	 *          The array name.
	 */
	public void setArrayName(final String arrayName) {
		this.arrayName = arrayName;
	}

	/**
	 * Get the element name.
	 *
	 * @return The element name.
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * Get the element name, or default value if null.
	 *
	 * @return The element name, or default value.
	 */
  String elementName() {
		return elementName != null ? elementName : DEFAULT_ELEMENTNAME;
	}

	/**
	 * Set the element name.
	 *
	 * @param elementName
	 *          The element name.
	 */
	public void setElementName(final String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Get the object name.
	 *
	 * @return The object name.
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * Get the object name, or default value if null.
	 *
	 * @return The object name, or default value.
	 */
	public String objectName() {
		return objectName != null ? objectName : DEFAULT_OBJECTNAME;
	}

	/**
	 * Set the object name.
	 *
	 * @param objectName
	 *          The object name.
	 */
	public void setObjectName(final String objectName) {
		this.objectName = objectName;
	}

	/**
	 * Get the root name.
	 *
	 * @return The root name.
	 */
	public String getRootName() {
		return rootName;
	}

	/**
	 * Get the root name, or default value if null.
	 *
	 * @return The root name, or default value.
	 */
  String rootName() {
		return rootName != null ? rootName : DEFAULT_ROOTNAME;
	}

	/**
	 * Set the root name.
	 *
	 * @param rootName
	 *          The root name.
	 */
	public void setRootName(final String rootName) {
		this.rootName = rootName;
	}

	/**
	 * Whether force top level is set. (Use object as value may not have been defined in configuration, hence null.)
	 *
	 * @return True if force top level is set.
	 */
	public Boolean getForceTopLevelObject() {
		return forceTopLevelObject;
	}

  boolean forceTopLevelObject() {
		return forceTopLevelObject != null ? forceTopLevelObject : DEFAULT_FORCE_TOP_LEVEL_OBJECT;
	}

	/**
	 * Set whether to force top level object.
	 *
	 * @param forceTopLevelObject
	 *          Whether to force top level object.
	 */
  public void setForceTopLevelObject(final Boolean forceTopLevelObject) {
		this.forceTopLevelObject = forceTopLevelObject;
	}

	/**
	 * Whether skip whitespace is set. (Use object as value may not have been defined in configuration, hence null.)
	 *
	 * @return True if skip whitespace is set.
	 */
	public Boolean getSkipWhitespace() {
		return skipWhitespace;
	}

	public boolean skipWhitespace() {
		return skipWhitespace != null ? skipWhitespace : DEFAULT_SKIP_WHITE_SPACE;
	}

	/**
	 * Set whether to skip whitespace.
	 *
	 * @param skipWhitespace
	 *          Whether to skip whitespace.
	 */
  public void setSkipWhitespace(final Boolean skipWhitespace) {
		this.skipWhitespace = skipWhitespace;
	}

	/**
	 * Whether trim whitespace is set. (Use object as value may not have been defined in configuration, hence null.)
	 *
	 * @return True if trim whitespace is set.
	 */
	public Boolean getTrimSpaces() {
		return trimSpaces;
	}

  boolean trimSpaces() {
		return trimSpaces != null ? trimSpaces : DEFAULT_TRIM_SPACES;
	}

	/**
	 * Set whether to trim whitespace.
	 *
	 * @param trimSpaces
	 *          Whether to trim whitespace.
	 */
  public void setTrimSpaces(final Boolean trimSpaces) {
		this.trimSpaces = trimSpaces;
	}

	/**
	 * Whether type hints compatibility is set. (Use object as value may not have been defined in configuration, hence null.)
	 *
	 * @return True if type hints compatibility is set.
	 */
	public Boolean getTypeHintsCompatibility() {
		return typeHintsCompatibility;
	}

  boolean typeHintsCompatibility() {
		return typeHintsCompatibility != null ? typeHintsCompatibility : DEFAULT_TYPE_HINTS_COMPAT;
	}

	/**
	 * Set whether type hints compatibility is enabled.
	 *
	 * @param typeHintsCompatibility
	 *          Whether type hints compatibility is enabled.
	 */
  public void setTypeHintsCompatibility(final Boolean typeHintsCompatibility) {
		this.typeHintsCompatibility = typeHintsCompatibility;
	}

	/**
	 * Whether type hints is enabled. (Use object as value may not have been defined in configuration, hence null.)
	 *
	 * @return True if type hints is enabled.
	 */
  public Boolean getTypeHintsEnabled() {
		return typeHintsEnabled;
	}

  boolean typeHintsEnabled() {
		return typeHintsEnabled != null ? typeHintsEnabled : DEFAULT_TYPE_HINTS_ENABLED;
	}

	/**
	 * Set whether type hints is enabled.
	 *
	 * @param typeHintsEnabled
	 *          Whether type hints is enabled.
	 */
	public void setTypeHintsEnabled(final Boolean typeHintsEnabled) {
		this.typeHintsEnabled = typeHintsEnabled;
	}
}
