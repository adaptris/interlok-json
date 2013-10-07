package com.adaptris.core.transform.json;

import org.json.JSONObject;
import org.json.XML;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSCON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert from JSON to XML. The
 * JSON input will be wrapped by a &lt;json> element when it is exported to XML.
 * </p>
 * 
 * <p>
 * In the adapter configuration file this class is aliased as <b>simple-json-to-xml-transform-service</b> which is the preferred
 * alternative to the fully qualified classname when building your configuration.
 * </p>
 * 
 * @author sellidge
 */
@XStreamAlias("simple-json-to-xml-transform-service")
public class SimpleJsonToXmlTransformService extends ServiceImp {

  private static final String JSON_TAG = "json";

  public SimpleJsonToXmlTransformService() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      JSONObject object = new JSONObject(msg.getStringPayload());
      String xml = XML.toString(object, JSON_TAG);
      msg.setStringPayload(xml);
    } catch (Exception e) {
      throw new ServiceException("Failed to convert JSON to XML", e);
    }
  }

  @Override
  public void close() {

  }

  @Override
  public void init() throws CoreException {

  }

}
