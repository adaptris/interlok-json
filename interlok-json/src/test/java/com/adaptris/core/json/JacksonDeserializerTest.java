package com.adaptris.core.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonDeserializerTest extends DeserializerCase {

  private JacksonJsonDeserializer s = new JacksonJsonDeserializer();

  @Test
  public void testValidObject() throws Exception {
    assertNotNull(s.deserialize(jsonObject));
    assertEquals(ObjectNode.class, s.deserialize(jsonObject).getClass());

  }

  @Test
  public void testInvalidObject() throws Exception {
    assertThrows(JsonProcessingException.class, ()->{
      s.deserialize(invalidJsonObj);
    }, "Failed, invalid Json object");
  }

  @Test
  public void testValidArray() throws Exception {
    assertNotNull(s.deserialize(jsonArray));
    assertEquals(ArrayNode.class, s.deserialize(jsonArray).getClass());

  }

  @Test
  public void testInvalidArray() throws Exception {
    assertThrows(JsonProcessingException.class, ()->{
      s.deserialize(invalidJsonArray);
    }, "Failed, invalid Json Array");
  }

  @Test
  public void testNotJson() throws Exception {
    assertThrows(JsonProcessingException.class, ()->{
      s.deserialize(notJson);
    }, "Failed, not Json");
  }

}
