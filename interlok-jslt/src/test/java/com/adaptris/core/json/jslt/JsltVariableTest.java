package com.adaptris.core.json.jslt;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.metadata.FixedValuesMetadataFilter;
import com.adaptris.util.KeyValuePair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JsltVariableTest {

  private AdaptrisMessageFactory factory = new DefaultMessageFactory();



  @Test
  public void testFixedVariables_Empty() throws Exception {
    JsltFixedVariables builder = new JsltFixedVariables();
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(0, vars.size());
  }

  @Test
  public void testFixedVariables() throws Exception {
    JsltFixedVariables builder =
        new JsltFixedVariables().withVariables(new KeyValuePair("key", "value"));
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(1, vars.size());
    assertEquals(TextNode.class, vars.get("key").getClass());
  }

  @Test
  public void testMetadataVariables_Empty() throws Exception {
    JsltMetadataVariables builder = new JsltMetadataVariables();
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(0, vars.size());
  }

  // Probably a bit cheaty since FixedValuesMetadataFilter means the behaviour is exactly the
  // same as FixedVariables...
  @Test
  public void testMetadataVariables() throws Exception {
    JsltMetadataVariables builder =
        new JsltMetadataVariables().withFilter(
            new FixedValuesMetadataFilter().withMetadata(new KeyValuePair("key", "value")));
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(1, vars.size());
    assertEquals(TextNode.class, vars.get("key").getClass());
  }

  @Test
  public void testObjectVariables_NoRegexp() throws Exception {
    JsltObjectVariables builder = new JsltObjectVariables();
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(0, vars.size());
  }

  @Test
  public void testObjectVariables_NoMatch() throws Exception {
    JsltObjectVariables builder = new JsltObjectVariables().withObjectMetadataKeyRegexp("^.*_metadata$");
    AdaptrisMessage msg = factory.newMessage();
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(0, vars.size());
  }

  @Test(expected = ClassCastException.class)
  public void testObjectVariables_BadCast() throws Exception {
    JsltObjectVariables builder =
        new JsltObjectVariables().withObjectMetadataKeyRegexp("^.*_metadata$");
    AdaptrisMessage msg = factory.newMessage();
    msg.addObjectHeader("obj_metadata", new Object());
    Map<String, JsonNode> vars = builder.build(msg);
  }

  @Test
  public void testObjectVariables() throws Exception {
    JsltObjectVariables builder =
        new JsltObjectVariables().withObjectMetadataKeyRegexp("^.*_metadata$");
    AdaptrisMessage msg = factory.newMessage();
    msg.addObjectHeader("obj_metadata", TextNode.valueOf("hello world"));
    Map<String, JsonNode> vars = builder.build(msg);
    assertEquals(1, vars.size());
    assertEquals(TextNode.class, vars.get("obj_metadata").getClass());
  }
}
