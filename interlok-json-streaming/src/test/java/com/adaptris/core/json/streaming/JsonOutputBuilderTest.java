package com.adaptris.core.json.streaming;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonOutputBuilderTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testBuild() {
    JsonStreamingOutputFactory builder = new JsonStreamingOutputFactory();
    assertNotNull(builder.build());
  }

}
