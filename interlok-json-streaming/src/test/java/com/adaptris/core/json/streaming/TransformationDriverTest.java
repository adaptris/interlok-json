package com.adaptris.core.json.streaming;

import static com.adaptris.core.json.streaming.JsonConversionTest.JSON_MESSAGE;
import static com.adaptris.core.json.streaming.JsonConversionTest.XML_MESSAGE;
import static com.adaptris.core.json.streaming.JsonConversionTest.unwrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.EnumSet;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.transform.json.TransformationDirection;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class TransformationDriverTest {

  @Test(expected = ServiceException.class)
  public void testDefault_Exception() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.INPUT).newMessage(XML_MESSAGE);
    DefaultStreamingTransformationDriver driver = new DefaultStreamingTransformationDriver();
    driver.transform(msg, TransformationDirection.XML_TO_JSON);
  }


  @Test
  public void testDefault_XmlToJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    DefaultStreamingTransformationDriver driver = new DefaultStreamingTransformationDriver();
    driver.transform(msg, TransformationDirection.XML_TO_JSON);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder()
        .jsonProvider(new JsonSmartJsonProvider()).mappingProvider(new JacksonMappingProvider())
        .options(EnumSet.noneOf(Option.class)).build();
    ReadContext ctx = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(ctx.read("$.envelope.document"));
  }

  @Test
  public void testDefault_JsonToXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_MESSAGE);
    DefaultStreamingTransformationDriver driver = new DefaultStreamingTransformationDriver();
    driver.transform(msg, TransformationDirection.JSON_TO_XML);
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals(3, xpath.selectNodeList(d, "/envelope/document").getLength());
  }

  @Test
  public void testAdvanced_XmlToJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    AdvancedStreamingTransformationDriver driver =
        new AdvancedStreamingTransformationDriver().withConfig(new JsonStreamingConfigBuilder()
            .withAutoArray(Boolean.TRUE).withPrettyPrint(Boolean.FALSE));
    driver.transform(msg, TransformationDirection.XML_TO_JSON);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext ctx = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertEquals("3", unwrap(ctx.read("$..document.length()").toString()));
  }

  @Test
  public void testAdvanced_JsonToXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_MESSAGE);
    AdvancedStreamingTransformationDriver driver = new AdvancedStreamingTransformationDriver().withConfig(
            new JsonStreamingConfigBuilder().withAutoArray(Boolean.TRUE)
                .withMultipleProcessingInstruction(Boolean.FALSE));
    driver.transform(msg, TransformationDirection.JSON_TO_XML);
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals(3, xpath.selectNodeList(d, "/envelope/document").getLength());
  }

}
