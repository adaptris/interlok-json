package com.adaptris.core.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.text.NullPassThroughConverter;

public class JsonToMetadataTest extends ExampleServiceCase {

  private static String SAMPLE_JSON_CONTENT =
     "\"category\": \"fiction\","
    + "\"title\": \"The Lord of the Rings Trilogy\","
    + "\"price\": 22.99,"
    + "\"volumes\" : [1,2,3]";

  private static String JSON_START = "{";
  private static String JSON_END = "}";
  private static String NESTED_CONTENT = "\"title\":\"The Hobbit\"";

  private static String NESTED_OBJECT = "\"suggested\": " + JSON_START + NESTED_CONTENT + JSON_END;
  private static String SAMPLE_JSON = JSON_START + SAMPLE_JSON_CONTENT + "," + NESTED_OBJECT + JSON_END;

  @Test
  public void testSetNullConverter() throws Exception {
    JsonToMetadata service = new JsonToMetadata();
    assertNull(service.getNullConverter());
    service.setNullConverter(new NullPassThroughConverter());
    assertEquals(NullPassThroughConverter.class, service.getNullConverter().getClass());
    service.setNullConverter(null);
    assertNull(service.getNullConverter());
  }

  @Test
  public void testMetadataPrefix() throws Exception {
    JsonToMetadata service = new JsonToMetadata();
    assertNull(service.getMetadataPrefix());
    assertEquals("", service.metadataPrefix());
    service.setMetadataPrefix("_");
    assertEquals("_", service.getMetadataPrefix());
    assertEquals("_", service.metadataPrefix());
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SAMPLE_JSON);
    JsonToMetadata service = new JsonToMetadata();
    execute(service, msg);
    assertEquals("fiction", msg.getMetadataValue("category"));
    assertEquals("The Lord of the Rings Trilogy", msg.getMetadataValue("title"));
    assertEquals("22.99", msg.getMetadataValue("price"));
    assertEquals("[1,2,3]", msg.getMetadataValue("volumes"));
    assertEquals(JSON_START + NESTED_CONTENT + JSON_END, msg.getMetadataValue("suggested"));
  }

  @Test
  public void testService_NotJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    msg.clearMetadata();
    JsonToMetadata service = new JsonToMetadata();
    execute(service, msg);
    assertEquals(0, msg.getMessageHeaders().size());
  }

  @Test
  public void testService_JsonArray() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("[" + SAMPLE_JSON + "]");
    msg.clearMetadata();
    JsonToMetadata service = new JsonToMetadata();
    execute(service, msg);
    assertEquals(0, msg.getMessageHeaders().size());
  }

  @Override
  protected JsonToMetadata retrieveObjectForSampleConfig() {
    return new JsonToMetadata();
  }




}
