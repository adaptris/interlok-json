package com.adaptris.core.services.splitter.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.interlok.util.CloseableIterable;

public class BatchedJsonArraySplitterTest extends SplitterServiceExample {

  @Test
  public void testSetBatchSize() {
    assertNull(createSplitterForTests().getBatchSize());
    assertEquals(10, createSplitterForTests().batchSize());
    assertNotNull(createSplitterForTests().withBatchSize(100).getBatchSize());
    assertEquals(100, createSplitterForTests().withBatchSize(100).batchSize());
    assertEquals(10, createSplitterForTests().withBatchSize(-1).batchSize());
  }

  @Test
  public void testWithBufferSize() {
    assertNull(createSplitterForTests().getBufferSize());
    assertEquals(8192, createSplitterForTests().bufferSize());
    assertNotNull(createSplitterForTests().withBufferSize(10).getBufferSize());
    assertEquals(10, createSplitterForTests().withBufferSize(10).bufferSize());
  }

  @Test
  public void testWithMessageFactory() {
    LargeJsonArraySplitter s = createSplitter().withMessageFactory(new DefaultMessageFactory());
    assertEquals(DefaultMessageFactory.class, s.getMessageFactory().getClass());
    assertNull(createSplitter().getMessageFactory());
  }

  @Test
  public void testSplitArray() throws Exception {
    BatchedJsonArraySplitter s = createSplitterForTests().withBatchSize(2);
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(s.splitMessage(src))) {
      for (AdaptrisMessage m : i) {
        count++;
        System.err.println(m.getContent());
      }
      assertEquals(2, count);
    }
  }

  @Test
  public void testSplitArray_EmptyArray() throws Exception {
    BatchedJsonArraySplitter s = createSplitterForTests().withBatchSize(2);
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("[]");
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(s.splitMessage(src))) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(0, count);
    }
  }

  @Test
  public void testSplitArray_BatchSizeExact() throws Exception {
    BatchedJsonArraySplitter s = createSplitterForTests().withBatchSize(4);
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(s.splitMessage(src))) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(1, count);
    }
  }

  @Test
  public void testSplitArray_BatchSizeExceeds() throws Exception {
    BatchedJsonArraySplitter s = createSplitterForTests();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonObjectSplitterTest.JSON_ARRAY);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(s.splitMessage(src))) {
      for (AdaptrisMessage m : i) {
        count++;
      }
      assertEquals(1, count);
    }
  }

  @Test
  public void testSplitNotJson() throws Exception {
    BatchedJsonArraySplitter s = createSplitterForTests();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      Iterable<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  BatchedJsonArraySplitter createSplitterForTests() {
    return new BatchedJsonArraySplitter();
  }

  @Override
  BatchedJsonArraySplitter createSplitter() {
    return new BatchedJsonArraySplitter().withBatchSize(2);
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n" + JsonObjectSplitterTest.JSON_ARRAY
        + "\n\nthis would create 2 new messages, 1 for every 2 elements" + "\n-->\n";
  }
}
