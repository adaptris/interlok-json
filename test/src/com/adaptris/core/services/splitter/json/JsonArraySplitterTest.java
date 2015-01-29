package com.adaptris.core.services.splitter.json;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.transform.json.JsonToXmlTransformServiceTest;

public class JsonArraySplitterTest extends SplitterServiceExample {

  public JsonArraySplitterTest(String name) {
    super(name);
  }

  public void testSplitArray() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    assertEquals(4, s.splitMessage(src).size());
  }

  public void testSplitNotArray() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonToXmlTransformServiceTest.JSON_INPUT);
    assertEquals(1, s.splitMessage(src).size());
  }

  public void testSplitNotJson() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      List<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Override
  JsonArraySplitter createSplitter() {
    return new JsonArraySplitter();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n" + JsonObjectSplitterTest.JSON_ARRAY
        + "\n\nthis would create 4 new messages, 1 for each element" + "\nIf the incoming document is\n\n"
        + JsonToXmlTransformServiceTest.JSON_INPUT
        + "\n\nthis would create a single message" + "\n-->\n";
  }
}
