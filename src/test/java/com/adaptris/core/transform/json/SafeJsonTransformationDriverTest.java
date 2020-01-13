package com.adaptris.core.transform.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.EnumSet;
import org.junit.Test;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class SafeJsonTransformationDriverTest extends DefaultTransformDriverTest {

  private static final String FORMATTED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"+
      "<o>\r\n" + 
      "    <animals class=\"array\">\r\n" + 
      "        <e class=\"object\">\r\n" + 
      "            <id type=\"string\">1</id>\r\n" + 
      "            <name type=\"string\">hi</name>\r\n" + 
      "        </e>\r\n" + 
      "        <e class=\"object\">\r\n" + 
      "            <id type=\"string\">1</id>\r\n" + 
      "            <name type=\"string\">hi</name>\r\n" + 
      "        </e>\r\n" + 
      "    </animals>\r\n" + 
      "</o>";


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Override
  @Test
  public void testXmlToJson() throws Exception {
    SafeJsonTransformationDriver driver = createDriver();
    assertNotNull(driver.transform(JsonXmlTransformServiceTest.DEFAULT_XML_INPUT, TransformationDirection.XML_TO_JSON));
    assertNotNull(driver.transform(JsonXmlTransformServiceTest.DEFAULT_XML_INPUT, TransformationDirection.XML_TO_JSON));
  }

  @Test
  public void testFormattedXML_ToJson() throws Exception {
    SafeJsonTransformationDriver driver = createDriver();
    String s = driver.transform(FORMATTED_XML, TransformationDirection.XML_TO_JSON);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(s, jsonConfig);
    assertNotNull(context.read("$.animals"));
  }

  @Test
  public void testFormattedXML_ToJson_NotSafe() throws Exception {
    DefaultJsonTransformationDriver driver = new DefaultJsonTransformationDriver();
    String s = driver.transform(FORMATTED_XML, TransformationDirection.XML_TO_JSON);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(s, jsonConfig);
    try {
      context.read("$.animals");
      fail();
    } catch (PathNotFoundException expected) {
      
    }
  }

  @Override
  protected SafeJsonTransformationDriver createDriver() {
    return new SafeJsonTransformationDriver();
  }
}
