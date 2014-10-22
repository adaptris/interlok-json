package com.adaptris.core.transform.json;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDriver;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
  private boolean forceTopLevelObject;
  private boolean skipWhitespace;
  private boolean trimSpaces;
  private boolean typeHintsCompatibility;
  private boolean typeHintsEnabled;
  
  public DefaultJsonTransformationDriver() {
    // Get the default values from a throw away serializer
    XMLSerializer serializer = new XMLSerializer();
    setArrayName(serializer.getArrayName());
    setElementName(serializer.getElementName());
    setObjectName(serializer.getObjectName());
    setRootName(serializer.getRootName());
    setForceTopLevelObject(serializer.isForceTopLevelObject());
    setSkipWhitespace(serializer.isSkipWhitespace());
    setTrimSpaces(serializer.isTrimSpaces());
    setTypeHintsCompatibility(serializer.isTypeHintsCompatibility());
    setTypeHintsEnabled(serializer.isTypeHintsEnabled());
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
    serializer.setArrayName(getArrayName());
    serializer.setElementName(getElementName());
    serializer.setObjectName(getObjectName());
    serializer.setRootName(getRootName());
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

  public String getElementName() {
    return elementName;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  public boolean isForceTopLevelObject() {
    return forceTopLevelObject;
  }

  public void setForceTopLevelObject(boolean forceTopLevelObject) {
    this.forceTopLevelObject = forceTopLevelObject;
  }

  public boolean isSkipWhitespace() {
    return skipWhitespace;
  }

  public void setSkipWhitespace(boolean skipWhitespace) {
    this.skipWhitespace = skipWhitespace;
  }

  public boolean isTrimSpaces() {
    return trimSpaces;
  }

  public void setTrimSpaces(boolean trimSpaces) {
    this.trimSpaces = trimSpaces;
  }

  public boolean isTypeHintsCompatibility() {
    return typeHintsCompatibility;
  }

  public void setTypeHintsCompatibility(boolean typeHintsCompatibility) {
    this.typeHintsCompatibility = typeHintsCompatibility;
  }

  public boolean isTypeHintsEnabled() {
    return typeHintsEnabled;
  }

  public void setTypeHintsEnabled(boolean typeHintsEnabled) {
    this.typeHintsEnabled = typeHintsEnabled;
  }

}
