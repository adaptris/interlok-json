package com.adaptris.core.json.streaming;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonInputBuilderTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testBuild() {
    JsonStreamingInputFactory builder = new JsonStreamingInputFactory().withConfig(new JsonStreamingConfigBuilder());
    assertNotNull(builder.build());
  }

}
