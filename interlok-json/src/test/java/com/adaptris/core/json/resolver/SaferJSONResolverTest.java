package com.adaptris.core.json.resolver;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.resolver.UnresolvableException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaferJSONResolverTest
{
	private static final String KEY = "greeting";
	private static final String GREETING = "Hello \"JSON resolver test\"";
	private static final String JSON_SOURCE = "{\n" +
			"  \"firstName\": \"John\",\n" +
			"  \"lastName\": \"Smith\",\n" +
			"  \"isAlive\": true,\n" +
			"  \"age\": 27,\n" +
			"  \"address\": {\n" +
			"    \"streetAddress\": \"21 2nd Street\",\n" +
			"    \"city\": \"New York\",\n" +
			"    \"state\": \"NY\",\n" +
			"    \"postalCode\": \"10021-3100\"\n" +
			"  },\n" +
			"  \"%message{key}\": \"%resolveJson{%message{" + KEY + "}}\"\n" +
			"}";
	private static final String JSON_RESOLVED = "{\"firstName\":\"John\",\"lastName\":\"Smith\",\"isAlive\":true,\"age\":27,\"address\":{\"streetAddress\":\"21 2nd Street\",\"city\":\"New York\",\"state\":\"NY\",\"postalCode\":\"10021-3100\"},\"greeting\":\"Hello \\\"JSON resolver test\\\"\"}";

	@Test
	public void testCanResolve()
	{
		assertTrue(new SaferJSONResolver().canHandle(JSON_SOURCE));
	}

	@Test
	public void testResolve()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata(KEY, GREETING);
		message.addMetadata("key", KEY);
		SaferJSONResolver resolver = new SaferJSONResolver();
		String result = resolver.resolve(JSON_SOURCE, message);
		assertEquals(JSON_RESOLVED, result);
	}

	@Test
	public void testResolveMessageContent()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_SOURCE);
		message.addMetadata(KEY, GREETING);
		message.addMetadata("key", KEY);
		SaferJSONResolver resolver = new SaferJSONResolver();
		String result = resolver.resolve(null, message);
		assertEquals(JSON_RESOLVED, result);
	}

	@Test(expected = UnresolvableException.class)
	public void testResolveNoValue()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		SaferJSONResolver resolver = new SaferJSONResolver();
		resolver.resolve(JSON_SOURCE, message);
	}

	@Test(expected = UnresolvableException.class)
	public void testNoMessage()
	{
		new SaferJSONResolver().resolve(JSON_SOURCE);
	}

	@Test(expected = UnresolvableException.class)
	public void testNullMessage()
	{
		new SaferJSONResolver().resolve(JSON_SOURCE, null);
	}

	@Test(expected = UnresolvableException.class)
	public void testNullExpression()
	{
		new SaferJSONResolver().resolve(null, null);
	}
}
