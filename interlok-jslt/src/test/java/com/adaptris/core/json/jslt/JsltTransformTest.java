package com.adaptris.core.json.jslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.EnumSet;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.KeyValuePair;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsltTransformTest extends ExampleServiceCase {

  private static final String JSLT_INPUT_KEY = "jslt.input.file";
  private static final String JSLT_TRANSFORM_KEY = "jslt.transform.url";
  private static final String JSLT_TRANSFORM__WITH_VARS_KEY = "jslt.transform.vars.url";

  private static final String JSLT_VAR_OBJECT = "object_metadata_key";
  private static final String JSLT_VAR_METADATA = "text_metadata_key";
  private static final String JSLT_VAR_FIXED = "fixed_metadata_key";

  private Configuration jsonConfig = new Configuration.ConfigurationBuilder()
      .jsonProvider(new JsonSmartJsonProvider()).mappingProvider(new JacksonMappingProvider())
      .options(EnumSet.noneOf(Option.class)).build();

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new JsltTransformService().withUrl("file:///path/to/my/jslt.jslt")
        .withVariables(new JsltVariables().withVariableBuilders(
            new JsltMetadataVariables()
                .withFilter(new RegexMetadataFilter().withIncludePatterns("^.*_metadata_key$")),
            new JsltObjectVariables().withObjectMetadataKeyRegexp("^.*_metadata_key$"),
            new JsltFixedVariables().withVariables(
                new KeyValuePair("fixed_metadata_key", "this-value-was-hard-coded"))));
  }


  @Test
  public void testTransform_NotJson() throws Exception {
    JsltTransformService service =
        new JsltTransformService().withUrl(PROPERTIES.getProperty(JSLT_TRANSFORM_KEY));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("hello world", "UTF-8");
    assertThrows(ServiceException.class, ()->{
      execute(service, msg);
    }, "Failed, not Json"); 
  }

  @Test
  public void testTransform() throws Exception {
    JsltTransformService service =
        new JsltTransformService().withUrl(PROPERTIES.getProperty(JSLT_TRANSFORM_KEY));
    String data =
        FileUtils.readFileToString(new File(PROPERTIES.getProperty(JSLT_INPUT_KEY)), "UTF-8");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(data, "UTF-8");
    execute(service, msg);
    ReadContext ctx = parse(msg);
    assertNotNull(ctx.read("$.result.Open"));
    assertNotNull(ctx.read("$.result.Close"));
  }

  @Test
  public void testTransform_Classpath() throws Exception {
    // should be on the classpath.
    JsltTransformService service = new JsltTransformService().withUrl("collapse.jslt");
    String data =
        FileUtils.readFileToString(new File(PROPERTIES.getProperty(JSLT_INPUT_KEY)), "UTF-8");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(data, "UTF-8");
    execute(service, msg);
    ReadContext ctx = parse(msg);
    assertNotNull(ctx.read("$.result.Open"));
    assertNotNull(ctx.read("$.result.Close"));
  }

  @Test
  public void testTransform_WithCache() throws Exception {
    // should be on the classpath
    // Set a simple cache.
    JsltTransformService service = new JsltTransformService().withUrl("collapse.jslt")
        .withConnection(new CacheConnection(new ExpiringMapCache().withMaxEntries(10)));
    String data =
        FileUtils.readFileToString(new File(PROPERTIES.getProperty(JSLT_INPUT_KEY)), "UTF-8");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(data, "UTF-8");
    AdaptrisMessage msg2 = new DefaultMessageFactory().newMessage(data, "UTF-8");
    try {
      LifecycleHelper.initAndStart(service);
      service.doService(msg);
      ReadContext ctx = parse(msg);
      assertNotNull(ctx.read("$.result.Open"));
      assertNotNull(ctx.read("$.result.Close"));
      service.doService(msg2);
      ctx = parse(msg2);
      assertNotNull(ctx.read("$.result.Open"));
      assertNotNull(ctx.read("$.result.Close"));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testTransform_WithVars() throws Exception {
    JsltVariables vars = new JsltVariables().withVariableBuilders(
        new JsltMetadataVariables()
            .withFilter(new RegexMetadataFilter().withIncludePatterns("^.*_metadata_key$")),
        new JsltObjectVariables().withObjectMetadataKeyRegexp("^.*_metadata_key$"),
        new JsltFixedVariables()
            .withVariables(new KeyValuePair("fixed_metadata_key", "this-value-was-hard-coded"))
        );

    JsltTransformService service =
        new JsltTransformService().withUrl(PROPERTIES.getProperty(JSLT_TRANSFORM__WITH_VARS_KEY))
            .withVariables(vars);
    String data =
        FileUtils.readFileToString(new File(PROPERTIES.getProperty(JSLT_INPUT_KEY)), "UTF-8");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(data, "UTF-8");
    msg.addObjectHeader(JSLT_VAR_OBJECT, TextNode.valueOf("this-value-was-from-object-metadata"));
    msg.addMetadata(JSLT_VAR_METADATA, "this-value-was-from-normal-metadata");

    execute(service, msg);
    // collapse-with-var.jslt should give us something like this :
    // {
    // "result": {
    // "Open": "OpenDoc()",
    // "Close": "CloseDoc()",
    // "metadata_value_from_object": "this-value-was-from-object-metadata",
    // "metadata_value_from_metadata": "this-value-was-from-normal-metadata",
    // "metadata_value_from_fixed": "this-value-was-hard-coded"
    // }
    // }
    ReadContext ctx = parse(msg);
    assertEquals("this-value-was-from-object-metadata", ctx.read("$.result.metadata_value_from_object"));
    assertEquals("this-value-was-from-normal-metadata", ctx.read("$.result.metadata_value_from_metadata"));
    assertEquals("this-value-was-hard-coded", ctx.read("$.result.metadata_value_from_fixed"));
  }

  protected ReadContext parse(AdaptrisMessage content) {
    return JsonPath.parse(content.getContent(), jsonConfig);
  }
}
