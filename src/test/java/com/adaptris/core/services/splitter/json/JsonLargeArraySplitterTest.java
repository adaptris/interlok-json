package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.CloseableIterable;

public class JsonLargeArraySplitterTest extends SplitterServiceExample {

  public JsonLargeArraySplitterTest(String name) {
    super(name);
  }

  public void testWithBufferSize() {
    assertNull(createSplitter().getBufferSize());
    assertEquals(8192, createSplitter().bufferSize());
    assertNotNull(createSplitter().withBufferSize(10).getBufferSize());
    assertEquals(10, createSplitter().withBufferSize(10).bufferSize());
  }

  public void testWithMessageFactory() {
    LargeJsonArraySplitter s = createSplitter().withMessageFactory(new DefaultMessageFactory());
    assertEquals(DefaultMessageFactory.class, s.getMessageFactory().getClass());
    assertNull(createSplitter().getMessageFactory());
  }

  public void testSplitArray() throws Exception {
    LargeJsonArraySplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(4, count);
    }
  }

  public void testRemove() throws Exception {
    LargeJsonArraySplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      i.iterator().remove();
      fail();
    } catch (UnsupportedOperationException expected) {

    }
  }

  public void testSplitArray_EmptyArray() throws Exception {
    LargeJsonArraySplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("[]");
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(0, count);
    }
  }

  public void testSplitNotJson() throws Exception {
    LargeJsonArraySplitter s = createSplitter();
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
