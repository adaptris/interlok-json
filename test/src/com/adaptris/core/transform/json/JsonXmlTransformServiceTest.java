package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.transform.json.JsonXmlTransformService;
import com.adaptris.core.transform.json.SimpleJsonTransformationDriver;
import com.adaptris.core.transform.json.JsonXmlTransformService.DIRECTION;

public class JsonXmlTransformServiceTest extends TransformServiceExample {

  public JsonXmlTransformServiceTest(String name) {
    super(name);
  }

  // Input/output for Default Xml -> JSON transformation
  static final String DEFAULT_XML_INPUT = "<xml><version>0.5</version>\n" + "" + "<entry>\n"
      + "<name>Production System</name>\n" + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n"
      + "<name>R&amp;D sandbox</name>\n" + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</xml>";
  static final String JSON_OUTPUT =
      "{\"version\":\"0.5\",\"entry\":[{\"name\":\"Production System\",\"location\":\"Seattle\"},{\"name\":\"R&D sandbox\",\"location\":\"New York\"}],\"notes\":\"Some Notes\"}";
  
  // Input/output for Default and Simple JSON -> Xml transformation
  static final String JSON_INPUT = "{\n\"entry\":[\n" + "{\n\"location\":\"Seattle\"," + "\n\"name\":\"Production System\"},\n"
      + "{\"location\":\"New York\",\n" + "\"name\":\"R&D sandbox\"\n" + "}\n" + "],\n" + "\"notes\":\"Some Notes\",\n"
      + "\"version\":0.5\n" + "}";
  static final String DEFAULT_XML_OUTPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<o><entry class=\"array\"><e class=\"object\">"
      + "<location type=\"string\">Seattle</location><name type=\"string\">Production System</name></e><e class=\"object\">"
      + "<location type=\"string\">New York</location><name type=\"string\">R&amp;D sandbox</name></e></entry><notes type=\"string\">Some Notes</notes>"
      + "<version type=\"number\">0.5</version></o>\r\n";
  static final String SIMPLE_XML_OUTPUT = "<json><entry><location>Seattle</location><name>Production System</name></entry><entry><location>New York</location>"
      + "<name>R&amp;D sandbox</name></entry><notes>Some Notes</notes><version>0.5</version></json>";

  // Input/output for Simple Xml -> JSON transformation
  static final String SIMPLE_XML_INPUT = "<json><version>0.5</version>\n" + "" + "<entry>\n"
      + "<name>Production System</name>\n" + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n"
      + "<name>R&amp;D sandbox</name>\n" + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</json>";
  static final String SIMPLE_JSON_OUTPUT = "{\"entry\":[{\"location\":\"Seattle\",\"name\":\"Production System\"},{\"location\":\"New York\",\"name\":\"R&D sandbox\"}]"
      + ",\"notes\":\"Some Notes\",\"version\":0.5}";

  public void testTransformToXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(DIRECTION.JSON_TO_XML);
    execute(svc, msg);
    assertEquals(DEFAULT_XML_OUTPUT, msg.getStringPayload());
  }
  
  public void testTransformToJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_XML_INPUT);
    
    JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(DIRECTION.XML_TO_JSON);
    execute(svc, msg);
    assertEquals(JSON_OUTPUT, msg.getStringPayload());
  }

  public void testTransformJsonToSimpleXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(DIRECTION.JSON_TO_XML);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    System.out.println(msg.getStringPayload());
    
    assertEquals(SIMPLE_XML_OUTPUT, msg.getStringPayload());
  }
  
  public void testTransformSimpleXmlToJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_XML_INPUT);
    
    JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(DIRECTION.XML_TO_JSON);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    assertEquals(SIMPLE_JSON_OUTPUT, msg.getStringPayload());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new JsonXmlTransformService();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \nThe example JSON input for this could be\n" + JSON_INPUT + "\n"
        + "\n\nThis will generate XML output that looks similar to this (without the formatting...):" + "\n\n"
        + DEFAULT_XML_INPUT + "\n-->\n";
  }

}
