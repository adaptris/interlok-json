package com.adaptris.core.services.path;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;

public class JsonPathServiceTest extends ServiceCase {
  
  private static final String BASE_DIR_KEY = "JsonPathServiceExamples.baseDir";
  
  private JsonPathService jsonPathService;

  private AdaptrisMessage message;
  
  public JsonPathServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  public void setUp() throws Exception {
    jsonPathService = new JsonPathService();
    message = DefaultMessageFactory.getDefaultInstance().newMessage(this.sampleJsonContent());
  }
  
  public void tearDown() throws Exception {
    jsonPathService = null;
  }
  
  public void testSimpleResultFromPayloadToMetadata() throws Exception {
    MetadataDestination targetMetadataDestination = new MetadataDestination();
    targetMetadataDestination.setKey("JsonResultKey");
    targetMetadataDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[1].title"));   // Get the 2nd book's title.
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetMetadataDestination }));
    jsonPathService.setSourceDestination(new PayloadDestination());
    
    jsonPathService.doService(message);
    
    assertTrue(message.containsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMetadataUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDestination targetMetadataDestination = new MetadataDestination();
    targetMetadataDestination.setKey("JsonResultKey");
    targetMetadataDestination.setConfiguredJsonPath(new MetadataJsonPath("JsonPath"));   // Get the 2nd book's title.
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetMetadataDestination }));
    jsonPathService.setSourceDestination(new PayloadDestination());
    
    jsonPathService.doService(message);
    
    assertTrue(message.containsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromMetadataToPayload() throws Exception {
    MetadataDestination sourceMetadataDestination = new MetadataDestination();
    sourceMetadataDestination.setKey("JsonResultKey");
    
    PayloadDestination targetPayloadDestination = new PayloadDestination();
    targetPayloadDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[1].title")); // Get the 2nd book's title.
    
    message.setStringPayload("", message.getCharEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetPayloadDestination }));
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getStringPayload());
  }
  
  public void testSimpleResultFromMetadataToPayloadUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDestination sourceMetadataDestination = new MetadataDestination();
    sourceMetadataDestination.setKey("JsonResultKey");
    
    MetadataJsonPath metadataJsonPath = new MetadataJsonPath();
    metadataJsonPath.setMetadataKey("JsonPath");
    
    PayloadDestination targetPayloadDestination = new PayloadDestination();
    targetPayloadDestination.setConfiguredJsonPath(metadataJsonPath); // Get the 2nd book's title.
    
    message.setStringPayload("", message.getCharEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetPayloadDestination }));
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getStringPayload());
  }
  
  public void testSimpleResultFromPayloadToMultipleDestinations() throws Exception {
    MetadataDestination targetMetadataDestination = new MetadataDestination();
    targetMetadataDestination.setKey("JsonResultKey");
    targetMetadataDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[0].title"));   // Get the 1st book's title.
        
    PayloadDestination targetPayloadDestination = new PayloadDestination();
    targetPayloadDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[0].title"));   // Get the 1st book's title.
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetMetadataDestination, targetPayloadDestination }));
    jsonPathService.setSourceDestination(new PayloadDestination());
    
    jsonPathService.doService(message);
    
    assertEquals("Sayings of the Century", message.getStringPayload());
    assertEquals("Sayings of the Century", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMultiplePayloadDestinations() throws Exception {
    PayloadDestination targetPayloadDestination1 = new PayloadDestination();
    targetPayloadDestination1.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[0].title"));   // Get the 1st book's title.
        
    PayloadDestination targetPayloadDestination2 = new PayloadDestination();
    targetPayloadDestination2.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[1].title"));   // Get the 1st book's title.
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetPayloadDestination1, targetPayloadDestination2 }));
    jsonPathService.setSourceDestination(new PayloadDestination());
    
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getStringPayload());
  }
  
  public void testComplexResultFromPayloadToPayload() throws Exception {
    PayloadDestination targetPayloadDestination = new PayloadDestination();
    targetPayloadDestination.setConfiguredJsonPath(new ConstantJsonPath("$..book[?(@.isbn)]"));   // Get all books with an ISBN
    
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetPayloadDestination }));
    jsonPathService.setSourceDestination(new PayloadDestination());
    
    jsonPathService.doService(message);
    
    assertEquals(complexExpected(), message.getStringPayload());
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataDestination targetMetadataDestination = new MetadataDestination();
    targetMetadataDestination.setKey("metadata-key");
    targetMetadataDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[0].title"));
    
    PayloadDestination targetPayloadDestination = new PayloadDestination();
    targetPayloadDestination.setConfiguredJsonPath(new ConstantJsonPath("$.store.book[1].title"));
    
    JsonPathService jsonPathService = new JsonPathService();
    jsonPathService.setSourceDestination(new PayloadDestination());
    jsonPathService.setTargetDestinations(Arrays.asList(new Destination[] { targetMetadataDestination, targetPayloadDestination }));
    
    return jsonPathService;
  }

  private String sampleJsonContent() {
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
    + "\"expensive\": 10"
    + "}";
  }
  
  private String complexExpected() {
    return "["
        + "{category=fiction, author=Herman Melville, title=Moby Dick, isbn=0-553-21311-3, price=8.99}, "
        + "{category=fiction, author=J. R. R. Tolkien, title=The Lord of the Rings, isbn=0-395-19395-8, price=22.99}"
        + "]";
  }
}
