package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

  public static final String XML_OUTPUT = "<json><entry><location>Seattle</location><name>Production System</name></entry>"
      + "<entry><location>New York</location><name>R&amp;D sandbox</name></entry>"
      + "<notes>Some Notes</notes><version>0.5</version></json>";

  public static final String JSON_INPUT =
      "{\n\"entry\":[\n" + "{\n\"location\":\"Seattle\"," + "\n\"name\":\"Production System\"},\n" + "{\"location\":\"New York\",\n"
          + "\"name\":\"R&D sandbox\"\n" + "}\n" + "],\n" + "\"notes\":\"Some Notes\",\n" + "\"version\":0.5\n" + "}";
  public static final String XML_INPUT =
      "<json>\n" + "<version>0.5</version>\n" + "" + "<entry>\n" + "<name>Production System</name>\n"
      + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n" + "<name>R&amp;D sandbox</name>\n"
      + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n" + "</json>\n";

  public static final String JSON_OUTPUT =
      "{\"entry\":[{\"location\":\"Seattle\",\"name\":\"Production System\"},{\"location\":\"New York\",\"name\":\"R&D sandbox\"}],\"notes\":\"Some Notes\",\"version\":0.5}";

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
    System.err.println(msg.getContent());
    ServiceCase.execute(toJson, msg);
    System.err.println(msg.getContent());
    ReadContext context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.object.secondary.values"));
    assertEquals("", context.read("$.object.primary.value"));
    assertEquals("", context.read("$.object.watermark"));
    JsonTransformService transform = new JsonTransformService(
        new ConstantDataInputParameter("[{\"operation\": \"com.adaptris.core.transform.json.jolt.NullToEmptyString\"}]"));
    ServiceCase.execute(transform, msg);
    context = JsonPath.parse(msg.getInputStream(), jsonConfig);
    assertNotNull(context.read("$.object.secondary.values"));
    assertEquals("", context.read("$.object.primary.value"));
    assertEquals("", context.read("$.object.watermark"));
  }
}
