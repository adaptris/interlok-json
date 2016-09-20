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
		assertEquals(serializer.getArrayName(), driver.getArrayNameOrDefault());

		driver.setArrayName(ABC);

		assertEquals(ABC, driver.getArrayName());
	}

	/**
	 * Test element name.
	 */
	@Test
	public void testElementName() {
		assertNull(driver.getElementName());
		assertEquals(serializer.getElementName(), driver.getElementNameOrDefault());

		driver.setElementName(ABC);

		assertEquals(ABC, driver.getElementName());
	}

	/**
	 * Test object name.
	 */
	@Test
	public void testObjectName() {
		assertNull(driver.getObjectName());
		assertEquals(serializer.getObjectName(), driver.getObjectNameOrDefault());

		driver.setObjectName(ABC);

		assertEquals(ABC, driver.getObjectName());
	}

	/**
	 * Test root name.
	 */
	@Test
	public void testRootName() {
		assertNull(driver.getRootName());
		assertEquals(serializer.getRootName(), driver.getRootNameOrDefault());

		driver.setRootName(ABC);

		assertEquals(ABC, driver.getRootName());
	}

	/**
	 * Test top-level object.
	 */
	@Test
	public void testForceTopLevelObject() {
		assertNull(driver.isForceTopLevelObject());
		assertEquals(serializer.isForceTopLevelObject(), driver.isForceTopLevelObjectOrDefault());

		driver.setForceTopLevelObject(true);

		assertTrue(driver.isForceTopLevelObject());
	}

	/**
	 * Test skipping whitespace.
	 */
	@Test
	public void testSkipWhiteSpace() {
		assertNull(driver.isSkipWhitespace());
		assertEquals(serializer.isSkipWhitespace(), driver.isSkipWhitespaceOrDefault());

		driver.setSkipWhitespace(true);

		assertTrue(driver.isSkipWhitespace());
	}

	/**
	 * Test trimming whitespace.
	 */
	@Test
	public void testTrimSpaces() {
		assertNull(driver.isTrimSpaces());
		assertEquals(serializer.isTrimSpaces(), driver.isTrimSpacesOrDefault());

		driver.setTrimSpaces(true);

		assertTrue(driver.isTrimSpaces());
	}

	/**
	 * Test hints compatibility.
	 */
	@Test
	public void testTypeHintsCompatibility() {
		assertNull(driver.isTypeHintsCompatibility());
		assertEquals(serializer.isTypeHintsCompatibility(), driver.isTypeHintsCompatibilityOrDefault());

		driver.setTypeHintsCompatibility(true);

		assertTrue(driver.isTypeHintsCompatibility());
	}

	/**
	 * Test hints enabled.
	 */
	@Test
	public void testTypeHintsEnabled() {
		assertNull(driver.isTypeHintsEnabled());
		assertEquals(serializer.isTypeHintsEnabled(), driver.isTypeHintsEnabledOrDefault());

		driver.setTypeHintsEnabled(true);

		assertTrue(driver.isTypeHintsEnabled());
	}
}
