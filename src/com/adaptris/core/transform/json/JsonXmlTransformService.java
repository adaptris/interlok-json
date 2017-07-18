package com.adaptris.core.transform.json;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
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
 *
 * @author gdries
 */
@XStreamAlias("json-xml-transform-service")
@AdapterComponent
@ComponentProfile(summary = "Transform a JSON document to XML, or vice versa", tag = "service,transform,json,xml")
public class JsonXmlTransformService extends ServiceImp {

	@AutoPopulated
	@NotNull
	private TransformationDirection direction = TransformationDirection.JSON_TO_XML;

	@NotNull
	@AutoPopulated
	@Valid
	private TransformationDriver driver = new DefaultJsonTransformationDriver();

  public JsonXmlTransformService() {
    super();
  }

  public JsonXmlTransformService(TransformationDirection direction) {
    this(direction, new DefaultJsonTransformationDriver());
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
		msg.setContent(driver.transform(msg.getContent(), direction), msg.getContentEncoding());
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	protected void closeService() {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
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
	 * @param direction
	 *          The transformation direction.
	 */
	public void setDirection(final TransformationDirection direction) {
		this.direction = direction;
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
	 * @param driver
	 *          The transformation driver.
	 */
	public void setDriver(final TransformationDriver driver) {
		this.driver = driver;
	}
}
