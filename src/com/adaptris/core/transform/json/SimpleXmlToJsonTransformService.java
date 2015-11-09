package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedService;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSCON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert from XML to JSON. The
 * XML input should contain a &lt;json> element that wraps the elements that need to be converted to JSON.
 * </p>
 * 
 * @deprecated use {@link JsonXmlTransformService} with a {@link SimpleJsonTransformationDriver} instead.
 * @config simple-xml-to-json-transform-service
 * @license BASIC
 * 
 * @author sellidge
 */
@XStreamAlias("simple-xml-to-json-transform-service")
@Deprecated
public class SimpleXmlToJsonTransformService extends LicensedService {

  private transient static boolean warningLogged;

  private transient SimpleJsonTransformationDriver driver = new SimpleJsonTransformationDriver();

  public SimpleXmlToJsonTransformService() {
    if (!warningLogged) {
      log.warn("{} , is deprecated, please upgrade to {}", this.getClass().getSimpleName(),
          JsonXmlTransformService.class.getName());
      warningLogged = true;
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      msg.setStringPayload(driver.transform(msg.getStringPayload(), DIRECTION.XML_TO_JSON), msg.getCharEncoding());
    } catch (Exception e) {
      throw new ServiceException("Failed to convert XML to JSON", e);
    }
  }

  @Override
  protected void prepareService() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Basic);
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
  }
}
