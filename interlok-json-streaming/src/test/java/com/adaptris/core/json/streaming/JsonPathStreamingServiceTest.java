package com.adaptris.core.json.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.jsfr.json.Collector;
import org.jsfr.json.JsonSurferGson;
import org.jsfr.json.JsonSurferJackson;
import org.jsfr.json.JsonSurferJsonSimple;
import org.jsfr.json.ValueBox;
import org.jsfr.json.exception.JsonSurfingException;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.MetadataStreamInputParameter;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.json.JsonPathExecution;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.text.NullToEmptyStringConverter;

public class JsonPathStreamingServiceTest extends ExampleServiceCase {

  private static final String PATH_TO_ARRAY = "$.some_integers";
  private static final String PATH_NOT_FOUND = "$.path.not.found";
  private static final String JSON_PATH = "JsonPath";
  private static final String JSON_RESULT_KEY = "JsonResultKey";
  private static final String BASE_DIR_KEY = "JsonPathServiceExamples.baseDir";

  private static final String STORE_BOOK_0_TITLE = "$.store.book[0].title";
  private static final String SAYINGS_OF_THE_CENTURY = "Sayings of the Century";
  private static final String SWORD_OF_HONOUR = "Sword of Honour";
  private static final String STORE_BOOK_1_TITLE = "$.store.book[1].title";

  private static final String JSON_WITH_NULL =
      "{\r\n" + "    \"LastModifiedDate\": \"2017-11-23T15:15:31.000+0000\",\r\n" + "    \"WhatId\": null,\r\n"
          + "    \"Description\": \"wanted more info on ebusiness and agility including analytical models\"\r\n" + "}";

  public JsonPathStreamingServiceTest() {
    super();
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  AdaptrisMessage createMessage() throws Exception {
    return DefaultMessageFactory.getDefaultInstance().newMessage(sampleJsonContent());
  }

  @Test
  public void testExtract_JsonObject() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.store.bicycle");
    AdaptrisMessage message = createMessage();
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setSurfer(JsonPathStreamingService.Surfer.JACKSON);
    execute(jsonPathService, message);

    // Should be {"color":"red","price":19.95} so we can extract the color and price
    String json = message.getMetadataValue(JSON_RESULT_KEY);

    Collector collector = JsonSurferGson.INSTANCE.collector(json);
    ValueBox<String> box1 = collector.collectOne("$.color", String.class);
    ValueBox<Double> box2 = collector.collectOne("$.price", Double.class);
    collector.exec();
    String color = box1.get();
    Double price = box2.get();

    assertEquals("red", color);
    assertEquals(19.95, price, 0.1);
  }

  @Test
  public void testExtract_JsonArray() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.store.book");
    AdaptrisMessage message = createMessage();
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setSurfer(JsonPathStreamingService.Surfer.GSON);
    execute(jsonPathService, message);

    // This gets us all the books, which is an array, so we can interrogate all this as well.
    // [{book}, {book}, {book}]
    String json = message.getMetadataValue(JSON_RESULT_KEY);

    Collector collector = JsonSurferJsonSimple.INSTANCE.collector(json);
    ValueBox<String> box1 = collector.collectOne("$[0].title", String.class);
    ValueBox<String> box2 = collector.collectOne("$[1].title", String.class);
    collector.exec();
    String title0 = box1.get();
    String title1 = box2.get();

