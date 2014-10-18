package com.adaptris.core.transform.json;

import org.json.JSONObject;
import org.json.XML;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSCON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert from XML to JSON. The
 * XML input should contain a &lt;json> element that wraps the elements that need to be converted to JSON.
 * </p>
 * 
 * 
 * @config simple-xml-to-json-transform-service
 * @license BASIC
 * 
 * @author sellidge
 */
@XStreamAlias("simple-xml-to-json-transform-service")
@Deprecated
public class SimpleXmlToJsonTransformService extends ServiceImp {

  private static final String JSON_TAG = "json";

  public SimpleXmlToJsonTransformService() {
    log.warn("This service is deprecated, please upgrade to " + JsonXmlTransformService.class.getName());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      JSONObject object = XML.toJSONObject(msg.getStringPayload());
      msg.setStringPayload(object.get(JSON_TAG).toString(), msg.getCharEncoding());
    } catch (Exception e) {
      throw new ServiceException("Failed to convert XML to JSON", e);
    }
  }

  @Override
  public void close() {

  }

  @Override
  public void init() throws CoreException {

  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
