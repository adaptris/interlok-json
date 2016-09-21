package com.adaptris.core.transform.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDirection;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDriver;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert between JSON and XML.
 * </p>
 * @config simple-transformation-driver
 * 
 * @author gdries
 */
@XStreamAlias("simple-transformation-driver")
public class SimpleJsonTransformationDriver implements TransformationDriver {

  private static final String ELEMENT_NAME_ARRAY = "array-item";
  private static final String ELEMENT_NAME_JSON = "json";

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private String jsonTag; 

  public SimpleJsonTransformationDriver() {
    setJsonTag(ELEMENT_NAME_JSON);
  }
  
  @Override
  public String transform(String input, TransformationDirection direction) throws ServiceException {
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
    String result = null;
    try {
      result = XML.toString(toJSONObject(input), getJsonTag());
    } catch (JSONException e) {
      throw new ServiceException("Exception while converting JSON to XML", e);
    }
    return result;
  }

  private JSONObject toJSONObject(String input) {
    JSONObject result = null;
    try {
      result = new JSONObject(new JSONTokener(input));
    } catch (JSONException e) {
      log.debug("Exception [{}], attempting re-process as JSON Array", e.getMessage());
      result = new JSONObject();
      result.put(ELEMENT_NAME_ARRAY, new JSONArray(new JSONTokener(input)));
    }
    return result;
  }

  private JSONTokener tokenize(String input) {
    return new JSONTokener(input);
  }

  public String getJsonTag() {
    return jsonTag;
  }

  public void setJsonTag(String jsonTag) {
    this.jsonTag = jsonTag;
  }
}
