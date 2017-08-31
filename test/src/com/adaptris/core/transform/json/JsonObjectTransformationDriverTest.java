package com.adaptris.core.transform.json;

import com.adaptris.core.ServiceException;

public class JsonObjectTransformationDriverTest extends DefaultTransformDriverTest {

  public JsonObjectTransformationDriverTest(String name) {
    super(name);
  }

  @Override
  public void testJsonArrayToXml() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    try {
      driver.transform(JsonXmlTransformServiceTest.ARRAY_JSON_INPUT, TransformationDirection.JSON_TO_XML);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected JsonObjectTransformationDriver createDriver() {
    return new JsonObjectTransformationDriver();
  }
}
