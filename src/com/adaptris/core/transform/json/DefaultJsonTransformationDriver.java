package com.adaptris.core.transform.json;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDriver;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default Transformation Driver.
 * 
 * @config default-transformation-driver
 * @author gdries
 */
@XStreamAlias("default-transformation-driver")
public class DefaultJsonTransformationDriver implements TransformationDriver {

  /* These fields exists to make XStream serialize the driver properly. Since the driver 
   * uses an XMLSerializer instance, exposing only SOME of its settings, we don't want the
   * serializer itself to be generated into the XML. There are some issues with the 
   * 'expandedProperties' field that cause it to not (de)serialize correctly. */
  private String arrayName;
  private String elementName;
  private String objectName;
  private String rootName;
  private Boolean forceTopLevelObject;
  private Boolean skipWhitespace;
  private Boolean trimSpaces;
  private Boolean typeHintsCompatibility;
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
    XMLSerializer serializer = new XMLSerializer();
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

  public DefaultJsonTransformationDriver() {
  }
  
  @Override
  public String transform(String input, DIRECTION direction) throws ServiceException {
    switch(direction) {
    case JSON_TO_XML:
      return jsonToXML(input);
      
    case XML_TO_JSON:
      return xmlToJSON(input);
      
    default:
      throw new IllegalArgumentException("direction");
    }
  }
  
  private String xmlToJSON(String input) throws ServiceException {
    try {
      return getSerializer().read(input).toString();
    } catch (JSONException e) {
      throw new ServiceException("Exception while converting XML to JSON", e);
    }
  }

  private String jsonToXML(String input) throws ServiceException {
    try {
      JSONObject object = JSONObject.fromObject(input);
      return getSerializer().write(object);
    } catch (JSONException e) {
      throw new ServiceException("Exception while converting JSON to XML", e);
    }
  }
  
  private XMLSerializer getSerializer() {
    XMLSerializer serializer = new XMLSerializer();
    serializer.setArrayName(arrayName());
    serializer.setElementName(elementName());
    serializer.setObjectName(objectName());
    serializer.setRootName(rootName());
    serializer.setForceTopLevelObject(isForceTopLevelObject());
    serializer.setSkipWhitespace(isSkipWhitespace());
    serializer.setTrimSpaces(isTrimSpaces());
    serializer.setTypeHintsCompatibility(isTypeHintsCompatibility());
    serializer.setTypeHintsEnabled(isTypeHintsEnabled());
    return serializer;
  }

  public String getArrayName() {
    return arrayName;
  }

  public void setArrayName(String arrayName) {
    this.arrayName = arrayName;
  }

  String arrayName() {
    return getArrayName() != null ? getArrayName() : DEFAULT_ARRAYNAME;
  }

  public String getElementName() {
    return elementName;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  String elementName() {
    return getElementName() != null ? getElementName() : DEFAULT_ELEMENTNAME;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  String objectName() {
    return getObjectName() != null ? getObjectName() : DEFAULT_OBJECTNAME;
  }

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  String rootName() {
    return getRootName() != null ? getRootName() : DEFAULT_ROOTNAME;
  }

  boolean isForceTopLevelObject() {
    return getForceTopLevelObject() != null ? getForceTopLevelObject().booleanValue() : DEFAULT_FORCE_TOP_LEVEL_OBJECT;
  }

  public void setForceTopLevelObject(Boolean forceTopLevelObject) {
    this.forceTopLevelObject = forceTopLevelObject;
  }

  public Boolean getForceTopLevelObject() {
    return forceTopLevelObject;
  }

  boolean isSkipWhitespace() {
    return getSkipWhitespace() != null ? getSkipWhitespace().booleanValue() : DEFAULT_SKIP_WHITE_SPACE;
  }

  public void setSkipWhitespace(Boolean skipWhitespace) {
    this.skipWhitespace = skipWhitespace;
  }

  public Boolean getSkipWhitespace() {
    return skipWhitespace;
  }

  boolean isTrimSpaces() {
    return getTrimSpaces() != null ? getTrimSpaces().booleanValue() : DEFAULT_TRIM_SPACES;
  }

  public void setTrimSpaces(Boolean trimSpaces) {
    this.trimSpaces = trimSpaces;
  }

  public Boolean getTrimSpaces() {
    return trimSpaces;
  }

  boolean isTypeHintsCompatibility() {
    return getTypeHintsCompatibility() != null ? getTypeHintsCompatibility().booleanValue() : DEFAULT_TYPE_HINTS_COMPAT;
  }

  public void setTypeHintsCompatibility(Boolean typeHintsCompatibility) {
    this.typeHintsCompatibility = typeHintsCompatibility;
  }

  public Boolean getTypeHintsCompatibility() {
    return typeHintsCompatibility;
  }

  boolean isTypeHintsEnabled() {
    return getTypeHintsEnabled() != null ? getTypeHintsEnabled().booleanValue() : DEFAULT_TYPE_HINTS_ENABLED;
  }

  public void setTypeHintsEnabled(Boolean typeHintsEnabled) {
    this.typeHintsEnabled = typeHintsEnabled;
  }

  public Boolean getTypeHintsEnabled() {
    return typeHintsEnabled;
  }

}
