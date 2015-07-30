package com.adaptris.core.transform.json;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XML to JSON converter and vice versa.
 * <p>
 * This service requires BOTH json.jar and json-lib-2.4-jdk15.jar on the classpath, since it supports both libraries to perform the
 * conversion. Select a transformation driver to determine which library to use. The "simple" library (json.jar) yields simpler
 * looking and cleaner XML, but will sometimes cause problems transforming XML elements into JSON arrays. The simple driver will
 * behave exactly like the legacy JSON services, including requiring and generating an XML element names "json" to wrap the
 * generated Xml.
 * </p>
 * <p>
 * The Default transformation driver uses json-lib-2.4-jdk15.jar and has a more complicated, but more information rich XML format.
 * This format can then be used to precisely control the JSON output when converting to JSON, resolving issues like improper array
 * generation. This driver always takes the entire message body as input and does not support converting only a part of it.
 * </p>
 * 
 * @config json-xml-transform-service
 * @license BASIC
 * 
 * @author gdries
 */
@XStreamAlias("json-xml-transform-service")
public class JsonXmlTransformService extends ServiceImp {

  @AutoPopulated
  @NotNull
  private DIRECTION direction;
  @NotNull
  @AutoPopulated
  @Valid
  private TransformationDriver driver;
  
  public JsonXmlTransformService() {
    setDirection(DIRECTION.JSON_TO_XML);
    setDriver(new JsonObjectTransformationDriver());
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
      return name();
      // return WordUtils.capitalizeFully(name(), new char[] {'_'}).replace('_', ' ');
    }
  }
  
  public interface TransformationDriver {
    public String transform(String input, DIRECTION direction) throws ServiceException;
  }
}
