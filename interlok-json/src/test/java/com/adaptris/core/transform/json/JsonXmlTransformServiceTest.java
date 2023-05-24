package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;
import com.adaptris.util.text.xml.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JsonXmlTransformServiceTest extends TransformServiceExample {


  // Input/output for Default Xml -> JSON transformation
  static final String DEFAULT_XML_INPUT = "<xml><version>0.5</version>\n" + "" + "<entry>\n" + "<name>Production System</name>\n"
      + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n" + "<name>R&amp;D sandbox</name>\n"
      + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</xml>";
  static final String JSON_OUTPUT =
      "{\"version\":\"0.5\",\"entry\":[{\"name\":\"Production System\",\"location\":\"Seattle\"},{\"name\":\"R&D sandbox\",\"location\":\"New York\"}],\"notes\":\"Some Notes\"}";

  // Input/output for Default and Simple JSON -> Xml transformation
  static final String JSON_INPUT =
      "{\n\"entry\":[\n" + "{\n\"location\":\"Seattle\"," + "\n\"name\":\"Production System\"},\n" + "{\"location\":\"New York\",\n"
          + "\"name\":\"R&D sandbox\"\n" + "}\n" + "],\n" + "\"notes\":\"Some Notes\",\n" + "\"version\":0.5\n" + "}";
  static final String DEFAULT_XML_OUTPUT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<o><entry class=\"array\"><e class=\"object\">"
          + "<location type=\"string\">Seattle</location><name type=\"string\">Production System</name></e><e class=\"object\">"
          + "<location type=\"string\">New York</location><name type=\"string\">R&amp;D sandbox</name></e></entry><notes type=\"string\">Some Notes</notes>"
          + "<version type=\"number\">0.5</version></o>\r\n";
  static final String SIMPLE_XML_OUTPUT =
      "<json><entry><location>Seattle</location><name>Production System</name></entry><entry><location>New York</location>"
          + "<name>R&amp;D sandbox</name></entry><notes>Some Notes</notes><version>0.5</version></json>";

  // Input/output for Simple Xml -> JSON transformation
  static final String SIMPLE_XML_INPUT = "<json><version>0.5</version>\n" + "" + "<entry>\n" + "<name>Production System</name>\n"
      + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n" + "<name>R&amp;D sandbox</name>\n"
      + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</json>";

  static final String SIMPLE_JSON_OUTPUT =
      "{\"entry\":[{\"location\":\"Seattle\",\"name\":\"Production System\"},{\"location\":\"New York\",\"name\":\"R&D sandbox\"}]"
          + ",\"notes\":\"Some Notes\",\"version\":0.5}";

  static final String ARRAY_JSON_INPUT =
      "[ { \"type\": \"Tfl.Api.Presentation.Entities.Line, Tfl.Api.Presentation.Entities\", " + "\"id\": \"victoria\", "
          + "\"name\": \"Victoria\", " + "\"modeName\": \"tube\", " + "\"created\": \"2015-07-23T14:35:19.787\", "
          + "\"modified\": \"2015-07-23T14:35:19.787\", " + "\"lineStatuses\": [], " + "\"routeSections\": [] }]";

  static final String BAD_JSON_INPUT =
      "[ { \"$type\": \"Tfl.Api.Presentation.Entities.Line, Tfl.Api.Presentation.Entities\", " + "\"id\": \"victoria\", "
          + "\"name\": \"Victoria\", " + "\"modeName\": \"tube\", " + "\"created\": \"2015-07-23T14:35:19.787\", "
          + "\"modified\": \"2015-07-23T14:35:19.787\", " + "\"lineStatuses\": [], " + "\"routeSections\": [] }]";

  static final String LONGER_THAN_INT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<outputresult class=\"object\">\n"
        + "<systemresponse class=\"object\">\n<records class=\"array\">\n<responsearrayelement class=\"object\">\n"
        + "<record type=\"number\">1</record>\n<resultid type=\"number\">123456789112</resultid>\n"
        + "<runid type=\"number\">12345</runid>\n</responsearrayelement>\n</records>\n</systemresponse>\n</outputresult>";
  static final String LONGER_THAN_INT_JSON = "{\"record\":1,\"resultid\":123456789112,\"runid\":12345}";
  static final String LONGER_THAN_INT_JSON_OUTPUT = "[[[" + LONGER_THAN_INT_JSON + "]]]";
  static final String LONGER_THAN_INT_XML_OUTPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<outputresult><record type=\"number\">1</record><resultid type=\"number\">123456789112</resultid><runid type=\"number\">12345</runid></outputresult>\r\n";

  @Test
  public void testTransformToXml() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService().withDriver(new JsonlibTransformationDriver()).withDirection(TransformationDirection.JSON_TO_XML);
    execute(svc, msg);
    assertEquals(DEFAULT_XML_OUTPUT, msg.getContent());
    execute(svc, AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_JSON_INPUT));
  }

  @Test
  public void testTransformToXml_StripIllegalXmlElement() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(BAD_JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService().withDriver(new JsonlibTransformationDriver()).withDirection(TransformationDirection.JSON_TO_XML);
    execute(svc, msg);
    System.err.println(msg.getContent());
  }

  @Test
  public void testTransformToXml_ArrayNotObject() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService().withDriver(new JsonObjectTransformationDriver()).withDirection(TransformationDirection.JSON_TO_XML);
    try {
      // Shouldn't parse because JsonArray input isn't valid.
      execute(svc, msg);
      fail();
    } catch (final ServiceException expected) {
    }
    svc.setDriver(new JsonArrayTransformationDriver());
    // This should be OK.
    execute(svc, msg);
  }

  @Test
  public void testTransformToXml_ObjectNotArray() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService().withDriver(new JsonArrayTransformationDriver()).withDirection(TransformationDirection.JSON_TO_XML);
    try {
      execute(svc, msg);
      fail();
    } catch (final ServiceException expected) {
    }
    svc.setDriver(new JsonObjectTransformationDriver());
    // This should be OK.
    execute(svc, msg);
  }

  @Test
  public void testTransformToJson() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_XML_INPUT);

    final JsonXmlTransformService svc = new JsonXmlTransformService().withDriver(new JsonlibTransformationDriver());
    svc.setDirection(TransformationDirection.XML_TO_JSON);
    execute(svc, msg);
    assertEquals(JSON_OUTPUT, msg.getContent());
  }

  @Test
  public void testTransformJsonToSimpleXml() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    doXmlAssertions(msg);
  }

  @Test
  public void testTransformSimpleXmlToJson() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_XML_INPUT);

    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.XML_TO_JSON);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    doJsonAssertions(msg);
  }

  @Test
  public void testLongIntegerValuesXmlToJson() throws Exception
  {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(LONGER_THAN_INT_XML);
    final JsonXmlTransformService service = new JsonXmlTransformService();
    service.setDirection(TransformationDirection.XML_TO_JSON);
    JsonObjectTransformationDriver driver = new JsonObjectTransformationDriver();
    driver.setRootName("outputresult");
    driver.setArrayName("responsearray");
    driver.setElementName("responsearrayelement");
    service.setDriver(driver);
    execute(service, message);
    assertEquals(LONGER_THAN_INT_JSON_OUTPUT, message.getContent());
  }

  @Test
  public void testLongIntegerValuesJsonToXml() throws Exception
  {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(LONGER_THAN_INT_JSON);
    final JsonXmlTransformService service = new JsonXmlTransformService();
    service.setDirection(TransformationDirection.JSON_TO_XML);
    JsonObjectTransformationDriver driver = new JsonObjectTransformationDriver();
    driver.setRootName("outputresult");
    driver.setArrayName("responsearray");
    driver.setElementName("responsearrayelement");
    service.setDriver(driver);
    execute(service, message);
    assertEquals(LONGER_THAN_INT_XML_OUTPUT, message.getContent());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new JsonXmlTransformService();
  }

  @Override
  protected String getExampleCommentHeader(final Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \nThe example JSON input for this could be\n" + JSON_INPUT + "\n"
        + "\n\nThis will generate XML output that looks similar to this (without the formatting...):" + "\n\n" + DEFAULT_XML_INPUT
        + "\n-->\n";
  }

  public static void doXmlAssertions(AdaptrisMessage msg) throws Exception {
    Document d = XmlHelper.createDocument(msg,
        DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(false));
    XPath xp = new XPath();
    assertEquals("Seattle", xp.selectSingleTextItem(d, "/json/entry[1]/location"));
    assertEquals("New York", xp.selectSingleTextItem(d, "/json/entry[2]/location"));
  }

  public static void doJsonAssertions(AdaptrisMessage msg) throws Exception {
    JSONObject obj = new JSONObject(msg.getContent());
    List<String> names = Arrays.asList(JSONObject.getNames(obj));
    assertTrue(names.contains("entry"));
    JSONArray array = obj.getJSONArray("entry");
    assertEquals(2, array.length());
    JSONObject seattle = (JSONObject) array.get(0);
    assertEquals("Seattle", seattle.getString("location"));
    JSONObject newyork = (JSONObject) array.get(1);
    assertEquals("New York", newyork.getString("location"));

  }

}
