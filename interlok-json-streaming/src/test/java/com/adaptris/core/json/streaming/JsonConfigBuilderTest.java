package com.adaptris.core.json.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JsonConfigBuilderTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testBuild() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertNotNull(builder.build());
  }

  @Test
  public void testAutoArray() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertFalse(builder.autoArray());
    assertTrue(builder.withAutoArray(Boolean.TRUE).autoArray());
  }

  @Test
  public void testAutoPrimitive() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertFalse(builder.autoPrimitive());
    assertTrue(builder.withAutoPrimitive(Boolean.TRUE).autoPrimitive());
  }

  @Test
  public void testNamespaceDeclarations() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertTrue(builder.namespaceDeclarations());
    assertTrue(builder.withNamespaceDeclarations(Boolean.TRUE).namespaceDeclarations());
  }

  @Test
  public void testNamespaceSeparator() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertEquals(':', builder.namespaceSeparator());
    assertEquals('&', builder.withNamespaceSeparator('&').namespaceSeparator());
  }

  @Test
  public void testPrettyPrint() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertFalse(builder.prettyPrint());
    assertTrue(builder.withPrettyPrint(Boolean.TRUE).prettyPrint());
  }

  @Test
  public void testVirtualRoot() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertNull(builder.virtualRoot());
    assertNotNull(builder.withVirtualRoot("root").virtualRoot());
  }

  @Test
  public void testRepairingNamespaces() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertFalse(builder.repairingNamespaces());
    assertTrue(builder.withRepairingNamespaces(Boolean.TRUE).repairingNamespaces());
  }

  @Test
  public void testNamespaceMappings() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertNull(builder.namespaceMappings());
    KeyValuePairSet mappings = new KeyValuePairSet();
    mappings.addKeyValuePair(new KeyValuePair("hello", "world"));
    assertNotNull(builder.withNamespaceMappings(mappings).namespaceMappings());
  }

  @Test
  public void testMultiplePI() {
    JsonStreamingConfigBuilder builder = new JsonStreamingConfigBuilder();
    assertTrue(builder.multiplePI());
    assertFalse(builder.withMultipleProcessingInstruction(Boolean.FALSE).multiplePI());

  }

}
