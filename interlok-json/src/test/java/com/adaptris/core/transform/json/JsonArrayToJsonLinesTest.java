package com.adaptris.core.transform.json;

import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;

public class JsonArrayToJsonLinesTest extends TransformServiceExample {

  @Override
  protected JsonArrayToJsonLines retrieveObjectForSampleConfig() {
    return new JsonArrayToJsonLines();
  }

}
