package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.CloseableIterable;

public class LargeJsonArrayPathSplitterTest extends SplitterServiceExample {


  public static final String JSON_ARRAY =
      "{ \"status\" :\"ok\", \n" +
          "\"colours\" : [\n{\"colour\": \"red\",\"value\": \"#f00\"},\n"
          + "{\"colour\": \"green\",\"value\": \"#0f0\"},\n"
          + "{\"colour\": \"blue\",\"value\": \"#00f\"},"
          + "\n{\"colour\": \"black\",\"value\": \"#000\"}\n"
          + "], {\"colour\": \"red\",\"value\": \"#f00\"} }";


  public static final String TWO_LAYERS_JSON_ARRAY = "{\"status\":\"ok\",\"result\":{\"colours\":[{\"colour\":\"red\",\"value\":\"#f00\"},{\"colour\":\"green\",\"value\":\"#0f0\"},{\"colour\":\"blue\",\"value\":\"#00f\"},{\"colour\":\"black\",\"value\":\"#000\"}]}}";

  public LargeJsonArrayPathSplitterTest(String name) {
    super(name);
  }

  public void testWithPath() {
    assertNull(new LargeJsonArrayPathSplitter().getPath());
    assertNotNull(new LargeJsonArrayPathSplitter().withPath("/colours").getPath());
    assertEquals("/colours", new LargeJsonArrayPathSplitter().withPath("/colours").getPath());
  }

  public void testWithSuppressPathNotFound() {
    assertNull(createSplitter().getSuppressPathNotFound());
    assertFalse(createSplitter().suppressPathNotFound());
    assertTrue(createSplitter().withSuppressPathNotFound(true).suppressPathNotFound());
  }

  public void testWithSuppressPathNotAnArray() {
    assertNull(createSplitter().getSuppressPathNotAnArray());
    assertFalse(createSplitter().suppressPathNotAnArray());
    assertTrue(createSplitter().withSuppressPathNotAnArray(true).suppressPathNotAnArray());
  }

  public void testSplitArray() throws Exception {
    LargeJsonArrayPathSplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(4, count);
    }
  }

  public void testSplitTwoLayerArray() throws Exception {
    LargeJsonArrayPathSplitter s = new LargeJsonArrayPathSplitter().withPath("/result/colours");
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(TWO_LAYERS_JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(4, count);
    }
  }

  public void testRemove() throws Exception {
    LargeJsonArrayPathSplitter s = new LargeJsonArrayPathSplitter().withPath("/colours");
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      i.iterator().remove();
      fail();
    } catch (UnsupportedOperationException expected) {

    }
  }

  public void testSplitArrayEmptyArray() throws Exception {
    LargeJsonArrayPathSplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("{\"colours\" : [] }");
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(0, count);
    }
  }

  public void testSplitArrayNotArray() throws Exception {
    LargeJsonArrayPathSplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("{\"colours\" : {\"colour\": \"red\",\"value\": \"#f00\"} }");
    try {
      Iterable<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }


  public void testSplitNotJson() throws Exception {
    LargeJsonArrayPathSplitter s = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      Iterable<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testSplitNoMatch() throws Exception {
    LargeJsonArrayPathSplitter s = new LargeJsonArrayPathSplitter().withPath("/nomatch");
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    try {
      Iterable<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testSplitNoMatchSuppressPathNotFound() throws Exception {
    LargeJsonArrayPathSplitter s = new LargeJsonArrayPathSplitter().withPath("/nomatch").withSuppressPathNotFound(true);
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
        System.out.println(m.getPayloadForLogging());
      }
      assertEquals(0, count);
    }
  }

  public void testSplitNoMatchSuppressPathNotAnArray() throws Exception {
    LargeJsonArrayPathSplitter s = createSplitter().withSuppressPathNotAnArray(true);
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("{\"colours\" : {\"colour\": \"red\",\"value\": \"#f00\"} }");
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = s.splitMessage(src)) {
      for (AdaptrisMessage m : i) {
        count++;
        System.out.println(m.getPayloadForLogging());
      }
      assertEquals(0, count);
    }
  }

  @Override
  LargeJsonArrayPathSplitter createSplitter() {
    return new LargeJsonArrayPathSplitter().withPath("/colours");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n" + JSON_ARRAY+ "\n\nAnd a path of /colours"
        + "\n\nthis would create 4 new messages, 1 for each element" + "\n-->\n";
  }
}
