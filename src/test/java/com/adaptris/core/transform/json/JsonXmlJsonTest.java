package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

// Tests INTERLOK-1499
public class JsonXmlJsonTest {

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
  public void testJsonXmlJson() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SAMPLE_INPUT);
    JsonXmlTransformService toXml = new JsonXmlTransformService(TransformationDirection.JSON_TO_XML);
    JsonXmlTransformService toJson = new JsonXmlTransformService(TransformationDirection.XML_TO_JSON);
    ServiceCase.execute(toXml, msg);
    ServiceCase.execute(toJson, msg);
    ReadContext context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.object.secondary.values"));
    assertNull(context.read("$.object.primary.value"));
    assertNull(context.read("$.object.watermark"));
    JsonTransformService transform = new JsonTransformService(
        new ConstantDataInputParameter("[{\"operation\": \"com.adaptris.core.transform.json.jolt.NullToEmptyString\"}]"));
    ServiceCase.execute(transform, msg);
    context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.object.secondary.values"));
    assertEquals("", context.read("$.object.primary.value"));
    assertEquals("", context.read("$.object.watermark"));
  }
}
