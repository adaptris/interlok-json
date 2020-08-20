package com.adaptris.core.transform.json;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XML to JSON converter and vice versa.
 *
 * @config json-xml-transform-service
 *
 * @author gdries
 */
@XStreamAlias("json-xml-transform-service")
@AdapterComponent
@ComponentProfile(summary = "Transform a JSON document to XML, or vice versa", tag = "service,transform,json,xml")
public class JsonXmlTransformService extends ServiceImp {

  @AutoPopulated
  @NotNull
  @InputFieldDefault(value = "JSON_TO_XML")
  private TransformationDirection direction = TransformationDirection.JSON_TO_XML;

  @NotNull
  @AutoPopulated
  @Valid
  @InputFieldDefault(value="simple-json-transformation-driver")
  private TransformationDriver driver;

  public JsonXmlTransformService() {
    super();
    setDriver(new SimpleJsonTransformationDriver());
  }

  public JsonXmlTransformService(TransformationDirection direction) {
    this(direction, new SimpleJsonTransformationDriver());
  }

  public JsonXmlTransformService(TransformationDirection direction, TransformationDriver driver) {
    this();
    setDriver(driver);
    setDirection(direction);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void doService(final AdaptrisMessage msg) throws ServiceException {
    driver.transform(msg, getDirection());
  }

  @Override
  public void prepare() throws CoreException {
    /* unused/empty method */
  }

  @Override
  protected void closeService() {
    /* unused/empty method */
  }

  @Override
  protected void initService() throws CoreException {
    /* unused/empty method */
  }

  /**
   * Get the transformation direction.
   *
   * @return The transformation direction.
   */
  public TransformationDirection getDirection() {
    return direction;
  }

  /**
   * Set the transformation direction.
   *
   * @param direction The transformation direction.
   */
  public void setDirection(final TransformationDirection direction) {
    this.direction = direction;
  }

  public JsonXmlTransformService withDirection(TransformationDirection d) {
    setDirection(d);
    return this;
  }

  /**
   * Get the transformation driver.
   *
   * @return The transformation driver.
   */
  public TransformationDriver getDriver() {
    return driver;
  }

  /**
   * Set the transformation driver.
   *
   * @param driver The transformation driver.
   */
  public void setDriver(final TransformationDriver driver) {
    this.driver = driver;
  }

  public JsonXmlTransformService withDriver(TransformationDriver d) {
    setDriver(d);
    return this;
  }
}
