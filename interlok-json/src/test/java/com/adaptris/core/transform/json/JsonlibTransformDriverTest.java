package com.adaptris.core.transform.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import net.sf.json.xml.XMLSerializer;

public class JsonlibTransformDriverTest {
  private static final String ABC = "abc";
  
  private XMLSerializer serializer;

  @BeforeEach
  public void setUp() {
    serializer = new XMLSerializer();
  }

  /**
   * Test array name.
   */
  @Test
  public void testArrayName() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getArrayName());
    assertEquals(serializer.getArrayName(), driver.arrayName());

    driver.setArrayName(ABC);

    assertEquals(ABC, driver.getArrayName());
  }

  @Test
  public void testElementName() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getElementName());
    assertEquals(serializer.getElementName(), driver.elementName());

    driver.setElementName(ABC);

    assertEquals(ABC, driver.getElementName());
  }

  @Test
  public void testObjectName() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getObjectName());
    assertEquals(serializer.getObjectName(), driver.objectName());

    driver.setObjectName(ABC);

    assertEquals(ABC, driver.getObjectName());
  }

  @Test
  public void testRootName() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getRootName());
    assertEquals(serializer.getRootName(), driver.rootName());

    driver.setRootName(ABC);

    assertEquals(ABC, driver.getRootName());
  }

  @Test
  public void testForceTopLevelObject() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getForceTopLevelObject());
    assertEquals(serializer.isForceTopLevelObject(), driver.forceTopLevelObject());

    driver.setForceTopLevelObject(true);

    assertTrue(driver.getForceTopLevelObject());
  }

  @Test
  public void testSkipWhiteSpace() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getSkipWhitespace());
    assertEquals(serializer.isSkipWhitespace(), driver.skipWhitespace());

    driver.setSkipWhitespace(true);

    assertTrue(driver.getSkipWhitespace());
  }

  @Test
  public void testTrimSpaces() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getTrimSpaces());
    assertEquals(serializer.isTrimSpaces(), driver.trimSpaces());

    driver.setTrimSpaces(true);

    assertTrue(driver.getTrimSpaces());
  }

  @Test
  public void testTypeHintsCompatibility() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getTypeHintsCompatibility());
    assertEquals(serializer.isTypeHintsCompatibility(), driver.typeHintsCompatibility());

    driver.setTypeHintsCompatibility(true);

    assertTrue(driver.getTypeHintsCompatibility());
  }

  @Test
  public void testTypeHintsEnabled() {
    JsonlibTransformationDriver driver = createDriver();
    assertNull(driver.getTypeHintsEnabled());
    assertEquals(serializer.isTypeHintsEnabled(), driver.typeHintsEnabled());

    driver.setTypeHintsEnabled(true);

    assertTrue(driver.getTypeHintsEnabled());
  }

  @Test
  public void testJsonToXml() throws Exception {
    JsonlibTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.JSON_INPUT, TransformationDirection.JSON_TO_XML);
    assertNotNull(s);
  }

  @Test
  public void testJsonArrayToXml() throws Exception {
    JsonlibTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.ARRAY_JSON_INPUT, TransformationDirection.JSON_TO_XML);
    assertNotNull(s);
  }

  @Test
  public void testXmlToJson() throws Exception {
    JsonlibTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.DEFAULT_XML_INPUT, TransformationDirection.XML_TO_JSON);
    assertNotNull(s);
  }

  protected <T extends JsonlibTransformationDriver> T createDriver() {
    return (T) new JsonlibTransformationDriver();
  }
}
