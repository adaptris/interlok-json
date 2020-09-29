package com.adaptris.core.json.streaming;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonOutputBuilderTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testBuild() {
    JsonStreamingOutputFactory builder = new JsonStreamingOutputFactory();
    assertNotNull(builder.build());
  }

}
