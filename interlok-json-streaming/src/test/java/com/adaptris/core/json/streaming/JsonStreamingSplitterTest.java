package com.adaptris.core.json.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.json.TransformationDirection;
import com.adaptris.interlok.util.CloseableIterable;

/**
 * @author mwarman
 */
public class JsonStreamingSplitterTest {

  @Test
  public void testDoService() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload());
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    JSONAssert.assertEquals("{\"document\":{\"nested\":0,\"array\":[5,6,7],\"object\":{\"something\":true}}}",
        list.get(0).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":{\"nested\":1,\"value\":\"Another\"}}",
        list.get(1).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":{\"nested\":2}}",
        list.get(2).getContent(), JSONCompareMode.STRICT_ORDER);
  }

  @Test
  public void testDoServiceWrapWithArray() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    splitter.setWrapWithArray(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload());
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    JSONAssert.assertEquals("{\"document\":[{\"nested\":0,\"array\":[5,6,7],\"object\":{\"something\":true}}]}",
        list.get(0).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":[{\"nested\":1,\"value\":\"Another\"}]}",
        list.get(1).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":[{\"nested\":2}]}",
        list.get(2).getContent(), JSONCompareMode.STRICT_ORDER);
  }

  @Test
  public void testDoServiceInception() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document/nested");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload());
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      JSONAssert.assertEquals(String.format("{\"nested\":%s}",i), list.get(i).getContent(), JSONCompareMode.STRICT_ORDER);
    }
  }

  @Test
  public void testDoServiceTypeLoss() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    splitter.setJsonStreamingConfig(new JsonStreamingConfigBuilder().withAutoPrimitive(false));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("{ \"envelope\" : { \"document\": [ { \"nested\" : \"0\"}, { \"nested\" : \"1\"}, { \"nested\" : \"3\"} ] } }");
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    JSONAssert.assertEquals("{\"document\":{\"nested\":\"0\"}}",
            list.get(0).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":{\"nested\":\"1\"}}",
            list.get(1).getContent(), JSONCompareMode.STRICT_ORDER);
    JSONAssert.assertEquals("{\"document\":{\"nested\":\"3\"}}",
            list.get(2).getContent(), JSONCompareMode.STRICT_ORDER);
  }

  @Test
  public void testSplit_NotFound() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document/x");
    splitter.setSuppressPathNotFound(Boolean.FALSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload());
    assertThrows(CoreException.class, ()->{
      splitter.splitMessage(msg);
    }, "Failed split with not found");
  }

  @Test
  public void testSplit_Remove() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload());
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(splitter.splitMessage(msg))) {
      assertThrows(UnsupportedOperationException.class, ()->{
        i.iterator().remove();
      }, "Failed split with unsupported operation");
    };
  }

  @Test
  public void testBufferSize() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    assertEquals(8192, splitter.bufferSize());
    assertNull(splitter.getBufferSize());
    splitter.setBufferSize(1024);
    assertEquals(1024, splitter.bufferSize());
  }

  @Test
  public void testJsonStreamingConfigBuilder() throws Exception {
    JsonStreamingSplitter splitter = new JsonStreamingSplitter("/envelope/document");
    assertTrue(splitter.getJsonStreamingConfig().autoArray());
    assertTrue(splitter.getJsonStreamingConfig().autoPrimitive());
    splitter = new JsonStreamingSplitter("/envelope/document");
    splitter.setJsonStreamingConfig(new JsonStreamingConfigBuilder().withAutoPrimitive(false).withAutoArray(false));
    assertFalse(splitter.getJsonStreamingConfig().autoArray());
    assertFalse(splitter.getJsonStreamingConfig().autoPrimitive());
  }

  private static List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if (iter instanceof List) {
      return (List<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    } catch (IOException e) {
    }
    return result;
  }

  private String payload() {
    return "{\n" +
        "  \"envelope\": {\n" +
        "    \"document\": [\n" +
        "      {\n" +
        "        \"nested\": 0,\n" +
        "        \"array\": [\n" +
        "          5,\n" +
        "          6,\n" +
        "          7\n" +
        "        ],\n" +
        "        \"object\": {\n" +
        "          \"something\": true\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"nested\": 1,\n" +
        "        \"value\": \"Another\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"nested\": 2\n" +
        "      }\n" +
        "    ]\n" +
        "  }\n" +
        "}";
  }
}
