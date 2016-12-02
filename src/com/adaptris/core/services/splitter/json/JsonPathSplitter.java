package com.adaptris.core.services.splitter.json;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.services.path.json.JsonPathService;
import com.adaptris.core.services.splitter.MessageSplitter;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JSON path splitter.
 */
@XStreamAlias("json-path-splitter")
public class JsonPathSplitter extends MessageSplitterImp {

	private DataInputParameter<String> jsonSource;

	private DataInputParameter<String> jsonPath;

	private MessageSplitter messageSplitter;

	/**
	 * Split JSON path. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message with the JSON payload.
	 */
	@Override
	public Iterable<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException {
		try {

			final String extractedMessage = jsonPath.extract(message);

			final ConstantDataInputParameter source = new ConstantDataInputParameter(extractedMessage);
			final StringPayloadDataOutputParameter target = new StringPayloadDataOutputParameter();
			final Execution execution = new Execution(source, target);

			final JsonPathService jsonPathService = new JsonPathService();
			jsonPathService.setSource(jsonSource);
			jsonPathService.setExecutions(Arrays.asList(execution));
      execute(jsonPathService, message);
			return messageSplitter.splitMessage(message);
		} catch (final InterlokException ex) {
      throw ExceptionHelper.wrapCoreException(ex);
		}
	}

  public static void execute(Service s, AdaptrisMessage msg) throws CoreException {
    try {
      s.prepare();
      LifecycleHelper.init(s);
      LifecycleHelper.start(s);
      s.doService(msg);
    }
    finally {
      LifecycleHelper.stop(s);
      LifecycleHelper.close(s);
    }
  }

	/**
	 * Get the JSON source.
	 *
	 * @return The JSON source.
	 */
	public DataInputParameter<String> getJsonSource() {
		return jsonSource;
	}

	/**
	 * Set the JSON source.
	 *
	 * @param jsonSource
	 *          The JSON source.
	 */
	public void setJsonSource(final DataInputParameter<String> jsonSource) {
		this.jsonSource = jsonSource;
	}

	/**
	 * Get the JSON path.
	 *
	 * @return The JSON path.
	 */
	public DataInputParameter<String> getJsonPath() {
		return jsonPath;
	}

	/**
	 * Set the JSON path.
	 *
	 * @param jsonPath
	 *          The JSON path.
	 */
	public void setJsonPath(final DataInputParameter<String> jsonPath) {
		this.jsonPath = jsonPath;
	}

	/**
	 * Get the message splitter.
	 *
	 * @return The message splitter.
	 */
	public MessageSplitter getMessageSplitter() {
		return messageSplitter;
	}

	/**
	 * Set the message splitter.
	 *
	 * @param messageSplitter
	 *          The message splitter.
	 */
	public void setMessageSplitter(final MessageSplitter messageSplitter) {
		this.messageSplitter = messageSplitter;
	}
}
