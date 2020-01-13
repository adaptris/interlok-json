package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import net.sf.json.xml.XMLSerializer;

/**
 * Unit tests for DefaultJsonTransformationDriver and child classes.
 */
public class DefaultTransformDriverTest extends BaseCase {

  /**
   * Test string.
   */
  private static final String ABC = "abc";


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  private XMLSerializer serializer;

  @Before
  public void setUp() {
    serializer = new XMLSerializer();
  }

  /**
   * Test array name.
   */
  @Test
  public void testArrayName() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getArrayName());
    assertEquals(serializer.getArrayName(), driver.arrayName());

    driver.setArrayName(ABC);

    assertEquals(ABC, driver.getArrayName());
  }

  @Test
  public void testElementName() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getElementName());
    assertEquals(serializer.getElementName(), driver.elementName());

    driver.setElementName(ABC);

    assertEquals(ABC, driver.getElementName());
  }

  @Test
  public void testObjectName() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getObjectName());
    assertEquals(serializer.getObjectName(), driver.objectName());

    driver.setObjectName(ABC);

    assertEquals(ABC, driver.getObjectName());
  }

  @Test
  public void testRootName() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getRootName());
    assertEquals(serializer.getRootName(), driver.rootName());

    driver.setRootName(ABC);

    assertEquals(ABC, driver.getRootName());
  }

  @Test
  public void testForceTopLevelObject() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getForceTopLevelObject());
    assertEquals(serializer.isForceTopLevelObject(), driver.forceTopLevelObject());

    driver.setForceTopLevelObject(true);

    assertTrue(driver.getForceTopLevelObject());
  }

  @Test
  public void testSkipWhiteSpace() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getSkipWhitespace());
    assertEquals(serializer.isSkipWhitespace(), driver.skipWhitespace());

    driver.setSkipWhitespace(true);

    assertTrue(driver.getSkipWhitespace());
  }

  @Test
  public void testTrimSpaces() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getTrimSpaces());
    assertEquals(serializer.isTrimSpaces(), driver.trimSpaces());

    driver.setTrimSpaces(true);

    assertTrue(driver.getTrimSpaces());
  }

  @Test
  public void testTypeHintsCompatibility() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getTypeHintsCompatibility());
    assertEquals(serializer.isTypeHintsCompatibility(), driver.typeHintsCompatibility());

    driver.setTypeHintsCompatibility(true);

    assertTrue(driver.getTypeHintsCompatibility());
  }

  @Test
  public void testTypeHintsEnabled() {
    DefaultJsonTransformationDriver driver = createDriver();
    assertNull(driver.getTypeHintsEnabled());
    assertEquals(serializer.isTypeHintsEnabled(), driver.typeHintsEnabled());

    driver.setTypeHintsEnabled(true);

    assertTrue(driver.getTypeHintsEnabled());
  }

  @Test
  public void testJsonToXml() throws Exception {
    DefaultJsonTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.JSON_INPUT, TransformationDirection.JSON_TO_XML);
    assertNotNull(s);
  }

  @Test
  public void testJsonArrayToXml() throws Exception {
    DefaultJsonTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.ARRAY_JSON_INPUT, TransformationDirection.JSON_TO_XML);
    assertNotNull(s);
  }

  @Test
  public void testXmlToJson() throws Exception {
    DefaultJsonTransformationDriver driver = createDriver();
    String s = driver.transform(JsonXmlTransformServiceTest.DEFAULT_XML_INPUT, TransformationDirection.XML_TO_JSON);
    assertNotNull(s);
  }

  protected DefaultJsonTransformationDriver createDriver() {
    return new DefaultJsonTransformationDriver();
  }
}
