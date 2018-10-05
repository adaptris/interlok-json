package com.adaptris.core.services.splitter.json;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Message splitter implementation that splits a JSON array so that each element forms a new message.
 *
 * <p>
 * If the message cannot be parsed as JSON then an exception will be thrown; If the message is a JSON object but not a JSON array,
 * then the original message is returned. Note that because it operates on the entire payload, size of message considerations may be
 * in order.
 * </p>
 * <p>
 * For instance the JSON array <code>
[{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}]</code>
 * would be split into 4 messages whereas
 * <code>{"colours" : [{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}] }</code>
 * would remain a single message.
 * </p>
 *
 * @config json-array-splitter
 * @author lchan
 *
 */
@XStreamAlias("json-array-splitter")
public class JsonArraySplitter extends JsonObjectSplitter {

	/**
	 * Split an JSON array message. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message.
	 *
	 * @return A list of Adaptris messages for each JSON object in array.
	 */
	@Override
	public List<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException {
		final List<AdaptrisMessage> result = new ArrayList<>();

    try (InputStream in = message.getInputStream()) {

			final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
      final Object object = jsonParser.parse(in);

			if (object instanceof JSONObject) {

				result.add(message);

			} else if (object instanceof JSONArray) {

				final JSONArray array = (JSONArray)object;
				final List<AdaptrisMessage> splitMessages = splitMessage(array, message);
				result.addAll(splitMessages);

			} else {
        throw new CoreException(
            "Message payload was not JSON; could not be parsed to " + JSONObject.class + " from " + object.getClass());
			}

		} catch (final Exception e) {
			LOGGER.error("Could not parse or split JSON array payload.", e);
      throw ExceptionHelper.wrapCoreException(e);
		}

		return result;
	}
}
