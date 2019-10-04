package com.adaptris.core.json.schema;

import java.io.IOException;
import java.util.EnumSet;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.services.jdbc.BrokenAdaptrisMessage;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.util.LifecycleHelper;
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

  /**
   * Default constructor.
   */
  public JsonSchemaServiceTest() {
    super("JSON schema validation service.");
  }

  public JsonSchemaServiceTest(final String name) {
    super(name);
  }

  private static final String SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_schema.json";
  private static final String CLASSPATH_SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_array_schema.json";

  private static final String VALID_JSON = "{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }";
  private static final String INVALID_JSON = "{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }";
  private static final String JSON_ARRAY = "[{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }]";
  private static final String INVALID_JSON_ARRAY = "[{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }]";

  public void testInit() throws Exception {
    JsonSchemaService service = new JsonSchemaService();
    try {
      assertNull(service.getSchemaUrl());
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testJsonSchemaLoader(){
    JsonSchemaService jsonSchemaService = new JsonSchemaService();
    assertTrue(jsonSchemaService.getJsonSchemaLoader() instanceof DefaultJsonSchemaLoader);
    jsonSchemaService.setJsonSchemaLoader(new AdvancedJsonSchemaLoader());
    assertTrue(jsonSchemaService.getJsonSchemaLoader() instanceof AdvancedJsonSchemaLoader);
  }

  public void testSuccess() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
    execute(createService(), message);
  }

  public void testFailure() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testArray() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }


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

  public void testAdvancedJsonSchemaLoader() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setDestination(new ConfiguredDestination(CLASSPATH_SCHEMA_URL));
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader().withClassPathAwareClient(true));
    execute(service, message);

  }

  public void testAdvancedJsonSchemaLoader_Failed() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON_ARRAY);
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setDestination(new ConfiguredDestination(CLASSPATH_SCHEMA_URL));
    JsonSchemaService service = new JsonSchemaService(schemaUrl);
    service.setJsonSchemaLoader(new AdvancedJsonSchemaLoader().withClassPathAwareClient(true));
    try {
      execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

  private JsonSchemaService createService() {
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setDestination(new ConfiguredDestination(SCHEMA_URL));
    return new JsonSchemaService(schemaUrl);
  }

  private void assertModifications(AdaptrisMessage msg) throws IOException {
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
          .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.original"));
    assertNotNull(context.read("$.schema-violations"));
  }
}
