package com.adaptris.core.transform.json;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.ServiceException;

public class JsonObjectTransformationDriverTest extends DefaultTransformDriverTest {


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Override
  @Test
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
