package com.adaptris.core.transform.json;

import com.adaptris.core.ServiceException;

public class JsonArrayTransformationDriverTest extends DefaultTransformDriverTest {

  public JsonArrayTransformationDriverTest(String name) {
    super(name);
  }

  public void testJsonToXml() throws Exception {
    JsonArrayTransformationDriver driver = createDriver();
    try {
      driver.transform(JsonXmlTransformServiceTest.JSON_INPUT, TransformationDirection.JSON_TO_XML);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  protected JsonArrayTransformationDriver createDriver() {
    return new JsonArrayTransformationDriver();
  }
}
