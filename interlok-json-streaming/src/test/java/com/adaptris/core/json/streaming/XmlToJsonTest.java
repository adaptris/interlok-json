package com.adaptris.core.json.streaming;

import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.stax.DefaultInputFactory;
import com.adaptris.stax.StaxStreamingService;

public class XmlToJsonTest extends ExampleServiceCase {

  @Override
  protected StaxStreamingService retrieveObjectForSampleConfig() {
    return new StaxStreamingService().withInputBuilder(new DefaultInputFactory())
        .withOutputBuilder(new JsonStreamingOutputFactory()
            .withConfig(new JsonStreamingConfigBuilder().withAutoArray(Boolean.TRUE).withPrettyPrint(Boolean.FALSE)));

  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XML-JSON";
  }

}
