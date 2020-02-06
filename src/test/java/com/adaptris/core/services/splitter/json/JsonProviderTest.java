package com.adaptris.core.services.splitter.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.util.CloseableIterable;

public class JsonProviderTest {

  private static final String LINES = "{\"key\":\"value\"}\r\n\r\n{\"key\":\"value\"}\r\n";
  private static final String ARRAY = "[{\"key\":\"value\"}, {\"key\":\"value\"}]";
  private static final String OBJECT = "{\"key\":\"value\", \"key2\":\"value2\"}\r\n";
  @Test
  public void testJsonLines() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINES);
    try (CloseableIterable<AdaptrisMessage> itr = JsonProvider.JsonStyle.JSON_LINES.createIterator(msg)) {
      int count = 0;
      for (AdaptrisMessage m : itr) {
        count++;
        Map<String, String> map = JsonUtil.mapifyJson(m);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
      }
      assertEquals(2, count);
    }
  }


  @Test
  public void testJsonArray() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY);
    try (CloseableIterable<AdaptrisMessage> itr = JsonProvider.JsonStyle.JSON_ARRAY.createIterator(msg)) {
      int count = 0;
      for (AdaptrisMessage m : itr) {
        count++;
        Map<String, String> map = JsonUtil.mapifyJson(m);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
      }
      assertEquals(2, count);
    }
  }

  @Test
  public void testJsonObject() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT);
    try (CloseableIterable<AdaptrisMessage> itr = JsonProvider.JsonStyle.JSON_OBJECT.createIterator(msg)) {
      int count = 0;
      for (AdaptrisMessage m : itr) {
        count++;
        Map<String, String> map = JsonUtil.mapifyJson(m);
        assertEquals(1, map.size());
      }
      assertEquals(2, count);
    }
  }
}
