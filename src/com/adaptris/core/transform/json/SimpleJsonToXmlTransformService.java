package com.adaptris.core.transform.json;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.transform.json.JsonXmlTransformService.TransformationDirection;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple XML to JSCON converter.
 * 
 * <p>
 * This uses the default <a href="http://www.json.org/java/index.html">json.org implementation</a> to convert from JSON to XML. The
 * JSON input will be wrapped by a &lt;json> element when it is exported to XML.
 * </p>
 * 
 * @deprecated use {@link JsonXmlTransformService} with a {@link SimpleJsonTransformationDriver} instead.
 * @config simple-json-to-xml-transform-service
 * @license BASIC
 * @author sellidge
 */
@XStreamAlias("simple-json-to-xml-transform-service")
@Deprecated
@AdapterComponent
@ComponentProfile(summary = "Transform a JSON document to XML", tag = "service,transform,json,xml")
public class SimpleJsonToXmlTransformService extends ServiceImp {

  private static transient boolean warningLogged;

  private transient SimpleJsonTransformationDriver driver = new SimpleJsonTransformationDriver();

  public SimpleJsonToXmlTransformService() {
    if (!warningLogged) {
      log.warn("{} , is deprecated, please upgrade to {}", this.getClass().getSimpleName(),
          JsonXmlTransformService.class.getName());
      warningLogged = true;
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      msg.setStringPayload(driver.transform(msg.getStringPayload(), TransformationDirection.JSON_TO_XML), msg.getCharEncoding());
    } catch (Exception e) {
      throw new ServiceException("Failed to convert JSON to XML", e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
  }
}
