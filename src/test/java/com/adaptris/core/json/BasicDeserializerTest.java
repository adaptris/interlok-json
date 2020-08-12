package com.adaptris.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class BasicDeserializerTest extends DeserializerCase {

  private BasicJsonDeserializer s = new BasicJsonDeserializer();

  @Test
  public void testValidObject() throws Exception {
    assertNotNull(s.deserialize(jsonObject));
    assertEquals(JSONObject.class, s.deserialize(jsonObject).getClass());

  }

  // Should coerce
  @Test
  public void testInvalidObject() throws Exception {
    assertNotNull(s.deserialize(jsonObject));
    assertEquals(JSONObject.class, s.deserialize(jsonObject).getClass());
  }

  @Test
  public void testValidArray() throws Exception {
    assertNotNull(s.deserialize(jsonArray));
    assertEquals(JSONArray.class, s.deserialize(jsonArray).getClass());
  }

  public void testInvalidArray() throws Exception {
    assertNotNull(s.deserialize(jsonArray));
    assertEquals(JSONArray.class, s.deserialize(jsonArray).getClass());
  }

  @Test(expected = JSONException.class)
  public void testNotJson() throws Exception {
    s.deserialize(notJson);
  }

}
