package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.transform.TransformServiceExample;

public class JsonToXmlTransformServiceTest extends TransformServiceExample {

  public JsonToXmlTransformServiceTest(String name) {
    super(name);
  }

  static final String XML_OUTPUT = "<json><entry><location>Seattle</location><name>Production System</name></entry>"
      + "<entry><location>New York</location><name>R&amp;D sandbox</name></entry>"
      + "<notes>Some Notes</notes><version>0.5</version></json>";

  static final String JSON_INPUT = "{\n\"entry\":[\n" + "{\n\"location\":\"Seattle\"," + "\n\"name\":\"Production System\"},\n"
      + "{\"location\":\"New York\",\n" + "\"name\":\"R&D sandbox\"\n" + "}\n" + "],\n" + "\"notes\":\"Some Notes\",\n"
      + "\"version\":0.5\n" + "}";

  String json = "{\n  \"implementation_version\": \"597\",\n  \"vdcs\": [\n    {\n      \"name\": \"XRGY Virtual Data Center\",\n      \"uri\": \"/vdc\"\n    },\n    {\n      \"name\": \"R&D sandbox\",\n      \"uri\": \"/sandbox\"\n    }\n  ],\n  \"uri\": \"http://xrgy.cloud.sun.com/\",\n  \"specification_version\": [\n    \"0.5\"\n  ]\n}";

  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    SimpleJsonToXmlTransformService svc = new SimpleJsonToXmlTransformService();
    execute(svc, msg);
    assertEquals(XML_OUTPUT, msg.getStringPayload());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new SimpleJsonToXmlTransformService();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \nThe example JSON input for this could be\n" + JSON_INPUT + "\n"
        + "\n\nThis will generate XML output that looks similar to this (without the formatting...):" + "\n\n"
        + XmlToJsonTransformServiceTest.XML_INPUT + "\n-->\n";
  }

}
