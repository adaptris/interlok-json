package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDriver;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSCON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert from JSON to XML. The
 * JSON input will be wrapped by a &lt;json> element when it is exported to XML.
 * </p>
 * 
 * @config simple-json-to-xml-transform-service
 * @license BASIC
 * 
 * @author sellidge
 */
@XStreamAlias("simple-json-to-xml-transform-service")
@Deprecated
public class SimpleJsonToXmlTransformService extends ServiceImp {

  private final TransformationDriver driver = new SimpleJsonTransformationDriver();

  public SimpleJsonToXmlTransformService() {
    log.warn("This service is deprecated, please upgrade to " + JsonXmlTransformService.class.getName());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      msg.setStringPayload(driver.transform(msg.getStringPayload(), DIRECTION.JSON_TO_XML), msg.getCharEncoding());
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

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
