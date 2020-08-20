package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class MetadataToJsonServiceTest extends ExampleServiceCase {


  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "1234");
    execute(new MetadataToJsonService(), msg);
    DocumentContext ctx = JsonPath.parse(msg.getContent());

    assertEquals("ABCDE", ctx.read("$.key1"));
    assertEquals("1234", ctx.read("$.key2"));
  }

  @Test
  public void testDoServiceWithConvertNumerics() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "256.368480478703");
    execute(new MetadataToJsonService().withConvertNumeric(true), msg);
    DocumentContext ctx = JsonPath.parse(msg.getContent());

    assertEquals("ABCDE", ctx.read("$.key1"));
    assertEquals(Double.valueOf(256.368480478703), ctx.read("$.key2"));
  }

  @Test
  public void testDoServiceFilter() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "ABCDE");
    msg.addMetadata("key2", "1234");
    msg.addMetadata("skip", "1234");
    execute(new MetadataToJsonService()
        .withMetadataFilter(new RegexMetadataFilter().withIncludePatterns("key1", "key2")), msg);
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

  @Test
  public void testDoService_NewLine() throws Exception {
    HashSet<MetadataElement> metadata =
        new HashSet<>(Arrays.asList(new MetadataElement("key1", "ABCDE"), new MetadataElement("key2", "1234")));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("", metadata);
    execute(new MetadataToJsonService().withAddTrailingNewline(false), msg);
    assertFalse(msg.getContent().endsWith(System.lineSeparator()));
    execute(new MetadataToJsonService().withAddTrailingNewline(true), msg);
    long newlineCount = msg.getContent().chars().filter(ch -> ch == '\n').count();
    assertEquals(1, newlineCount);
    assertTrue(msg.getContent().endsWith(System.lineSeparator()));
  }

  @Test
  public void testDoService_Exception() throws Exception {
    HashSet<MetadataElement> metadata =
        new HashSet<>(Arrays.asList(new MetadataElement("key1", "ABCDE"), new MetadataElement("key2", "1234")));
    AdaptrisMessage msg = new DefectiveMessageFactory(DefectiveMessageFactory.WhenToBreak.METADATA_GET).newMessage("", metadata);
    try {
      execute(new MetadataToJsonService().withAddTrailingNewline(false), msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new MetadataToJsonService();
  }
}
