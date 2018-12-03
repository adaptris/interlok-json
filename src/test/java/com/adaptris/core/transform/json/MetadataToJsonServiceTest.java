package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MetadataToJsonServiceTest extends ServiceCase {

  public MetadataToJsonServiceTest(final String name) {
    super(name);
  }

  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "1234");
    ServiceCase.execute(new MetadataToJsonService(), msg);
    DocumentContext ctx = JsonPath.parse(msg.getContent());

    assertEquals("ABCDE", ctx.read("$.key1"));
    assertEquals("1234", ctx.read("$.key2"));
  }

  @Test
  public void testDoServiceWithConvertNumerics() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "256.368480478703");
    ServiceCase.execute(new MetadataToJsonService().withConvertNumeric(true), msg);
    DocumentContext ctx = JsonPath.parse(msg.getContent());

    assertEquals("ABCDE", ctx.read("$.key1"));
    assertEquals(Double.valueOf(256.368480478703), ctx.read("$.key2"));
  }

  @Test
  public void testDoServiceFitler() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "1234");
    msg.addMetadata("skip", "1234");
    ServiceCase.execute(new MetadataToJsonService().withMetadataFilter(new RegexMetadataFilter().withIncludePatterns("key1", "key2")), msg);
    DocumentContext ctx = JsonPath.parse(msg.getContent());

    assertEquals("ABCDE", ctx.read("$.key1"));
    assertEquals("1234", ctx.read("$.key2"));
    try {
      ctx.read("$.skip");
      fail();
    } catch (PathNotFoundException expected){
      assertEquals("No results for path: $['skip']", expected.getMessage());
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new MetadataToJsonService();
  }
}
