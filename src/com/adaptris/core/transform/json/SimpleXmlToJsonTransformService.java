package com.adaptris.core.transform.json;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
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
 *
 * @author sellidge
 */
@XStreamAlias("simple-xml-to-json-transform-service")
@Deprecated
@AdapterComponent
@ComponentProfile(summary = "Transform a XML document to JSON", tag = "service,transform,json,xml")
public class SimpleXmlToJsonTransformService extends ServiceImp {

	private transient static boolean warningLogged;

	private transient SimpleJsonTransformationDriver driver = new SimpleJsonTransformationDriver();

	public SimpleXmlToJsonTransformService() {
		if (!warningLogged) {
			log.warn("{} , is deprecated, please upgrade to {}", this.getClass().getSimpleName(), JsonXmlTransformService.class.getName());
			warningLogged = true;
		}
	}

	@Override
	public void doService(final AdaptrisMessage msg) throws ServiceException {
		try {
			msg.setStringPayload(driver.transform(msg.getStringPayload(), TransformationDirection.XML_TO_JSON), msg.getCharEncoding());
		} catch (final Exception e) {
			throw new ServiceException("Failed to convert XML to JSON", e);
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
