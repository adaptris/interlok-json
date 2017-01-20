package com.adaptris.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.path.JsonPathServiceTest;
import com.adaptris.interlok.InterlokException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonPathDataInputParameterTest {

  private static final String JSON_ARRAY = "["
      + "{\"author\":\"Herman Melville\",\"price\":8.99,\"isbn\":\"0-553-21311-3\",\"category\":\"fiction\",\"title\":\"Moby Dick\"},"
      + "{\"author\":\"J. R. R. Tolkien\",\"price\":22.99,\"isbn\":\"0-395-19395-8\",\"category\":\"fiction\",\"title\":\"The Lord of the Rings\"}"
      + "]";

  private static final String JSON_OBJECT = "{\"author\":\"J. R. R. Tolkien\",\"price\":22.99,\"isbn\":\"0-395-19395-8\",\"category\":\"fiction\",\"title\":\"The Lord of the Rings\"}";

  @Test
  public void testExtractFromArray() throws Exception {
    JsonPathDataInputParameter param = new JsonPathDataInputParameter("$[1].author");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    assertEquals("J. R. R. Tolkien", param.extract(msg));
  }

  @Test
  public void testExtractFromObject() throws Exception {
    JsonPathDataInputParameter param = new JsonPathDataInputParameter("$.author");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_OBJECT);
    assertEquals("J. R. R. Tolkien", param.extract(msg));
  }

  @Test
  public void testExtract_NotJson() throws Exception {
    try {
      JsonPathDataInputParameter param = new JsonPathDataInputParameter("$.store.book");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      String s = param.extract(msg);
      fail();
    }
    catch (InterlokException expected) {

    }
  }

  @Test
  public void testExtract_JSONObject() throws Exception {
    // Should return a JSONObject which we just toString it
    JsonPathDataInputParameter param = new JsonPathDataInputParameter("$.store.book[0]");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonPathServiceTest.sampleJsonContent());
    String result = param.extract(msg);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(result, jsonConfig);
    assertEquals("Nigel Rees", context.read("$.author"));
  }

  @Test
  public void testExtract_JSONArray() throws Exception {
    // Should return a JSONArray which we just toString it
    JsonPathDataInputParameter param = new JsonPathDataInputParameter("$.store.book");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonPathServiceTest.sampleJsonContent());
    String result = param.extract(msg);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(result, jsonConfig);
    assertEquals("Nigel Rees", context.read("$[0].author"));
  }

  @Test
  public void testExtract_Primitive() throws Exception {
    // Should return a 8.95 which we just toString it
    JsonPathDataInputParameter param = new JsonPathDataInputParameter("$.store.book[0].price");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JsonPathServiceTest.sampleJsonContent());
    String result = param.extract(msg);
    assertEquals("8.95", result);
  }
}
