package com.adaptris.core.transform.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDriver;
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

  private String jsonTag; 

  public SimpleJsonTransformationDriver() {
    setJsonTag("json");
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
      return XML.toJSONObject(input).getJSONObject(getJsonTag()).toString();
    } catch (JSONException e) {
      throw new ServiceException("Exception while converting XML to JSON", e);
    }
  }

  private String jsonToXML(String input) throws ServiceException {
    try {
      return XML.toString(new JSONObject(input), getJsonTag());
    } catch (JSONException e) {
      throw new ServiceException("Exception while converting JSON to XML", e);
    }
  }

  public String getJsonTag() {
    return jsonTag;
  }

  public void setJsonTag(String jsonTag) {
    this.jsonTag = jsonTag;
  }
}
