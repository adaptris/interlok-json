package com.adaptris.core.json.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
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

  private static Map<String, String> resultKeyValuePairs;
  private static List<String> jsonPath;
  private static JsonPathBuilder jsonPathProvider;

  @BeforeAll
  public static void setUp() {
    resultKeyValuePairs = new LinkedHashMap<>();
    jsonPath = new ArrayList<>();
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
    jsonPathProvider.setJsonPaths(jsonPath);
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
    jsonPathProvider.setJsonPaths(jsonPath);
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
    jsonPathProvider.setJsonPaths(jsonPath);
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
    jsonPathProvider.setJsonPaths(jsonPath);
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
    jsonPathProvider.setJsonPaths(jsonPath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    }, "JSON path does not resolve to a single object.");
  }

  @Test
  public void testArrayJsonPathException() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_ARRAY_PATH);
    jsonPathProvider.setJsonPaths(jsonPath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    }, "JSON path does not resolve to a single object.");
  }

  @Test
  public void testNonJson() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NON_JSON);
    jsonPath.add(JSON_FIRSTNAME_PATH);
    jsonPathProvider.setJsonPaths(jsonPath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    }, "Non JSON message.");
  }

  @Test
  public void testNonExistentJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_NON_EXISTENT_PATH);
    jsonPathProvider.setJsonPaths(jsonPath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    }, "JSON path does not exist.");
  }

  @Test
  public void testInvalidJsonPath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON);
    jsonPath.add(JSON_INVALID_PATH);
    jsonPathProvider.setJsonPaths(jsonPath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = jsonPathProvider.extract(msg);
    }, "Invalid JSON path.");
  }

}
