package com.adaptris.core.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minidev.json.JSONArray;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

import com.adaptris.core.ServiceException;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.json.JacksonJsonDeserializer;
import com.adaptris.core.services.jdbc.BrokenAdaptrisMessage;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * Unit tests for {@link JsonSchemaService}.
 *
 * @author Ashley Anderson
 */
public class JsonSchemaServiceTest extends TransformServiceExample {

  private static final String SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_schema.json";
  private static final String CLASSPATH_SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_array_schema.json";
  private static final String RELATIVE_SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_array_schema_relative.json";

  private static final String KEY_SCHEMA_DIR = "json.schema.baseDir";

  private static final String VALID_JSON = "{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }";
  private static final String INVALID_JSON = "{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }";
  private static final String SLIGHTLY_INVALID_JSON = "{ \"rectangle\" : { \"a\" : -5, \"b\" : 5 } }";
  private static final String JSON_ARRAY = "[{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }]";
  private static final String INVALID_JSON_ARRAY = "[{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }]";
  private static final String INVALID_JSON_STRICT = "{ \"rectangle\" : value }";

  @Test
  public void testInit() throws Exception {
    JsonSchemaService service = new JsonSchemaService();
    try {
      assertNull(service.getSchemaUrl());
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (Exception expected) {

    }
  }

  @Test
  public void testJsonSchemaLoader(){
    JsonSchemaService jsonSchemaService = new JsonSchemaService();
    assertTrue(jsonSchemaService.getJsonSchemaLoader() instanceof DefaultJsonSchemaLoader);
    jsonSchemaService.setJsonSchemaLoader(new AdvancedJsonSchemaLoader());
    assertTrue(jsonSchemaService.getJsonSchemaLoader() instanceof AdvancedJsonSchemaLoader);
  }

  @Test
  public void testSuccess() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
    execute(createService(), message);
  }

  @Test
  public void testFailure() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testArray() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }


  @Test
  public void testFailure_ObjectMetadata() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ObjectMetadataExceptionHandler());
      execute(service, message);
      fail();
    }
    catch (ServiceException expected) {
    }
    assertTrue(message.getObjectHeaders().containsKey(ObjectMetadataExceptionHandler.class.getSimpleName()));
  }

  @Test
  public void testFailure_ObjectMetadata_NoException() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ObjectMetadataExceptionHandler(Boolean.FALSE, getName()));
      execute(service, message);
      assertTrue(message.getObjectHeaders().containsKey(getName()));
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testFailure_Ignore() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new IgnoreValidationException());
      execute(service, message);
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testFailure_ModifyPayload() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ModifyPayloadExceptionHandler());
      execute(service, message);
      assertModifications(message);
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testFailure_ModifyPayload_ThrowException() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ModifyPayloadExceptionHandler(true));
      execute(service, message);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testFailure_ModifyPayload_IOException() throws Exception {
    AdaptrisMessage message = new BrokenAdaptrisMessage();
    message.setStringPayload(INVALID_JSON);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ModifyPayloadExceptionHandler());
      execute(service, message);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Test
  public void testFailure_ModifyPayload_Array() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON_ARRAY);
    try {
      JsonSchemaService service = createService();
      service.setOnValidationException(new ModifyPayloadExceptionHandler());
      execute(service, message);
      System.out.println(message.getContent());
      assertModifications(message);
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testAdvancedJsonSchemaLoaderClasspath() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setEndPoint(CLASSPATH_SCHEMA_URL);
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader().withClassPathAwareClient(true));
    execute(service, message);
  }


  @Test
  public void testAdvancedJsonSchemaLoader_NonClasspath() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
    JsonSchemaService service = createService();
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader());
    execute(service, message);

  }

  @Test
  public void testAdvancedJsonSchemaLoader_ResolutionScopeAndClasspath() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setEndPoint(RELATIVE_SCHEMA_URL);
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader()
        .withClassPathAwareClient(true)
        .withResolutionScope("classpath://com/adaptris/core/json/schema/"));
    execute(service, message);
  }

  @Test
  public void testAdvancedJsonSchemaLoader_ResolutionScopeAndFs() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setEndPoint(RELATIVE_SCHEMA_URL);
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader()
        .withResolutionScope(String.format("file:///%s", PROPERTIES.getProperty(KEY_SCHEMA_DIR))));
    execute(service, message);
  }

  @Test
  public void testAdvancedJsonSchemaLoader_Failed() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setEndPoint(CLASSPATH_SCHEMA_URL);
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader().withClassPathAwareClient(true));
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Test
  public void testJacksonDeserializer_Success() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
    JsonSchemaService service = createService();
    service.setDeserializer(new JacksonJsonDeserializer());
    execute(service, message);
  }

  // Should throw a service exception even though we've said ignore since it will be a parse
  // error. If it parsed, it would become a validation exception.
  @Test(expected = ServiceException.class)
  public void testJacksonDeserializer_Failure() throws Exception {
    AdaptrisMessage message =
        AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON_STRICT);
    JsonSchemaService service = createService();
    service.setDeserializer(new JacksonJsonDeserializer());
    service.setOnValidationException(new IgnoreValidationException());
    execute(service, message);
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

  private JsonSchemaService createService() {
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setEndPoint(SCHEMA_URL);
    return new JsonSchemaService(schemaUrl);
  }

  private void assertModifications(AdaptrisMessage msg) throws IOException {
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
          .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.original"));

    JSONArray violations = context.read("$.schema-violations");
    assertNotNull(violations);
    Set<String> messages = new HashSet<>();
    for (Object o : violations.toArray())
    {
      messages.add(o.toString());
    }
    // make sure there are no duplicates (INTERLOK-3484)
    assertEquals(violations.size(), messages.size());
  }
}
