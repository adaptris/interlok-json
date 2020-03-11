package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamJsonMarshaller;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.GuidGenerator;

public class XStreamTransformationDriverTest extends BaseCase {

  private transient XStreamMarshaller xmlMarshaller;
  private transient XStreamJsonMarshaller jsonMarshaller;


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testToXML() throws Exception {
    final XStreamTransformationDriver driver = new XStreamTransformationDriver();
    final SerializableMessage original = configure(new SerializableAdaptrisMessage());
    final String jsonData = jsonMarshaller().marshal(original);
    System.err.println(jsonData);
    final String xmlData = driver.transform(jsonData, TransformationDirection.JSON_TO_XML);
    System.err.println(xmlData);
    final SerializableAdaptrisMessage unmarshalled = (SerializableAdaptrisMessage) xmlMarshaller().unmarshal(xmlData);
    assertEquals(original, unmarshalled);
  }

  @Test
  public void testToJSON() throws Exception {
    final XStreamTransformationDriver driver = new XStreamTransformationDriver();
    final SerializableMessage original = configure(new SerializableAdaptrisMessage());
    final String xmlData = xmlMarshaller().marshal(original);
    System.err.println(xmlData);
    final String jsonData = driver.transform(xmlData, TransformationDirection.XML_TO_JSON);
    System.err.println(jsonData);
    final SerializableAdaptrisMessage unmarshalled = (SerializableAdaptrisMessage) jsonMarshaller().unmarshal(jsonData);
    assertEquals(original, unmarshalled);
  }

  @Test
  public void testBadData() throws Exception {
    final XStreamTransformationDriver driver = new XStreamTransformationDriver();
    final SerializableMessage original = configure(new SerializableAdaptrisMessage());
    final String xmlData = xmlMarshaller().marshal(original);
    try {
      final String jsonData = driver.transform(xmlData, TransformationDirection.JSON_TO_XML);
      fail();
    } catch (final ServiceException expected) {

    }
  }

  private SerializableMessage configure(final SerializableMessage msg) {
    msg.setContent("Pack my bag with a dozen liqour jugs");
    msg.setUniqueId(new GuidGenerator().getUUID());
    final int max = ThreadLocalRandom.current().nextInt(10);
    for (int i = 0; i < max; i++) {
      msg.addMessageHeader("header_" + i, "value_" + i);
    }
    return msg;
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
