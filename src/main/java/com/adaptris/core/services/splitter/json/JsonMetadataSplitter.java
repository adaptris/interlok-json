package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.CloseableIterable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias("json-metadata-splitter")
public class JsonMetadataSplitter extends LargeJsonArraySplitter
{
	private static final Logger logger = LoggerFactory.getLogger(JsonMetadataSplitter.class);

	/**
	 * Split a JSON payload from an Adaptris message metadata. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message.
	 *
	 * @return A list of Adaptris messages for each JSON object.
	 */
	@Override
	public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException
	{
		final CloseableIterable<AdaptrisMessage> splitMessages = super.splitMessage(message);
		for (final AdaptrisMessage splitMessage : splitMessages)
		{
			try
			{
				final String payload = splitMessage.getContent();

				final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				final JSONObject json = (JSONObject)jsonParser.parse(payload);
				for (final String key : json.keySet())
				{
					final String value = json.getAsString(key);
					splitMessage.addMetadata(key, value);
				}

				splitMessage.setPayload(null);
			}
			catch (final ParseException e)
			{
				logger.error("Error parsing JSON", e);
			}
		}
		return splitMessages;
	}
}
