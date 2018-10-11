package com.adaptris.core.services.splitter.json;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.CloseableIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * Split an arbitrarily large JSON array.
 *
 * <p>
 * Note: tested with an 85Mb file containing an array of >15k JSON objects
 * </p>
 *
 * @config json-metadata-splitter
 */
@XStreamAlias("json-metadata-splitter")
public class JsonMetadataSplitter extends MessageSplitterImp
{
	private transient static final int DEFAULT_BUFFER_SIZE = 8192;

	@AdvancedConfig
	private Integer bufferSize;

	@Override
	@SuppressWarnings("deprecation")
	public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage adaptrisMessage) throws CoreException
	{
		try
		{
			final BufferedReader bufferedReader = new BufferedReader(adaptrisMessage.getReader(), bufferSize());
			final ObjectMapper objectMapper = new ObjectMapper();
			final JsonParser parser = objectMapper.getFactory().createParser(bufferedReader);
			if (parser.nextToken() != JsonToken.START_ARRAY)
			{
				IOUtils.closeQuietly(bufferedReader);
				IOUtils.closeQuietly(parser);
				throw new CoreException("Expected an array");
			}
			return new JsonMetadataSplitGenerator(adaptrisMessage, bufferedReader, parser, objectMapper);
		}
		catch (final IOException e)
		{
			throw new CoreException(e);
		}
	}

	protected class JsonMetadataSplitGenerator implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage>
	{
		private final JsonParser parser;
		private final Reader reader;
		private final transient ObjectMapper mapper;

		private final AdaptrisMessageFactory factory;
		private final transient AdaptrisMessage originalMessage;
		private transient AdaptrisMessage nextMessage;

		protected JsonMetadataSplitGenerator(final AdaptrisMessage originalMessage, final Reader reader, final JsonParser parser, final ObjectMapper mapper)
		{
			this.originalMessage = originalMessage;
			this.reader = reader;
			this.parser = parser;
			this.mapper = mapper;

			factory = selectFactory(originalMessage);
			logR.trace("Using message factory: {}", factory.getClass());
		}

		@Override
		public Iterator<AdaptrisMessage> iterator()
		{
			return this;
		}

		@Override
		public boolean hasNext()
		{
			if (nextMessage == null)
			{
				try
				{
					nextMessage = splitMessage();
				}
				catch (final IOException e)
				{
					throw new RuntimeException("Could not construct next AdaptrisMessage", e);
				}
			}
			return nextMessage != null;
		}

		@Override
		public AdaptrisMessage next()
		{
			final AdaptrisMessage ret = nextMessage;
			nextMessage = null;
			return ret;
		}

		protected AdaptrisMessage splitMessage() throws IOException
		{
			final AdaptrisMessage splitMessage = factory.newMessage();
			if (parser.nextToken() == JsonToken.START_OBJECT)
			{
				final ObjectNode objectNode = mapper.readTree(parser);
				final Iterator<String> fields = objectNode.fieldNames();
				while (fields.hasNext())
				{
					final String field = fields.next();
					final String value = objectNode.get(field).textValue();
					splitMessage.addMetadata(field, value);
				}
				copyMetadata(originalMessage, splitMessage);
				return splitMessage;
			}
			return null;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void close()
		{
			IOUtils.closeQuietly(parser);
			IOUtils.closeQuietly(reader);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	public Integer getBufferSize()
	{
		return bufferSize;
	}

	/**
	 * Set the internal buffer size.
	 * <p>
	 * This is used when; the default buffer size matches the default buffer size in {@link BufferedReader} and {@link BufferedWriter}
	 * , changes to the buffersize will impact performance and memory usage depending on the underlying operating system/disk.
	 * </p>
	 *
	 * @param b
	 * 		the buffer size (default is 8192).
	 */
	public void setBufferSize(final Integer b)
	{
		this.bufferSize = b;
	}

	private int bufferSize()
	{
		return getBufferSize() != null ? getBufferSize().intValue() : DEFAULT_BUFFER_SIZE;
	}

	public JsonMetadataSplitter withBufferSize(final Integer i)
	{
		setBufferSize(i);
		return this;
	}

	public JsonMetadataSplitter withMessageFactory(final AdaptrisMessageFactory messageFactory)
	{
		setMessageFactory(messageFactory);
		return this;
	}
}
