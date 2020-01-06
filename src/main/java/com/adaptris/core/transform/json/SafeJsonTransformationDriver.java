package com.adaptris.core.transform.json;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Alternative to {@link DefaultJsonTransformationDriver} that executes a transform to strip-spaces before rendering as JSON.
 * <p>
 * This differs from {@link DefaultJsonTransformationDriver} in that it will execute a transform to strip spaces using the standard
 * {@code xsl:strip-space elements="*"} directive before attempting to serialize the XML as JSON. In some situations the default
 * driver can be very sensitive to whitespace that may occur because of indenting and formatting.
 * </p>
 * <p>
 * The transform used is stored in the jar file itself - {@value #STRIP_SPACES_XSLT}; it is very simple, and may not handle
 * namespaces terribly well. If in doubt, execute a transform yourslef, and then use {@link DefaultJsonTransformationDriver} as
 * usual.
 * </p>
 * 
 * @since 3.6.4
 * @config json-safe-transformation-driver
 */
@XStreamAlias("json-safe-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver that strips spaces before rendering as JSON", since = "3.6.4",
    tag = "json,xml,transformation")
public class SafeJsonTransformationDriver extends DefaultJsonTransformationDriver {
  private transient DocumentBuilder builder = null;
  private transient Transformer transformer = null;

  public static final String STRIP_SPACES_XSLT = "META-INF/com.adaptris.core.transform.json.strip-spaces.xsl";

  @Override
  protected String xmlToJSON(final String input) throws ServiceException {
    return super.xmlToJSON(transform(new StringReader(input)));
  }

  private String transform(Reader input) throws ServiceException {
    String result = null;
    try (Reader autoClose = input; StringWriter output = new StringWriter()) {
      robotsInDisguise().transform(new DOMSource(builder().parse(new InputSource(input))), new StreamResult(output));
      result = output.toString();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return result;
  }

  private Transformer robotsInDisguise() throws Exception {
    if (transformer == null) {
      URL url = getClass().getClassLoader().getResource(STRIP_SPACES_XSLT);
      try (InputStream in = url.openConnection().getInputStream()) {
        // builder() defaults to in instance that disables xxe.
        Document xmlDoc = builder().parse(in); // lgtm [java/xxe]
        transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(xmlDoc, url.toExternalForm()));
      }
    }
    return transformer;
  }

  private DocumentBuilder builder() throws ParserConfigurationException {
    if (builder == null) {
      builder = DocumentBuilderFactoryBuilder.newRestrictedInstance().withNamespaceAware(true)
          .newDocumentBuilder(DocumentBuilderFactory.newInstance());
    }
    return builder;
  }

}
