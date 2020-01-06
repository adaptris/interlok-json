package com.adaptris.core.services.splitter.json;

import java.util.ArrayList;
import java.util.Arrays;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.json.JsonPathExecution;
import com.adaptris.core.services.path.json.JsonPathService;
import com.adaptris.core.services.splitter.MessageSplitter;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JSON path splitter.
 * 
 * @config json-path-splitter
 */
@XStreamAlias("json-path-splitter")
@ComponentProfile(summary = "Split a JSON Array specified by a JSON path into indvidual JSON objects", tag = "json,splitting")
public class JsonPathSplitter extends MessageSplitterImp {

  @NotNull
  @Valid
  @AutoPopulated
	private DataInputParameter<String> jsonSource;

  @NotNull
  @Valid
	private DataInputParameter<String> jsonPath;

  @NotNull
  @Valid
  @AutoPopulated
	private MessageSplitter messageSplitter;

  public JsonPathSplitter() {
    setJsonSource(new StringPayloadDataInputParameter());
    setMessageSplitter(new JsonArraySplitter());
  }

  public JsonPathSplitter(DataInputParameter<String> source, DataInputParameter<String> path) {
    this();
    setJsonSource(source);
    setJsonPath(path);
  }

	/**
	 * Split JSON path. {@inheritDoc}.
	 *
	 * @param message
	 *          The Adaptris message with the JSON payload.
	 */
	@Override
	public Iterable<AdaptrisMessage> splitMessage(final AdaptrisMessage message) throws CoreException {
		try {

      final String jsonPathToUse = jsonPath.extract(message);
      // INTERLOK-1651 Work on a "clone" of the message so the original remains
      // untouched.
      AdaptrisMessage clone = selectFactory(message).newMessage(message, null);
      clone.setContent(message.getContent(), message.getContentEncoding());

      final ConstantDataInputParameter source = new ConstantDataInputParameter(jsonPathToUse);
      final StringPayloadDataOutputParameter target = new StringPayloadDataOutputParameter();
      final JsonPathExecution execution = new JsonPathExecution(source, target);

      final JsonPathService jsonPathService = new JsonPathService(jsonSource, new ArrayList<Execution>(Arrays.asList(execution)));
      execute(jsonPathService, clone);

      return getMessageSplitter().splitMessage(clone);
    } catch (final InterlokException | CloneNotSupportedException ex) {
      throw ExceptionHelper.wrapCoreException(ex);
		}
	}

  public static void execute(Service s, AdaptrisMessage msg) throws CoreException {
    try {
      LifecycleHelper.prepare(s);
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
    this.jsonPath = Args.notNull(jsonPath, "jsonPath");;
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
    this.messageSplitter = Args.notNull(messageSplitter, "messageSplitter");
	}
}
