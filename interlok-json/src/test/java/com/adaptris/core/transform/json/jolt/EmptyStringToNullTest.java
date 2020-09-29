package com.adaptris.core.transform.json.jolt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.EnumSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.transform.json.JsonTransformService;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class EmptyStringToNullTest {

  private static final String SAMPLE_INPUT = "{" +
      "\"object\": {" +
      "    \"primary\": {" +
      "        \"value\": \"\"" +
      "    }," +
      "    \"secondary\": {" +
      "        \"values\": [3, 4, 5, 6]" +
      "    }," +
      "    \"watermark\": \"\"" +
      "}" +
      "}";

  private static final String SPEC = "[{\"operation\": \"com.adaptris.core.transform.json.jolt.EmptyStringToNull\"}]";

  private Configuration jsonConfig;

  @Before
  public void setUp() {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testTransform() throws Exception {
    JsonTransformService transform = new JsonTransformService();
    ConstantDataInputParameter spec = new ConstantDataInputParameter(SPEC);
    transform.setMappingSpec(spec);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SAMPLE_INPUT);
    ExampleServiceCase.execute(transform, msg);
    ReadContext context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.object.secondary.values"));
    assertNull(context.read("$.object.primary.value"));
    assertNull(context.read("$.object.watermark"));
  }

}
