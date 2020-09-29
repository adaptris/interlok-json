package com.adaptris.core.json.jq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.EnumSet;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonJqTransformTest extends ServiceCase {

  private static final String SAMPLE_QUERY = "{\n" + " \"status-id\": .id,\n" + " \"status-code\": .status.agreementStatusCd,\n"
      + " \"status-description\": .status.agreementStatusDesc\n" + "}";

  private static final String SAMPLE_DATA = "{\n" + "  \"id\": 15809259,\n" + "  \"status\": {\n"
      + "    \"agreementStatusCd\": \"4\",\n" + "    \"agreementStatusDesc\": \"Completed\"\n" + "  }\n" + "}";

  private static final String METADATA_QUERY = "{\n" + " \"greeting\": $metadata.greeting\n" + "}";

  private Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
      .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Test
  public void testService() throws Exception {
    JsonJqTransform service = new JsonJqTransform().withQuerySource(new ConstantDataInputParameter(SAMPLE_QUERY));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(SAMPLE_DATA);
    execute(service, msg);
    assertNotNull(msg.getContent());
    System.err.println(msg.getContent());
    ReadContext ctx = parse(msg);
    assertNotNull(ctx.read("$.status-description"));
    assertNotNull(ctx.read("$.status-code"));
    assertNotNull(ctx.read("$.status-id"));
  }

  @Test
  public void testService_WithMetadata() throws Exception {
    JsonJqTransform service = new JsonJqTransform().withQuerySource(new ConstantDataInputParameter(METADATA_QUERY))
        .withMetadataFilter(new NoOpMetadataFilter());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SAMPLE_DATA);
    msg.addMetadata(new MetadataElement("greeting", "Hello World"));
    execute(service, msg);
    assertNotNull(msg.getContent());
    System.err.println(msg.getContent());
    ReadContext ctx = parse(msg);
    assertNotFound(ctx, "$.status-description");
    assertNotFound(ctx, "$.status-code");
    assertNotFound(ctx, "$.status-id");
    assertNotNull(ctx.read("$.greeting"));
    assertEquals("Hello World", ctx.read("$.greeting"));
  }

  private void assertNotFound(ReadContext ctx, String path) {
    try {
      ctx.read(path);
      fail("[" + path + "] should have thrown a PathNotFoundException");
    }
    catch (PathNotFoundException expected) {

    }
  }
  @Override
  protected JsonJqTransform retrieveObjectForSampleConfig() {
    JsonJqTransform service = new JsonJqTransform();
    service.setQuerySource(new ConstantDataInputParameter(SAMPLE_QUERY));
    return service;
  }

  protected ReadContext parse(String content) {
    return JsonPath.parse(content, jsonConfig);
  }

  protected ReadContext parse(AdaptrisMessage content) {
    return parse(content.getContent());
  }
}
