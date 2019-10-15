package com.adaptris.core.transform.json.jolt;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author mwarman
 */
public class NullToArrayTest {

  @Test
  public void transform() {
    NullToArray nullToArray =  new NullToArray();
    Object result = nullToArray.transform(null);
    assertTrue(result instanceof ArrayList);
    assertEquals(0, ((ArrayList)result).size());
  }

  @Test
  public void transformUntouched() {
    NullToArray nullToArray =  new NullToArray();
    Map<String, Object> input = new HashMap<>();
    input.put("key", "value");
    Object result = nullToArray.transform(input);
    assertEquals(input, result);
  }
}