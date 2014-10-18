package com.adaptris.core.transform.json;

import org.apache.commons.lang.WordUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XML to JSON converter and vice versa. This service requires BOTH json.jar and json-lib-2.4-jdk15.jar on the classpath, since it supports
 * both libraries to perform the conversion. Select a transformation driver to determine which library to use. The "simple" library (json.jar)
 * yields simpler looking and cleaner Xml, but will sometimes cause problems transforming Xml elements into JSON arrays. The simple driver will
 * behave exactly like the legacy JSON services, including requiring and generating an Xml element names "json" to wrap the generated Xml.
 * <p>
 * The Default transformation driver uses json-lib-2.4-jdk15.jar and has a more complicated, but more information rich Xml format. This format
 * can then be used to precisely control the JSON output when converting to JSON, resolving issues like improper array generation. This driver
 * always takes the entire message body as input and does not support converting only a part of it.
 * 
 * @config json-xml-transform-service
 * @license BASIC
 * 
 * @author gdries
 */
@XStreamAlias("json-xml-transform-service")
public class JsonXmlTransformService extends ServiceImp {

  private DIRECTION direction;
  private TransformationDriver driver;
  
  public JsonXmlTransformService() {
    setDirection(DIRECTION.JSON_TO_XML);
    setDriver(new DefaultJsonTransformationDriver());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.setStringPayload(getDriver().transform(msg.getStringPayload(), getDirection()), msg.getCharEncoding());
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
  
  public DIRECTION getDirection() {
    return direction;
  }

  public void setDirection(DIRECTION direction) {
    this.direction = direction;
  }

  public TransformationDriver getDriver() {
    return driver;
  }

  public void setDriver(TransformationDriver driver) {
    this.driver = driver;
  }

  public enum DIRECTION {
    JSON_TO_XML, XML_TO_JSON;
    
    @Override 
    public String toString() {
      return WordUtils.capitalizeFully(name(), new char[] {'_'}).replace('_', ' ');
    }
  }
  
  public interface TransformationDriver {
    public String transform(String input, DIRECTION direction) throws ServiceException;
  }
}
