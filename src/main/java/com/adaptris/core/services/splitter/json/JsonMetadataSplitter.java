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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("json-metadata-splitter")
public class JsonMetadataSplitter extends MessageSplitterImp
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonMetadataSplitter.class.getName());

	@Override
	public List<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException
	{
		final List<AdaptrisMessage> result = new ArrayList<>();
		final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);

		try
		{
			for (final MetadataElement me : message.getMetadata())
			{
				final JsonObjectSplitter jos = new JsonObjectSplitter();
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
						result.addAll(jos.splitMessage(array, message));
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
							result.add(jos.createSplitMessage(o, message));
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
}
