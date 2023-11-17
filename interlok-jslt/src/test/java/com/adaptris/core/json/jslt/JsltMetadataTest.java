package com.adaptris.core.json.jslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.fasterxml.jackson.databind.JsonNode;

public class JsltMetadataTest extends ExampleServiceCase {

  private static final String JSLT_INPUT_JSON = "{\"foo\" : {\"bar\" : [1,2,3,4,5]}}";

  private static final String JSLT_QUERY_ARRAY = ".foo.bar"; // return [1,2,3,4,5]
  private static final String JSLT_QUERY_ELEMENT = ".foo.bar[0]"; // return 1...


  @Override
  protected JsltMetadataService retrieveObjectForSampleConfig() {
    return new JsltMetadataService().withQueries(
        new JsltQueryToMetadata().withKey("dest-metadata-key").withExpression(".jslt.expression"),
        new JsltQueryToObjectMetadata().withKey("dest-object-key").withExpression(".jslt.expression"));
  }

  @Test
  public void testTransform_NotJson() throws Exception {
    JsltMetadataService service = new JsltMetadataService().withQueries(
        new JsltQueryToMetadata().withKey("value").withExpression(JSLT_QUERY_ELEMENT),
        new JsltQueryToMetadata().withKey("array").withExpression(JSLT_QUERY_ARRAY));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello world", "UTF-8");
    assertThrows(ServiceException.class, ()->{
      execute(service, msg);
    }, "Failed, not Json"); 
  }

  @Test
  public void testExtractMetadata() throws Exception {
    JsltMetadataService service =
        new JsltMetadataService().withQueries(
            new JsltQueryToMetadata().withKey("value").withExpression(JSLT_QUERY_ELEMENT),
            new JsltQueryToMetadata().withKey("array").withExpression(JSLT_QUERY_ARRAY));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(JSLT_INPUT_JSON, "UTF-8");
    execute(service, msg);
    assertEquals("1", msg.getMetadataValue("value"));
    assertEquals("[1,2,3,4,5]", msg.getMetadataValue("array"));
  }

  @Test
  public void testExtractObjectMetadata() throws Exception {
    JsltMetadataService service = new JsltMetadataService().withQueries(
        new JsltQueryToMetadata().withKey("value").withExpression(JSLT_QUERY_ELEMENT),
        new JsltQueryToObjectMetadata().withKey("array").withExpression(JSLT_QUERY_ARRAY));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(JSLT_INPUT_JSON, "UTF-8");
    execute(service, msg);
    assertEquals("1", msg.getMetadataValue("value"));
    Object o = msg.getObjectHeaders().get("array");
    assertNotNull(o);
    assertTrue(JsonNode.class.isAssignableFrom(o.getClass()));
  }

}
