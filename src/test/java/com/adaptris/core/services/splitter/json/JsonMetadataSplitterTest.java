package com.adaptris.core.services.splitter.json;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.transform.json.JsonToXmlTransformServiceTest;

import com.adaptris.core.util.CloseableIterable;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonMetadataSplitterTest extends SplitterServiceExample
{
	public JsonMetadataSplitterTest(final String name)
	{
		super(name);
	}

	public static final String PAYLOAD = "[\n"
			+ "{\"colour\": \"red\"},\n"
			+ "{\"colour\": \"green\"},\n"
			+ "{\"colour\": \"blue\"},\n"
			+ "{\"colour\": \"black\"}\n"
			+ "]";

	private static final String METADATA = "a=b,b=c";

	public void testSplitArray() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);

		try (final CloseableIterable<AdaptrisMessage> i = createSplitter().splitMessage(message))
		{
			for (final AdaptrisMessage m : i)
			{
				assertNull(m.getPayload());
				switch (m.getMetadataValue("colour"))
				{
					case "red":
					case "green":
					case "blue":
					case "black":
						break;
					default:
						fail();
				}
			}
		}
	}
/*
	public void testSplitObject() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata("json", JsonToXmlTransformServiceTest.JSON_INPUT);

		final List<AdaptrisMessage> messages = new JsonMetadataSplitter().splitMessage(message);

		assertEquals(3, messages.size());

		JSONObject jsonObj = (JSONObject)JSONSerializer.toJSON(messages.get(0).getMetadataValue("json"));
		assertTrue(jsonObj.containsKey("entry"));

		jsonObj = (JSONObject)JSONSerializer.toJSON(messages.get(1).getMetadataValue("json"));
		assertTrue(jsonObj.containsKey("notes"));

		jsonObj = (JSONObject)JSONSerializer.toJSON(messages.get(2).getMetadataValue("json"));
		assertTrue(jsonObj.containsKey("version"));
	}

	public void testSplitEmptyObject() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata("json", "{}");

		final List<AdaptrisMessage> messages = new JsonMetadataSplitter().splitMessage(message);

		assertEquals(1, messages.size());
		assertEquals(1, messages.get(0).getMetadata().size());
		assertTrue(((JSONObject)JSONSerializer.toJSON(messages.get(0).getMetadataValue("json"))).isEmpty());
	}

	public void testSplitNotJson()
	{
		try
		{
			final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
			message.addMetadata("json", "hello world");

			new JsonMetadataSplitter().splitMessage(message);
			fail();
		}
		catch (CoreException expected)
		{
			// expected behaviour
		}
	}*/

	@Override
	protected String getExampleCommentHeader(Object o)
	{
		return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n"
				+ JsonToXmlTransformServiceTest.JSON_INPUT
				+ "\n\nthis would create 3 new messages, 1 each for 'entry', 'notes', 'version'." + "\nIf the incoming document is \n\n"
				+ PAYLOAD + "\n\nthis would create 4 messages, 1 for each element" + "\n-->\n";
	}

	@Override
	JsonMetadataSplitter createSplitter()
	{
		return new JsonMetadataSplitter();
	}
}
