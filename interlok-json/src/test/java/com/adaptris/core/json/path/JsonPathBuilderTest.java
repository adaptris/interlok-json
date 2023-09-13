package com.adaptris.core.json.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.booleanThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class JsonPathBuilderTest {

  private static final String JSON = "{\"firstName\":\"John\",\"lastName\":\"doe"
      + "\",\"age\":26,\"address\":{\"streetAddress\":\"street\",\"city\":\"city\",\"postalCode"
      + "\":\"TW12 3RR\"},\"phoneNumbers\":[{\"type\":\"mobile\",\"number\":\"000-000-000\"},"
      + "{\"type\":\"home\",\"number\":\"111-111-111\"}]}";
  
  private static final String NON_JSON = "not json";

  private static final String JSON_FIRSTNAME_PATH = "$.firstName";
  private static final String JSON_LASTNAME_PATH = "$.lastName";
  private static final String JSON_AGE_PATH = "$.age";
  private static final String JSON_MULTI_OBJECT_PATH = "$.address";
  private static final String JSON_ARRAY_PATH = "$.phoneNumbers";
  private static final String JSON_EXPRESSION_PATH = "$.phoneNumbers.[0].number";
  private static final String JSON_INVALID_PATH = "invalid json path";
  private static final String JSON_NON_EXISTENT_PATH = "$.thirdName";

  private static final String JSON_PATH_NOT_FOUND_EXCEPTION_MESSAGE = "No results found for JSON path [%s]";
  private static final String JSON_INVALID_PATH_EXCEPTION_MESSAGE = "Invalid Json path [%s]";
  private static final String JSON_NON_OBJECT_PATH_EXCEPTION_MESSAGE = "Please ensure your path [%s] points to a single JSON object";
  
  private static Map<String, String> resultKeyValuePairs;
  private static List<String> jsonPath;
  private static JsonPathBuilder jsonPathProvider;
  
  @BeforeAll
  public static void setUp() {
    resultKeyValuePairs  = new LinkedHashMap<>();
    jsonPath = new ArrayList<String>();
    jsonPathProvider = new JsonPathBuilder();
  }
  
  @AfterEach
  public void tearDown() {
    resultKeyValuePairs.clear();
    jsonPath.clear();
  }

  @Test
  public void testSingleObjectJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_FIRSTNAME_PATH);
    jsonPathProvider.setPaths(jsonPath);
    try {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
      jsonPathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(JSON, msg.getContent());
  }
  
  @Test
  public void testSingleObjectJsonPathFromMetadata() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    msg.addMetadata("jsonPath", JSON_FIRSTNAME_PATH);
    jsonPath.add("%message{jsonPath}");
    jsonPathProvider.setPaths(jsonPath);
    try {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
      jsonPathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(JSON, msg.getContent());
  }
  
  @Test
  public void testExpressionJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_EXPRESSION_PATH);
    jsonPathProvider.setPaths(jsonPath);
    try {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
      jsonPathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(JSON, msg.getContent());
  }
  
  @Test
  public void testMultipleJsonPaths() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_FIRSTNAME_PATH);
    jsonPath.add(JSON_LASTNAME_PATH);
    jsonPath.add(JSON_AGE_PATH);
    jsonPathProvider.setPaths(jsonPath);
    try {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
      jsonPathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(JSON, msg.getContent());
  }
  
  @Test
  public void testMultiObjectJsonPathException() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_MULTI_OBJECT_PATH);
    jsonPathProvider.setPaths(jsonPath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    });
    assertEquals(String.format(JSON_NON_OBJECT_PATH_EXCEPTION_MESSAGE, JSON_MULTI_OBJECT_PATH), exception.getMessage());
  }
  
  @Test
  public void testArrayJsonPathException() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_ARRAY_PATH);
    jsonPathProvider.setPaths(jsonPath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    });
    assertEquals(String.format(JSON_NON_OBJECT_PATH_EXCEPTION_MESSAGE, JSON_ARRAY_PATH), exception.getMessage());
  }
  
  @Test
  public void testNonJson() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NON_JSON);
    jsonPath.add(JSON_FIRSTNAME_PATH);
    jsonPathProvider.setPaths(jsonPath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    });
    assertEquals(String.format(JSON_PATH_NOT_FOUND_EXCEPTION_MESSAGE, JSON_FIRSTNAME_PATH), exception.getMessage());
  }
  
  @Test
  public void testNonExistentJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_NON_EXISTENT_PATH);
    jsonPathProvider.setPaths(jsonPath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    });
    assertEquals(String.format(JSON_PATH_NOT_FOUND_EXCEPTION_MESSAGE, JSON_NON_EXISTENT_PATH), exception.getMessage());
  }
  
  @Test
  public void testInvalidJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_INVALID_PATH);
    jsonPathProvider.setPaths(jsonPath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    });
    assertEquals(String.format(JSON_INVALID_PATH_EXCEPTION_MESSAGE, JSON_INVALID_PATH), exception.getMessage());
  }
  
}
