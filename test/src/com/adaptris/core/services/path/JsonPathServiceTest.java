package com.adaptris.core.services.path;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataDestination;
import com.adaptris.core.common.MetadataDataDestination;
import com.adaptris.core.common.PayloadDataDestination;

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
    MetadataDataDestination targetMetadataDestination = new MetadataDataDestination();
    targetMetadataDestination.setMetadataKey("JsonResultKey");
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("$.store.book[1].title");  // Get the 2nd book's title.
    
    Execution execution = new Execution();
    execution.setTargetDataDestination(targetMetadataDestination);
    execution.setSourceJsonPathExpression(constantDataDestination);
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(new PayloadDataDestination());
    
    jsonPathService.doService(message);
    
    assertTrue(message.headersContainsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMetadataUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDataDestination targetMetadataDestination = new MetadataDataDestination();
    targetMetadataDestination.setMetadataKey("JsonResultKey");
    
    MetadataDataDestination jsonMetadataDestination = new MetadataDataDestination();
    jsonMetadataDestination.setMetadataKey("JsonPath");
    
    Execution execution = new Execution();
    execution.setTargetDataDestination(targetMetadataDestination);
    execution.setSourceJsonPathExpression(jsonMetadataDestination);
        
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(new PayloadDataDestination());
    
    jsonPathService.doService(message);
    
    assertTrue(message.headersContainsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromMetadataToPayload() throws Exception {
    MetadataDataDestination sourceMetadataDestination = new MetadataDataDestination();
    sourceMetadataDestination.setMetadataKey("JsonResultKey");
    
    PayloadDataDestination targetPayloadDestination = new PayloadDataDestination();
    
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("$.store.book[1].title");  // Get the 2nd book's title.
    
    Execution execution = new Execution();
    execution.setTargetDataDestination(targetPayloadDestination);
    execution.setSourceJsonPathExpression(constantDataDestination);
    
    message.setContent("", message.getContentEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testSimpleResultFromMetadataToPayloadUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDataDestination sourceMetadataDestination = new MetadataDataDestination();
    sourceMetadataDestination.setMetadataKey("JsonResultKey");
    
    MetadataDataDestination sourceJsonPathDestination = new MetadataDataDestination();
    sourceJsonPathDestination.setMetadataKey("JsonPath");
    
    Execution execution = new Execution();
    execution.setTargetDataDestination(new PayloadDataDestination());
    execution.setSourceJsonPathExpression(sourceJsonPathDestination);
    
    message.setContent("", message.getContentEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testSimpleResultFromPayloadToMultipleDestinations() throws Exception {
    MetadataDataDestination targetMetadataDestination = new MetadataDataDestination();
    targetMetadataDestination.setMetadataKey("JsonResultKey");
            
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("$.store.book[0].title");  // Get the 2nd book's title.
    
    Execution exec1 = new Execution();
    exec1.setTargetDataDestination(targetMetadataDestination);
    exec1.setSourceJsonPathExpression(constantDataDestination);
    
    Execution exec2 = new Execution();
    exec2.setTargetDataDestination(new PayloadDataDestination());
    exec2.setSourceJsonPathExpression(constantDataDestination);
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { exec1, exec2 }));
    jsonPathService.setSourceDestination(new PayloadDataDestination());
    
    jsonPathService.doService(message);
    
    assertEquals("Sayings of the Century", message.getContent());
    assertEquals("Sayings of the Century", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMultiplePayloadDestinations() throws Exception {    
    ConstantDataDestination constantDataDestination1 = new ConstantDataDestination();
    constantDataDestination1.setValue("$.store.book[0].title");  // Get the 2nd book's title.
    ConstantDataDestination constantDataDestination2 = new ConstantDataDestination();
    constantDataDestination2.setValue("$.store.book[1].title");  // Get the 2nd book's title.
    
    Execution exec1 = new Execution();
    exec1.setTargetDataDestination(new PayloadDataDestination());
    exec1.setSourceJsonPathExpression(constantDataDestination1);
    
    Execution exec2 = new Execution();
    exec2.setTargetDataDestination(new PayloadDataDestination());
    exec2.setSourceJsonPathExpression(constantDataDestination2);
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { exec1, exec2 }));
    jsonPathService.setSourceDestination(new PayloadDataDestination());
    
    jsonPathService.doService(message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testComplexResultFromPayloadToPayload() throws Exception {
    ConstantDataDestination constantDataDestination = new ConstantDataDestination();
    constantDataDestination.setValue("$..book[?(@.isbn)]");
    
    Execution execution = new Execution();
    execution.setTargetDataDestination(new PayloadDataDestination());
    execution.setSourceJsonPathExpression(constantDataDestination);
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(new PayloadDataDestination());
    
    jsonPathService.doService(message);
    
    assertEquals(complexExpected(), message.getContent());
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    try {
      ConstantDataDestination constantDataDestination1 = new ConstantDataDestination();
      constantDataDestination1.setValue("$.store.book[0].title");  // Get the 2nd book's title.
      ConstantDataDestination constantDataDestination2 = new ConstantDataDestination();
      constantDataDestination2.setValue("$.store.book[1].title");  // Get the 2nd book's title.
      
      Execution exec1 = new Execution();
      exec1.setTargetDataDestination(new PayloadDataDestination());
      exec1.setSourceJsonPathExpression(constantDataDestination1);
      
      Execution exec2 = new Execution();
      exec1.setTargetDataDestination(new PayloadDataDestination());
      exec1.setSourceJsonPathExpression(constantDataDestination2);
      
      jsonPathService.setExecutions(Arrays.asList(new Execution[] { exec1, exec2 }));
      jsonPathService.setSourceDestination(new PayloadDataDestination());
    } catch (Exception ex) {
      fail("Exception thrown while building example config.");
    }
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
