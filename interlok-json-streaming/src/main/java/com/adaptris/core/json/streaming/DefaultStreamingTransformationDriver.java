package com.adaptris.core.json.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.TransformationDirection;
import com.adaptris.core.transform.json.TransformationDriver;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.stax.CloseableStaxWrapper;
import com.adaptris.stax.DefaultInputFactory;
import com.adaptris.stax.DefaultWriterFactory;
import com.adaptris.stax.XmlInputFactoryBuilder;
import com.adaptris.stax.XmlOutputFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * {@link TransformationDriver} implementation that uses all the defaults.
 *
 * @config default-streaming-transformation-driver
 *
 */
@XStreamAlias("default-streaming-transformation-driver")
@NoArgsConstructor
@ComponentProfile(summary = "streaming xml/json driver using defaults",
    tag = "json,xml", since = "3.11.0")
public class DefaultStreamingTransformationDriver implements TransformationDriver {

  private static XmlInputFactoryBuilder XML_IN =  new DefaultInputFactory();
  private static XmlOutputFactoryBuilder XML_OUT = new DefaultWriterFactory();

  private static XmlInputFactoryBuilder JSON_IN =new JsonStreamingInputFactory();
  private static XmlOutputFactoryBuilder JSON_OUT = new JsonStreamingOutputFactory();

  @Override
  public void transform(AdaptrisMessage msg, TransformationDirection dir) throws ServiceException {
    try (InputStream in = new BufferedInputStream(msg.getInputStream());
        OutputStream out = new BufferedOutputStream(msg.getOutputStream());
        CloseableStaxWrapper wrapper = new CloseableStaxWrapper(
            inputBuilder(dir).createXMLEventReader(in),
            outputBuilder(dir).createXMLEventWriter(out))) {
      wrapper.writer().add(wrapper.reader());
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected XMLInputFactory inputBuilder(TransformationDirection dir) {
    if (dir == TransformationDirection.JSON_TO_XML) {
      return JSON_IN.build();
    }
    return XML_IN.build();
  }


  protected XMLOutputFactory outputBuilder(TransformationDirection dir) {
    if (dir == TransformationDirection.JSON_TO_XML) {
      return XML_OUT.build();
    }
    return JSON_OUT.build();
  }
}
