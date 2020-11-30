package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import java.io.Reader;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonLinesTest {

  public static final String JSON_ARRAY =
      "[\n{\"colour\": \"red\",\"value\": \"#f00\"},\n"
      + "{\"colour\": \"green\",\"value\": \"#0f0\"},\n"
      + "{\"colour\": \"blue\",\"value\": \"#00f\"},"
      + "\n{\"colour\": \"black\",\"value\": \"#000\"}\n"
      + "]";

  public static final String JSON_LINES =
      "{\"colour\": \"red\",\"value\": \"#f00\"}\n"
      + "{\"colour\": \"green\",\"value\": \"#0f0\"}\n"
      + "{\"colour\": \"blue\",\"value\": \"#00f\"}\n"
      + "{\"colour\": \"black\",\"value\": \"#000\"}";

  private Configuration jsonConfig;

  @Before
  public void setUp() {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class))
        .build();

  }

  @Test
  public void testJsonLines_JsonArray() throws Exception {
    JsonLinesToJsonArray service = new JsonLinesToJsonArray();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_LINES);
    ExampleServiceCase.execute(service, msg);
    assertNotEquals(JSON_LINES, msg.getContent());
    // System.err.println(msg.getContent());
    assertTrue(msg.getContent().startsWith("["));
    ReadContext context = JsonPath.parse(msg.getContent(), jsonConfig);
    assertEquals("red", context.read("$[0].colour"));
    assertEquals("green", context.read("$[1].colour"));
    assertEquals("blue", context.read("$[2].colour"));
    assertEquals("black", context.read("$[3].colour"));
  }

  @Test(expected = ServiceException.class)
  public void testJsonLines_NotJson() throws Exception {
    JsonLinesToJsonArray service = new JsonLinesToJsonArray();
    AdaptrisMessage msg =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world\nhello world");
    ExampleServiceCase.execute(service, msg);
  }


  @Test
  public void testJsonArray_JsonLines() throws Exception {
    JsonArrayToJsonLines service = new JsonArrayToJsonLines();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    ExampleServiceCase.execute(service, msg);
    assertNotEquals(JSON_ARRAY, msg.getContent());
    // System.err.println(msg.getContent());
    try (Reader r = msg.getReader()) {
      List<String> lines = IOUtils.readLines(r);
      assertEquals(4, lines.size());
      assertEquals("red", JsonPath.parse(lines.get(0), jsonConfig).read("$.colour"));
      assertEquals("green", JsonPath.parse(lines.get(1), jsonConfig).read("$.colour"));
      assertEquals("blue", JsonPath.parse(lines.get(2), jsonConfig).read("$.colour"));
      assertEquals("black", JsonPath.parse(lines.get(3), jsonConfig).read("$.colour"));
    }
  }

  @Test(expected = ServiceException.class)
  public void testJsonArray_NotJson() throws Exception {
    JsonArrayToJsonLines service = new JsonArrayToJsonLines();
    AdaptrisMessage msg =
        AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world\nhello world");
    ExampleServiceCase.execute(service, msg);
  }
}
