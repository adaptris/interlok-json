package com.adaptris.core.services.path;

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;

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
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter("JsonResultKey");
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.store.book[1].title");
    
    Execution execution = new Execution(constantDataDestination, targetMetadataDestination);
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(new StringPayloadDataInputParameter());
    
    execute(jsonPathService, message);
    
    assertTrue(message.headersContainsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMetadataUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter("JsonResultKey");
    
    MetadataDataInputParameter jsonMetadataDestination = new MetadataDataInputParameter("JsonPath");
    jsonMetadataDestination.setMetadataKey("JsonPath");
    
    Execution execution = new Execution(jsonMetadataDestination, targetMetadataDestination);
        
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(new StringPayloadDataInputParameter());
    
    execute(jsonPathService, message);
    
    assertTrue(message.headersContainsKey("JsonResultKey"));
    assertEquals("Sword of Honour", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromMetadataToPayload() throws Exception {
    MetadataDataInputParameter sourceMetadataDestination = new MetadataDataInputParameter("JsonResultKey");
    
    StringPayloadDataOutputParameter targetPayloadDestination = new StringPayloadDataOutputParameter();
    
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.store.book[1].title");
    
    Execution execution = new Execution(constantDataDestination, targetPayloadDestination);
    
    message.setContent("", message.getContentEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    execute(jsonPathService, message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testSimpleResultFromMetadataToPayloadUsingMetadataJsonPath() throws Exception {
    message.addMetadata("JsonPath", "$.store.book[1].title");
    
    MetadataDataInputParameter sourceMetadataDestination = new MetadataDataInputParameter("JsonResultKey");
    
    MetadataDataInputParameter sourceJsonPathDestination = new MetadataDataInputParameter("JsonPath");
    
    Execution execution = new Execution(sourceJsonPathDestination, new StringPayloadDataOutputParameter());
    
    message.setContent("", message.getContentEncoding());
    message.addMetadata("JsonResultKey", this.sampleJsonContent());
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    jsonPathService.setSourceDestination(sourceMetadataDestination);
    
    execute(jsonPathService, message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testSimpleResultFromPayloadToMultipleDestinations() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter("JsonResultKey");
            
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$.store.book[0].title");
    
    Execution exec1 = new Execution(constantDataDestination, targetMetadataDestination);
    
    Execution exec2 = new Execution(constantDataDestination, new StringPayloadDataOutputParameter());
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { exec1, exec2 }));
    
    execute(jsonPathService, message);
    
    assertEquals("Sayings of the Century", message.getContent());
    assertEquals("Sayings of the Century", message.getMetadataValue("JsonResultKey"));
  }
  
  public void testSimpleResultFromPayloadToMultiplePayloadDestinations() throws Exception {    
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter("$.store.book[0].title");
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter("$.store.book[1].title");
    
    Execution exec1 = new Execution(constantDataDestination1, new StringPayloadDataOutputParameter());
    
    Execution exec2 = new Execution(constantDataDestination2, new StringPayloadDataOutputParameter());
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { exec1, exec2 }));
    
    execute(jsonPathService, message);
    
    assertEquals("Sword of Honour", message.getContent());
  }
  
  public void testComplexResultFromPayloadToPayload() throws Exception {
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$..book[?(@.isbn)]");
    
    Execution execution = new Execution(constantDataDestination, new StringPayloadDataOutputParameter());
    
    jsonPathService.setExecutions(Arrays.asList(new Execution[] { execution }));
    
    jsonPathService.doService(message);
    
    assertEquals(complexExpected(), message.getContent());
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    try {
      ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter("$.store.book[0].title");
      ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter("$.store.book[1].title");
      
      Execution exec1 = new Execution(constantDataDestination1, new MetadataDataOutputParameter("targetMetadataKey1"));
      
      Execution exec2 = new Execution(constantDataDestination2, new MetadataDataOutputParameter("targetMetadataKey2"));
      
      jsonPathService.setExecutions(new ArrayList<Execution>(Arrays.asList(new Execution[] {exec1, exec2})));
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
