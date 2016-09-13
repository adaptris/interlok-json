package com.adaptris.core.services.splitter.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
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
	 * Default logger.
	 */
	protected static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectSplitter.class.getName());

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

					result.addAll(splitMessage(array, message));

				}

			} else if (object instanceof JSONObject) {

				final JSONObject json = (JSONObject)object;

				if (json.isEmpty()) {
					result.add(message);
				} else {

					for (final String key : json.keySet()) {
						final JSONObject o = new JSONObject();
						o.put(key, json.get(key));

						result.add(createSplitMessage(o, message));
					}

				}

			} else {
				throw new Exception("Message payload was not JSON; could not be parsed to " + JSONObject.class + " from " + object.getClass());
			}

		} catch (final Exception e) {
			LOGGER.error("Could not parse or split JSON object payload.", e);
			throw new CoreException(e);
		}
		return result;
	}

	/**
	 * Split a JSON array into a list of Adaptris messages for each JSON array element.
	 *
	 * @param array
	 *          The JSON array.
	 * @param message
	 *          The original Adaptris message.
	 *
	 * @return A list of Adaptris messages.
	 *
	 * @throws IOException
	 *           If IOUtils cannot copy from a Reader to a Writer (see {@link #createSplitMessage(JSONObject, AdaptrisMessage)}.
	 */
	protected List<AdaptrisMessage> splitMessage(final JSONArray array, final AdaptrisMessage message) throws IOException {
		final List<AdaptrisMessage> result = new ArrayList<>();
		for (final Object element : array) {
			result.add(createSplitMessage((JSONObject)element, message));
		}
		return result;
	}

	/**
	 * Create a new Adaptris message for the given JSON object.
	 *
	 * @param json
	 *          The JSON object.
	 * @param message
	 *          The original Adaptris message.
	 *
	 * @return A new Adaptris message for the JSON object.
	 *
	 * @throws IOException
	 *           If IOUtils cannot copy from a Reader to a Writer.
	 */
	protected AdaptrisMessage createSplitMessage(final JSONObject json, final AdaptrisMessage message) throws IOException {
		final AdaptrisMessage newMessage = selectFactory(message).newMessage();
		try (final Reader reader = new StringReader(json.toString()); final Writer writer = newMessage.getWriter()) {
			IOUtils.copy(reader, writer);
			copyMetadata(message, newMessage);
		}
		return newMessage;
	}
}
