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

  private XMLSerializer serializer;
  
  public DefaultJsonTransformationDriver() {
    serializer = new XMLSerializer();
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

  public void setSerializer(XMLSerializer serializer) {
    this.serializer = serializer;
  }
  
  public XMLSerializer getSerializer() {
    return serializer;
  }
}
