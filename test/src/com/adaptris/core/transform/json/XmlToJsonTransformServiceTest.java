package com.adaptris.core.transform.json;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.transform.TransformServiceExample;

@SuppressWarnings("deprecation")
public class XmlToJsonTransformServiceTest extends TransformServiceExample {

  static final String XML_INPUT = "<json>\n" + "<version>0.5</version>\n" + "" + "<entry>\n"
      + "<name>Production System</name>\n" + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n"
      + "<name>R&amp;D sandbox</name>\n" + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n"
      + "</json>\n";

  static final String JSON_OUTPUT = "{\"entry\":[{\"location\":\"Seattle\",\"name\":\"Production System\"},{\"location\":\"New York\",\"name\":\"R&D sandbox\"}],\"notes\":\"Some Notes\",\"version\":0.5}";

  public XmlToJsonTransformServiceTest(String name) {
    super(name);
  }

  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(
        "<json><version>0.5</version>" + "<entry><name>Production System</name>" + "<location>Seattle</location>" + "</entry>"
            + "<entry><name>R&amp;D sandbox</name>" + "<location>New York</location>" + "</entry>" + "<notes>Some Notes</notes>"
            + "</json>");
    SimpleXmlToJsonTransformService svc = new SimpleXmlToJsonTransformService();
    execute(svc, msg);
    doAssertions(msg);
    // cannot do a raw comparison.
    // Due to http://docs.oracle.com/javase/8/docs/technotes/guides/collections/changes8.html
    // The iteration order has changed for HashMap.keySet()
    // assertEquals(JSON_OUTPUT, msg.getStringPayload());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new SimpleXmlToJsonTransformService();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \nThe example XML input for this could be\n" + XML_INPUT + "\n"
        + "\n\nThis will generate JSON output that looks similar to this (without the formatting):" + "\n\n"
        + JsonToXmlTransformServiceTest.JSON_INPUT + "\n-->\n";
  }

  public static void doAssertions(AdaptrisMessage msg) throws Exception {
    JSONObject obj = new JSONObject(msg.getStringPayload());
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
