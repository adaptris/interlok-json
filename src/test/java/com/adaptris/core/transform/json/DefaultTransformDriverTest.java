package com.adaptris.core.transform.json;

@SuppressWarnings("deprecation")
public class DefaultTransformDriverTest extends JsonlibTransformDriverTest {

  @Override
  protected DefaultJsonTransformationDriver createDriver() {
    return new DefaultJsonTransformationDriver();
  }
}
