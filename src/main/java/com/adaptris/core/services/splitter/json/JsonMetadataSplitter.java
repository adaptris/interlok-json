package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("json-metadata-splitter")
public class JsonMetadataSplitter extends MessageSplitterImp
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonMetadataSplitter.class.getName());

	/**
	 * Split a JSON payload from an Adaptris message metadata. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message.
	 *
	 * @return A list of Adaptris messages for each JSON object.
	 */
	@Override
	public List<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException
	{
		final List<AdaptrisMessage> result = new ArrayList<>();
		final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);

		try
		{
			for (final MetadataElement me : message.getMetadata())
			{
				final Object object = jsonParser.parse(message.getMetadataValue(me.getKey()));
				if (object instanceof JSONArray)
				{
					final JSONArray array = (JSONArray)object;
					if (array.isEmpty())
					{
						result.add(message);
					}
					else
					{
						result.addAll(splitMessage(array, message));
					}
				}
				else if (object instanceof JSONObject)
				{
					final JSONObject json = (JSONObject)object;
					if (json.isEmpty())
					{
						result.add(message);
					}
					else
					{
						for (final String key : json.keySet())
						{
							final JSONObject o = new JSONObject();
							o.put(key, json.get(key));
							result.add(createSplitMessage(o, message));
						}
					}
				}
				else
				{
					throw new CoreException("Message payload was not JSON; could not be parsed to " + JSONObject.class + " from " + object.getClass());
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not parse or split JSON object payload.", e);
			throw ExceptionHelper.wrapCoreException(e);
		}
		return result;
	}

	/**
	 * Split a JSON array into a list of Adaptris messages for each JSON array element.
	 *
	 * @param array
	 * 		The JSON array.
	 * @param message
	 * 		The original Adaptris message.
	 *
	 * @return A list of Adaptris messages.
	 *
	 * @throws IOException
	 * 		If IOUtils cannot copy from a Reader to a Writer.
	 */
	private List<AdaptrisMessage> splitMessage(final JSONArray array, final AdaptrisMessage message) throws IOException
	{
		final List<AdaptrisMessage> result = new ArrayList<>();
		for (final Object element : array)
		{
			final AdaptrisMessage splitMessage;
			if (element instanceof JSONObject)
			{
				splitMessage = createSplitMessage((JSONObject)element, message);
			}
			else
			{
				splitMessage = createSplitMessage(element.toString(), message);
			}
			result.add(splitMessage);
		}
		return result;
	}

	/**
	 * Create a new Adaptris message for the given JSON object.
	 *
	 * @param json
	 * 		The JSON object.
	 * @param message
	 * 		The original Adaptris message.
	 *
	 * @return A new Adaptris message for the JSON object.
	 *
	 * @throws IOException
	 * 		If IOUtils cannot copy from a Reader to a Writer.
	 */
	private AdaptrisMessage createSplitMessage(final JSONObject json, final AdaptrisMessage message) throws IOException
	{
		return createSplitMessage(json.toJSONString(), message);
	}

	/**
	 * Create a new Adaptris message for the given JSON object.
	 *
	 * @param json
	 * 		The JSON string.
	 * @param message
	 * 		The original Adaptris message.
	 *
	 * @return A new Adaptris message for the JSON object.
	 *
	 * @throws IOException
	 * 		If IOUtils cannot copy from a Reader to a Writer.
	 */
	private AdaptrisMessage createSplitMessage(final String json, final AdaptrisMessage message) throws IOException
	{
		final AdaptrisMessage newMessage = selectFactory(message).newMessage();
		newMessage.addMetadata("json", json);
		try (final Reader reader = new StringReader(json);
		     final Writer writer = newMessage.getWriter())
		{
			IOUtils.copy(reader, writer);
		}
		return newMessage;
	}
}
