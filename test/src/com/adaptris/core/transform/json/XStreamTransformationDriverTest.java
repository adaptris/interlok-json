package com.adaptris.core.transform.json;

import java.util.concurrent.ThreadLocalRandom;

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

  public XStreamTransformationDriverTest(String name) {
    super(name);
  }

  public void testToXML() throws Exception {
    XStreamTransformationDriver driver = new XStreamTransformationDriver();
    SerializableMessage original = configure(new SerializableAdaptrisMessage());
    String jsonData = jsonMarshaller().marshal(original);
    System.err.println(jsonData);
    String xmlData = driver.transform(jsonData, JsonXmlTransformService.TransformationDirection.JSON_TO_XML);
    System.err.println(xmlData);
    SerializableAdaptrisMessage unmarshalled = (SerializableAdaptrisMessage) xmlMarshaller().unmarshal(xmlData);
    assertEquals(original, unmarshalled);
  }

  public void testToJSON() throws Exception {
    XStreamTransformationDriver driver = new XStreamTransformationDriver();
    SerializableMessage original = configure(new SerializableAdaptrisMessage());
    String xmlData = xmlMarshaller().marshal(original);
    System.err.println(xmlData);
    String jsonData = driver.transform(xmlData, JsonXmlTransformService.TransformationDirection.XML_TO_JSON);
    System.err.println(jsonData);
    SerializableAdaptrisMessage unmarshalled = (SerializableAdaptrisMessage) jsonMarshaller().unmarshal(jsonData);
    assertEquals(original, unmarshalled);
  }


  public void testBadData() throws Exception {
    XStreamTransformationDriver driver = new XStreamTransformationDriver();
    SerializableMessage original = configure(new SerializableAdaptrisMessage());
    String xmlData = xmlMarshaller().marshal(original);
    try {
      String jsonData = driver.transform(xmlData, JsonXmlTransformService.TransformationDirection.JSON_TO_XML);
      fail();
    } catch (ServiceException expected) {

    }
  }


  private SerializableMessage configure(SerializableMessage msg) {
    msg.setContent("Pack my bag with a dozen liqour jugs");
    msg.setUniqueId(new GuidGenerator().getUUID());
    int max = ThreadLocalRandom.current().nextInt(10);
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
