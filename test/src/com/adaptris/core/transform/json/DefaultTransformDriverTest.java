package com.adaptris.core.transform.json;

public class DefaultTransformDriverTest extends JsonObjectTransformDriverTest {

  public DefaultTransformDriverTest(String name) {
    super(name);
  }

  protected DefaultJsonTransformationDriver createDriver() {
    return new DefaultJsonTransformationDriver();
  }

}
