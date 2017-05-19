package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class JsonLargeArraySplitterTest extends SplitterServiceExample {

  public JsonLargeArraySplitterTest(String name) {
    super(name);
  }

  public void testSplitArray() throws Exception {
    LargeJsonArraySplitter s = new LargeJsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    for (AdaptrisMessage m : s.splitMessage(src)) {
      count++;
    }
    assertEquals(4, count);
  }

  public void testSplitNotJson() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      Iterable<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Override
  LargeJsonArraySplitter createSplitter() {
    return new LargeJsonArraySplitter();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n" + JsonObjectSplitterTest.JSON_ARRAY
        + "\n\nthis would create 4 new messages, 1 for each element" + "\n-->\n";
  }
}
