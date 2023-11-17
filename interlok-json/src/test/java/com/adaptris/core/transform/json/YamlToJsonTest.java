package com.adaptris.core.transform.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;

public class YamlToJsonTest extends TransformServiceExample {

  private static final String SAMPLE_YAML = "schemes:" + System.lineSeparator() + "- http";
  private static final String EXPECTED_JSON = "{\"schemes\":[\"http\"]}";


  @Test
  public void testService() throws Exception {
    YamlToJsonService service = new YamlToJsonService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SAMPLE_YAML);
    execute(service, msg);
    assertEquals(EXPECTED_JSON, msg.getContent());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new YamlToJsonService();
  }

}