    assertEquals(SAYINGS_OF_THE_CENTURY, title0);
    assertEquals(SWORD_OF_HONOUR, title1);
  }

  @Test
  public void testSimpleResultFromPayloadToMetadata() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setSurfer(JsonPathStreamingService.Surfer.JACKSON);
    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals(SWORD_OF_HONOUR, message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testPathNotFound_NoSuppress_Execution() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);
    Execution execution = new Execution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new Execution[]{ execution }));

    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {
      // expected
    }

    assertFalse(message.headersContainsKey(JSON_RESULT_KEY));
  }

  @Test
  public void testPathNotFound_Suppress() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination).withSuppressPathNotFound(true);
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(),
            Arrays.asList(new JsonPathExecution[]
                    {
                            execution
                    }));
    execute(jsonPathService, message);

    assertFalse(message.headersContainsKey(JSON_RESULT_KEY));
  }

  @Test
  public void testPathNotFound_NoSuppress() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));

    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testService_UnwrapJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_TO_ARRAY);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setUnwrapJson(true);
    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals("1,2,3,4", message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testService_NoUnwrapJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_TO_ARRAY);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals("[1,2,3,4]", message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testNotJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("");

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));

    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {
      assertEquals(JsonSurfingException.class, expected.getCause().getClass());
    }
  }

  @Test
  public void testSimpleResultFromPayloadToMetadataUsingMetadataJsonPath() throws Exception {
    AdaptrisMessage message = createMessage();
    message.addMetadata(JSON_PATH, STORE_BOOK_1_TITLE);
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    MetadataDataInputParameter jsonMetadataDestination = new MetadataDataInputParameter(JSON_PATH);
    jsonMetadataDestination.setMetadataKey(JSON_PATH);
    JsonPathExecution execution = new JsonPathExecution(jsonMetadataDestination, targetMetadataDestination);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals(SWORD_OF_HONOUR, message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testSimpleResultFromMetadataToPayload() throws Exception {
    MetadataStreamInputParameter sourceMetadataDestination = new MetadataStreamInputParameter(JSON_RESULT_KEY);
    StringPayloadDataOutputParameter targetPayloadDestination = new StringPayloadDataOutputParameter();
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetPayloadDestination);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(JSON_RESULT_KEY, sampleJsonContent());

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(sourceMetadataDestination, Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setSource(sourceMetadataDestination);
    jsonPathService.setExecutions(Arrays.asList(new JsonPathExecution[]{ execution }));
    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  @Test
  public void testSimpleResultFromMetadataToPayloadUsingMetadataJsonPath() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(JSON_PATH, STORE_BOOK_1_TITLE);
    message.addMetadata(JSON_RESULT_KEY, sampleJsonContent());
    MetadataStreamInputParameter sourceMetadataDestination = new MetadataStreamInputParameter(JSON_RESULT_KEY);
    MetadataDataInputParameter sourceJsonPathDestination = new MetadataDataInputParameter(JSON_PATH);
    JsonPathExecution execution = new JsonPathExecution(sourceJsonPathDestination, new StringPayloadDataOutputParameter());

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(sourceMetadataDestination, Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setExecutions(Arrays.asList(new JsonPathExecution[]{ execution }));
    jsonPathService.setSource(sourceMetadataDestination);
    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  @Test
  public void testSimpleResultFromPayloadToMultipleDestinations() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);
    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination, new StringPayloadDataOutputParameter());
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ exec1, exec2 }));
    execute(jsonPathService, message);

    assertEquals(SAYINGS_OF_THE_CENTURY, message.getContent());
    assertEquals(SAYINGS_OF_THE_CENTURY, message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testSimpleResultFromPayloadToMultiplePayloadDestinations() throws Exception {
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);
    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination1, new StringPayloadDataOutputParameter());
    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination2, new StringPayloadDataOutputParameter());
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ exec1, exec2 }));
    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  @Test
  public void testComplexResultFromPayloadToPayload() throws Exception {
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$..book[?(@.isbn)]");
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, new StringPayloadDataOutputParameter());
    AdaptrisMessage message = createMessage();

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), Arrays.asList(new JsonPathExecution[]{ execution }));
    execute(jsonPathService, message);

    Collector collector = JsonSurferJackson.INSTANCE.collector(message.getInputStream());
    ValueBox<String> box1 = collector.collectOne("$[0].author", String.class);
    ValueBox<String> box2 = collector.collectOne("$[1].author", String.class);
    collector.exec();
    assertEquals("Herman Melville", box1.get());
    assertEquals("J. R. R. Tolkien", box2.get());
  }

  @Test
  public void testNullToEmptyString_JsonExecution() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.WhatId");
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination).withNullConverter(new NullToEmptyStringConverter());
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(JSON_WITH_NULL);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), execution);
    execute(jsonPathService, message);

    assertEquals("", message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testNullToEmptyString_JsonExecution_DefaultBehaviour() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.WhatId");
    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(JSON_WITH_NULL);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), execution);
    execute(jsonPathService, message);

    assertEquals("null", message.getMetadataValue(JSON_RESULT_KEY));
  }

  @Test
  public void testNullToEmptyString_Execution() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.WhatId");
    Execution execution = new Execution(constantDataDestination, targetMetadataDestination);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(JSON_WITH_NULL);

    JsonPathStreamingService jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), execution);

    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JsonPathStreamingService jsonPathService = null;
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);
    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination1, new MetadataDataOutputParameter("targetMetadataKey1"));
    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination2, new MetadataDataOutputParameter("targetMetadataKey2"));

    jsonPathService = new JsonPathStreamingService(new PayloadStreamInputParameter(), new ArrayList<>(Arrays.asList(new JsonPathExecution[]{ exec1, exec2 })));
    jsonPathService.setSurfer(JsonPathStreamingService.Surfer.SIMPLE);
    return jsonPathService;
  }

  public static String sampleJsonContent() {
    return "{"
    + "\"store\": {"
    +    "\"book\": ["
    +        "{"
    +            "\"category\": \"reference\","
    +            "\"author\": \"Nigel Rees\","
    +            "\"title\": \"Sayings of the Century\","
    +            "\"price\": 8.95"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"Evelyn Waugh\","
    +            "\"title\": \"Sword of Honour\","
    +            "\"price\": 12.99"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"Herman Melville\","
    +            "\"title\": \"Moby Dick\","
    +            "\"isbn\": \"0-553-21311-3\","
    +            "\"price\": 8.99"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"J. R. R. Tolkien\","
    +            "\"title\": \"The Lord of the Rings\","
    +            "\"isbn\": \"0-395-19395-8\","
    +            "\"price\": 22.99"
    +        "}"
    +    "],"
    +    "\"bicycle\": {"
    +        "\"color\": \"red\","
    +        "\"price\": 19.95"
    +    "}"
    + "},"
    + "\"expensive\": 10,"
    + "\"some_integers\" : [1,2,3,4]"
    + "}";
  }

}
