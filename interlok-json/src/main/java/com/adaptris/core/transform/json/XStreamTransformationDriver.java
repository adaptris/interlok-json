package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamJsonMarshaller;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Transformation drive that attempts to transform to and from JSON using a combination of
 * {@link XStreamMarshaller} and {@code XStreamJsonMarshaller}.
 *
 * <p>
 * Because XStream is primarily a way of serializing objects to XML and back again (we are using the
 * optional JSON support via {@code org.codehaus.jettison:jettison:1.2}), for some types of JSON
 * data, you will need to create java objects with appropriate {@link XStreamAlias} annotations that
 * represents your object tree. It is provided for completeness and not expected to be heavily used.
 * </p>
 *
 * @config xstream-json-transformation-driver
 */
@XStreamAlias("xstream-json-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver that uses XStream for conversion", tag = "json,xml,transformation")
public class XStreamTransformationDriver implements TransformationDriver {

	private transient XStreamMarshaller xmlMarshaller;
	private transient XStreamJsonMarshaller jsonMarshaller;

	public XStreamTransformationDriver() {
	}

	@Override
	public String transform(final String input, final TransformationDirection direction) throws ServiceException {
		String result = null;
		try {
			switch (direction) {
				case JSON_TO_XML: {
					result = xmlMarshaller().marshal(jsonMarshaller().unmarshal(input));
					break;
				}
				case XML_TO_JSON: {
					result = jsonMarshaller().marshal(xmlMarshaller().unmarshal(input));
					break;
				}
				default: {
					throw new ServiceException("Unsupported direction");
				}
			}
		} catch (final Exception e) {
			ExceptionHelper.rethrowServiceException(e);
		}
		return result;
	}

	private XStreamMarshaller xmlMarshaller() throws CoreException {
		if (xmlMarshaller == null) {
			xmlMarshaller = new XStreamMarshaller();
		}
		return xmlMarshaller;
	}

	private XStreamJsonMarshaller jsonMarshaller() throws CoreException {
		if (jsonMarshaller == null) {
			jsonMarshaller = new XStreamJsonMarshaller();
		}
		return jsonMarshaller;
	}

}
