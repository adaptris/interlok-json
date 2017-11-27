package com.adaptris.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.text.NullPassThroughConverter;
import com.adaptris.util.text.NullToEmptyStringConverter;
import com.adaptris.util.text.NullsNotSupportedConverter;

public class JsonUtilTest extends JsonUtil {
  private static final String OBJECT_CONTENT = "{ \"firstname\":\"alice\", \"lastname\":\"smith\", \"address\": null }";

  private static final String NESTED_CONTENT =
      " { \"firstname\":\"alice\", \"lastname\":\"smith\", \"address\": { \"address\" : \"Buckingham Palace\", \"postcode\":\"SW1A 1AA\"}}";

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testMapifyJson() throws Exception {
    AdaptrisMessage basic = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    AdaptrisMessage nested = AdaptrisMessageFactory.getDefaultInstance().newMessage(NESTED_CONTENT);
    assertEquals("null", mapifyJson(basic).get("address"));
    assertEquals("{\"address\":\"Buckingham Palace\",\"postcode\":\"SW1A 1AA\"}", mapifyJson(nested).get("address"));
  }

  @Test
  public void testMapifyJson_WithNullPassThrough() throws IOException {
    AdaptrisMessage basic = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    AdaptrisMessage nested = AdaptrisMessageFactory.getDefaultInstance().newMessage(NESTED_CONTENT);
    assertNull(mapifyJson(basic, new NullPassThroughConverter()).get("address"));
    assertEquals("{\"address\":\"Buckingham Palace\",\"postcode\":\"SW1A 1AA\"}",
        mapifyJson(nested, new NullPassThroughConverter()).get("address"));
  }

  @Test
  public void testMapifyJson_WithNullToEmptyString() throws IOException {
    AdaptrisMessage basic = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    AdaptrisMessage nested = AdaptrisMessageFactory.getDefaultInstance().newMessage(NESTED_CONTENT);
    assertEquals("", mapifyJson(basic, new NullToEmptyStringConverter()).get("address"));
    assertEquals("{\"address\":\"Buckingham Palace\",\"postcode\":\"SW1A 1AA\"}",
        mapifyJson(nested, new NullToEmptyStringConverter()).get("address"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMapifyJson_WithNullNotSupported() throws IOException {
    AdaptrisMessage basic = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    AdaptrisMessage nested = AdaptrisMessageFactory.getDefaultInstance().newMessage(NESTED_CONTENT);
    assertEquals("{\"address\":\"Buckingham Palace\",\"postcode\":\"SW1A 1AA\"}",
        mapifyJson(nested, new NullsNotSupportedConverter()).get("address"));
    // Should throw UOE.
    mapifyJson(basic, new NullsNotSupportedConverter());
  }
}
