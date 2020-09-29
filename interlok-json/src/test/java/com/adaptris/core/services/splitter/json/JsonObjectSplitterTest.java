package com.adaptris.core.services.splitter.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.transform.json.JsonXmlJsonTest;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonObjectSplitterTest extends SplitterServiceExample {

  public static final String JSON_ARRAY =
      "[\n{\"colour\": \"red\",\"value\": \"#f00\"},\n"
      + "{\"colour\": \"green\",\"value\": \"#0f0\"},\n"
      + "{\"colour\": \"blue\",\"value\": \"#00f\"},"
      + "\n{\"colour\": \"black\",\"value\": \"#000\"}\n"
      + "]";

  @Test
  public void testSplitArray() throws Exception {
    JsonObjectSplitter s = new JsonObjectSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    assertEquals(4, s.splitMessage(src).size());
  }

  @Test
  public void testSplitObject() throws Exception {
    JsonObjectSplitter s = new JsonObjectSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonXmlJsonTest.JSON_INPUT);
    List<AdaptrisMessage> msgs = s.splitMessage(src);

    assertEquals(3, msgs.size());
    JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(msgs.get(0).getContent());
    assertTrue(jsonObj.containsKey("entry"));
    jsonObj = (JSONObject) JSONSerializer.toJSON(msgs.get(1).getContent());
    assertTrue(jsonObj.containsKey("notes"));
    jsonObj = (JSONObject) JSONSerializer.toJSON(msgs.get(2).getContent());
    assertTrue(jsonObj.containsKey("version"));
  }

  @Test
  public void testSplitEmptyObject() throws Exception {
    JsonObjectSplitter s = new JsonObjectSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("{}");
    List<AdaptrisMessage> msgs = s.splitMessage(src);

    assertEquals(1, msgs.size());
    JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(msgs.get(0).getContent());
    assertTrue(jsonObj.isEmpty());
  }

  @Test
  public void testSplitNotJson() throws Exception {
    JsonObjectSplitter s = new JsonObjectSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      List<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n"
        + JsonXmlJsonTest.JSON_INPUT
        + "\n\nthis would create 3 new messages, 1 each for 'entry', 'notes', 'version'." + "\nIf the incoming document is \n\n"
        + JSON_ARRAY + "\n\nthis would create 4 messages, 1 for each element" + "\n-->\n";
  }

  @Override
  JsonObjectSplitter createSplitter() {
    return new JsonObjectSplitter();
  }

}
