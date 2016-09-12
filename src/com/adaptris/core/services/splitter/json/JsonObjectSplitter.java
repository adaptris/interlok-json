package com.adaptris.core.services.splitter.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Message splitter implementation that splits a JSON object so each entry forms a new message.
 *
 * <p>
 * If the message cannot be parsed as JSON then an exception will be thrown; If the message is an empty JSON object then the
 * original message is returned. Note that because it operates on the entire payload, size of message considerations may be in
 * order.
 * </p>
 * <p>
 * For instance the JSON Object <code>
{"entry":[{"location":"Seattle","name":"Production System"},{"location":"New York","name":"R&D sandbox"}],"notes":"Some Notes","version":0.5}</code>
 * would be split into 3 messages (the {@code entry}, {@code notes} and {@code version}). JSON arrays will be split so that each
 * element of the array becomes a separate message, so <code>
[{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}]</code>
 * would be split into 4 messages.
 * </p>
 *
 * @config json-object-splitter
 * @author lchan
 *
 */
@XStreamAlias("json-object-splitter")
public class JsonObjectSplitter extends MessageSplitterImp {

	/**
	 * Split a JSON payload from an Adaptris message. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message.
	 *
	 * @return A list of Adaptris messages.
	 */
	@Override
	public List<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException {
		final List<AdaptrisMessage> result = new ArrayList<>();
		try {

			final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			final Object object = jsonParser.parse(message.getInputStream());

			if (object instanceof JSONArray) {

				final JSONArray array = (JSONArray)object;

				if (array.isEmpty()) {
					result.add(message);
				} else {

					final List<AdaptrisMessage> splitMessages = splitMessage(array, message);
					result.addAll(splitMessages);

				}

			} else if (object instanceof JSONObject) {

				final JSONObject json = (JSONObject)object;

				if (json.isEmpty()) {
					result.add(message);
				} else {

					for (final String key : json.keySet()) {
						final JSONObject o = new JSONObject();
						o.put(key, json.get(key));

						final AdaptrisMessage splitMessage = createSplitMessage(o, message);
						result.add(splitMessage);
					}

				}

			} else {
				throw new Exception("Message payload was not JSON; could not be parsed to " + JSONObject.class + " from " + object.getClass());
			}

		} catch (final Exception e) {
			throw new CoreException(e);
		}
		return result;
	}

	/**
	 * TODO fill this in...
	 *
	 * @param array
	 * @param original
	 * @return
	 * @throws IOException
	 */
	List<AdaptrisMessage> splitMessage(final JSONArray array, final AdaptrisMessage original) throws IOException {
		final List<AdaptrisMessage> result = new ArrayList<>();
		for (final Object element : array) {
			final AdaptrisMessage splitMessage = createSplitMessage((JSONObject)element, original);
			result.add(splitMessage);
		}
		return result;
	}

	/**
	 * TODO fill this in...
	 *
	 * @param src
	 * @param original
	 * @return
	 * @throws IOException
	 */
	AdaptrisMessage createSplitMessage(final JSONObject src, final AdaptrisMessage original) throws IOException {
		final AdaptrisMessageFactory factory = selectFactory(original);
		final AdaptrisMessage dest = factory.newMessage();
		try (StringReader in = new StringReader(src.toString()); Writer out = dest.getWriter()) {
			IOUtils.copy(in, out);
			copyMetadata(original, dest);
		}
		return dest;
	}
}
