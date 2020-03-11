package com.adaptris.core.transform.json;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.ServiceException;

public class JsonArrayTransformationDriverTest extends JsonlibTransformDriverTest {

  @Override
  @Test
  public void testJsonToXml() throws Exception {
    JsonArrayTransformationDriver driver = createDriver();
    try {
      driver.transform(JsonXmlTransformServiceTest.JSON_INPUT, TransformationDirection.JSON_TO_XML);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected JsonArrayTransformationDriver createDriver() {
    return new JsonArrayTransformationDriver();
  }
}
