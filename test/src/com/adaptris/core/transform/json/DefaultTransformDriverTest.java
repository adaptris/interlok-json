package com.adaptris.core.transform.json;

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

	/**
	 * Constructor.
	 *
	 * @param name
	 *          Test name.
	 */
	public DefaultTransformDriverTest(final String name) {
		super(name);
	}

	/**
	 * JSON transformation driver.
	 */
	private DefaultJsonTransformationDriver driver;

	/**
	 * XML serializer.
	 */
	private XMLSerializer serializer;

	@Override
	@Before
	public void setUp() {
		driver = new DefaultJsonTransformationDriver();
		serializer = new XMLSerializer();
	}

	/**
	 * Test array name.
	 */
	@Test
	public void testArrayName() {
		assertNull(driver.getArrayName());
		assertEquals(serializer.getArrayName(), driver.arrayName());

		driver.setArrayName(ABC);

		assertEquals(ABC, driver.getArrayName());
		assertEquals(ABC, driver.arrayName());
	}

	/**
	 * Test element name.
	 */
	@Test
	public void testElementName() {
		assertNull(driver.getElementName());
		assertEquals(serializer.getElementName(), driver.elementName());

		driver.setElementName(ABC);

		assertEquals(ABC, driver.getElementName());
		assertEquals(ABC, driver.elementName());
	}

	/**
	 * Test object name.
	 */
	@Test
	public void testObjectName() {
		assertNull(driver.getObjectName());
		assertEquals(serializer.getObjectName(), driver.objectName());

		driver.setObjectName(ABC);

		assertEquals(ABC, driver.getObjectName());
		assertEquals(ABC, driver.objectName());
	}

	/**
	 * Test root name.
	 */
	@Test
	public void testRootName() {
		assertNull(driver.getRootName());
		assertEquals(serializer.getRootName(), driver.rootName());

		driver.setRootName(ABC);

		assertEquals(ABC, driver.getRootName());
		assertEquals(ABC, driver.rootName());
	}

	/**
	 * Test top-level object.
	 */
	@Test
	public void testForceTopLevelObject() {
		assertNull(driver.getForceTopLevelObject());
		assertEquals(serializer.isForceTopLevelObject(), driver.isForceTopLevelObject());

		driver.setForceTopLevelObject(true);

		assertTrue(driver.getForceTopLevelObject());
		assertTrue(driver.isForceTopLevelObject());
	}

	/**
	 * Test skipping whitespace.
	 */
	@Test
	public void testSkipWhiteSpace() {
		assertNull(driver.getSkipWhitespace());
		assertEquals(serializer.isSkipWhitespace(), driver.isSkipWhitespace());

		driver.setSkipWhitespace(true);

		assertTrue(driver.getSkipWhitespace());
		assertTrue(driver.isSkipWhitespace());
	}

	/**
	 * Test trimming whitespace.
	 */
	@Test
	public void testTrimSpaces() {
		assertNull(driver.getTrimSpaces());
		assertEquals(serializer.isTrimSpaces(), driver.isTrimSpaces());

		driver.setTrimSpaces(true);

		assertTrue(driver.getTrimSpaces());
		assertTrue(driver.isTrimSpaces());
	}

	/**
	 * Test hints compatibility.
	 */
	@Test
	public void testTypeHintsCompatibility() {
		assertNull(driver.getTypeHintsCompatibility());
		assertEquals(serializer.isTypeHintsCompatibility(), driver.isTypeHintsCompatibility());

		driver.setTypeHintsCompatibility(true);

		assertTrue(driver.getTypeHintsCompatibility());
		assertTrue(driver.isTypeHintsCompatibility());
	}

	/**
	 * Test hints enabled.
	 */
	@Test
	public void testTypeHintsEnabled() {
		assertNull(driver.getTypeHintsEnabled());
		assertEquals(serializer.isTypeHintsEnabled(), driver.isTypeHintsEnabled());

		driver.setTypeHintsEnabled(true);

		assertTrue(driver.getTypeHintsEnabled());
		assertTrue(driver.isTypeHintsEnabled());
	}
}
