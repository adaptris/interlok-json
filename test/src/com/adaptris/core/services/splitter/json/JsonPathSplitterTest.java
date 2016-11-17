package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.services.splitter.MessageSplitter;

public class JsonPathSplitterTest extends SplitterServiceExample {

  public JsonPathSplitterTest(String name) {
    super(name);
  }
  
  public void testSplitArray() throws Exception {
    JsonPathSplitter splitter = createSplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(sampleJsonContent());

    Iterable<AdaptrisMessage> splitMessages = splitter.splitMessage(src);
    int i = 0;
    for(AdaptrisMessage message : splitMessages) {
      i++;
      assertNotNull(message);
    }
    assertEquals(4, i);
  }


  @Override
  JsonPathSplitter createSplitter() {
    JsonPathSplitter splitter = new JsonPathSplitter();
    StringPayloadDataInputParameter jsonSource = new StringPayloadDataInputParameter();
    ConstantDataInputParameter jsonPath = new ConstantDataInputParameter("$.store.book");
    splitter.setJsonPath(jsonPath);
    splitter.setJsonSource(jsonSource);
    splitter.setMessageSplitter(new JsonArraySplitter());
    return splitter;
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

}
