package com.adaptris.core.transform.json;

import net.sf.json.xml.XMLSerializer;

import com.adaptris.core.BaseCase;

public class JsonObjectTransformDriverTest extends BaseCase {

  public JsonObjectTransformDriverTest(String name) {
    super(name);
  }

  public void testArrayName() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getArrayName());
    assertEquals(serializer.getArrayName(), driver.arrayName());
    driver.setArrayName("abc");
    assertEquals("abc", driver.getArrayName());
    assertEquals("abc", driver.arrayName());
  }

  public void testElementName() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getElementName());
    assertEquals(serializer.getElementName(), driver.elementName());
    driver.setElementName("abc");
    assertEquals("abc", driver.getElementName());
    assertEquals("abc", driver.elementName());
  }

  public void testObjectName() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getObjectName());
    assertEquals(serializer.getObjectName(), driver.objectName());
    driver.setObjectName("abc");
    assertEquals("abc", driver.getObjectName());
    assertEquals("abc", driver.objectName());
  }

  public void testRootName() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getRootName());
    assertEquals(serializer.getRootName(), driver.rootName());
    driver.setRootName("abc");
    assertEquals("abc", driver.getRootName());
    assertEquals("abc", driver.rootName());
  }


  public void testForceTopLevelObject() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getForceTopLevelObject());
    assertEquals(serializer.isForceTopLevelObject(), driver.isForceTopLevelObject());
    driver.setForceTopLevelObject(Boolean.TRUE);
    assertEquals(Boolean.TRUE, driver.getForceTopLevelObject());
    assertEquals(true, driver.isForceTopLevelObject());
  }

  public void testSkipWhiteSpace() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getSkipWhitespace());
    assertEquals(serializer.isSkipWhitespace(), driver.isSkipWhitespace());
    driver.setSkipWhitespace(Boolean.TRUE);
    assertEquals(Boolean.TRUE, driver.getSkipWhitespace());
    assertEquals(true, driver.isSkipWhitespace());
  }

  public void testTrimSpaces() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getTrimSpaces());
    assertEquals(serializer.isTrimSpaces(), driver.isTrimSpaces());
    driver.setTrimSpaces(Boolean.TRUE);
    assertEquals(Boolean.TRUE, driver.getTrimSpaces());
    assertEquals(true, driver.isTrimSpaces());
  }

  public void testTypeHintsCompatibility() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getTypeHintsCompatibility());
    assertEquals(serializer.isTypeHintsCompatibility(), driver.isTypeHintsCompatibility());
    driver.setTypeHintsCompatibility(Boolean.TRUE);
    assertEquals(Boolean.TRUE, driver.getTypeHintsCompatibility());
    assertEquals(true, driver.isTypeHintsCompatibility());
  }

  public void testTypeHintsEnabled() throws Exception {
    JsonObjectTransformationDriver driver = createDriver();
    XMLSerializer serializer = new XMLSerializer();
    assertNull(driver.getTypeHintsEnabled());
    assertEquals(serializer.isTypeHintsEnabled(), driver.isTypeHintsEnabled());
    driver.setTypeHintsEnabled(Boolean.TRUE);
    assertEquals(Boolean.TRUE, driver.getTypeHintsEnabled());
    assertEquals(true, driver.isTypeHintsEnabled());
  }

  protected JsonObjectTransformationDriver createDriver() {
    return new JsonObjectTransformationDriver();
  }
}
