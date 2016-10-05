package com.adaptris.core.services.path.json;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.jayway.jsonpath.JsonPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * This service allows you to search JSON content and the results are then set back into the message.
 * <p>
 * The searching works in much the same way as XPath, for more information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a> documentation.
 * </p>
 * <p>
 * By configuring the "source" and "target-destination" ({@link DataDestination}) you can specify where the JSON content
 * is sourced from and where the results of the search should be set.
 * </p>
 * <p>
 * You may configure multiple target destinations. This allows you to essentially perform multiple searches, rather than configuring
 * multiple instances of this service.
 * </br>
 * For example, if you have a message with the following payload;
 *
 * <pre>
 * {@code
{
  "store": {
    "book": [ {
      "category": "reference",
      "author": "Nigel Rees",
      "title": "Sayings of the Century",
      "price": 8.95
    }, {
      "category": "fiction",
      "author": "Evelyn Waugh",
      "title": "Sword of Honour",
      "price": 12.99
    }, {
      "category": "fiction",
      "author": "Herman Melville",
      "title": "Moby Dick",
      "isbn": "0-553-21311-3",
      "price": 8.99
    }, {
      "category": "fiction",
      "author": "J. R. R. Tolkien",
      "title": "The Lord of the Rings",
      "isbn": "0-395-19395-8",
      "price": 22.99
    } ],
    "bicycle": {
      "color": "red",
      "price": 19.95
    }
  },
  "expensive": 10
}
 * }
 * </pre>
 *
 * You could configure 2 target destinations, each one creating a new metadata item with the results of the specified search, like
 * this;
 *
 * <pre>
 * {@code
<target-destination class="json-metadata-destination">
  <configured-json-path class="constant-json-path">
    <json-path>$.store.book[0].title</json-path>
  </configured-json-path>
  <key>metadata-key-1</key>
</target-destination>
<target-destination class="json-metadata-destination">
  <configured-json-path class="constant-json-path">
    <json-path>$.store.book[1].title</json-path>
  </configured-json-path>
  <key>metadata-key-2</key>
</target-destination>
 * }
 * </pre>
 *
 * The first target above searches for the first book title, the second target searches for the second book title.
 * Each target-destination will be executed in the order they are configured and therefore with the two targets shown here, your
 * message, after the
 * service has run, will include two new metadata items;
 *
 * <ul>
 * <li>metadata-key-1 = "Sayings of the Century"</li>
 * <li>metadata-key-2 = "Sword of Honour"</li>
 * </ul>
 * </p>
 * <p>
 * Any results returned by this service will normally include the json brackets wrapping the returned value. However you can
 * configure this
 * service to unwrap the result for you, such that a value returned as "[myValue]" will now be returned as "myValue".
 * <br/>
 * The default value is false, but to override simply configure the "unwrap";
 *
 * <pre>
 * {@code
<json-path-service>
  <unwrap-json>true</unwrap-json>
  ...
</json-path-service>
 * }
 * </pre>
 * </p>
 *
 * @author amcgrath
 * @config json-path-service
 * @license BASIC
 */
@XStreamAlias("json-path-service")
@AdapterComponent
@ComponentProfile(summary = "Extract a value from a JSON document", tag = "service,transform,json,metadata")
public class JsonPathService extends ServiceImp {

	@NotNull
	@AutoPopulated
	private DataInputParameter<String> source = new StringPayloadDataInputParameter();

	@Deprecated
	private DataInputParameter<String> sourceDestination;

	@XStreamImplicit(itemFieldName = "json-path-execution")
	@NotNull
	@Valid
	@AutoPopulated
	private List<Execution> executions = new ArrayList<>();

	/**
	 * Whether to strip leading/trailing [] from the JSON.
	 */
	private boolean unwrapJson = false;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException {
		try {

			final DataInputParameter<String> src = sourceDestination != null ? sourceDestination : source;
			final String rawJson = src.extract(message);

			final JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			final Object json = jsonParser.parse(rawJson);

			for (final Execution execution : executions) {

				final DataInputParameter<String> executionSource = execution.getSource();
				final DataOutputParameter<String> executionTarget = execution.getTarget();

				/* extract the JSON path */
				final String jsonPath = executionSource.extract(message);

				final String jsonString = unwrap(JsonPath.read(json.toString(), jsonPath).toString());

				executionTarget.insert(jsonString, message);

			}

		} catch (final ParseException e) {
			log.warn("Failed to parse JSON!", e);
			throw new ServiceException(e);
		} catch (final InterlokException e) {
			log.warn("Failed to match JSON path!", e);
			throw new ServiceException(e);
		}
	}

	/**
	 * Strip (if necessary) the leading/trailing [] from the JSON.
	 *
	 * @param json
	 *          The JSON string.
	 */
	private String unwrap(final String json) {
		/* Do we need to strip the square brackets off of a value? */
		if (unwrapJson) {
			if (json.startsWith("[") && json.endsWith("]")) {
				return json.substring(1, json.length() - 1);
			}
		}
		return json;
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	protected void closeService() {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	protected void initService() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * @return The source destination.
	 *
	 * @deprecated since 3.2.0 use {@link #getSource()} instead.
	 */
	@Deprecated
	public DataInputParameter<String> getSourceDestination() {
		return sourceDestination;
	}

	/**
	 * @param sourceDestination
	 *          The source destination.
	 *
	 * @deprecated since 3.2.0 use {@link #setSource()} instead.
	 */
	@Deprecated
	public void setSourceDestination(final DataInputParameter<String> sourceDestination) {
		log.warn("source-destination deprecated; use source instead");
		this.sourceDestination = Args.notNull(sourceDestination, "sourceDestination");
	}

	/**
	 * Get the source.
	 *
	 * @return The source.
	 */
	public DataInputParameter<String> getSource() {
		return source;
	}

	/**
	 * Set the source.
	 *
	 * @param source
	 *          The source.
	 */
	public void setSource(final DataInputParameter<String> source) {
		this.source = Args.notNull(source, "source");
	}

	/**
	 * Get the list of execution.
	 *
	 * @return The list of executions.
	 */
	public List<Execution> getExecutions() {
		return executions;
	}

	/**
	 * Set the list of executions.
	 *
	 * @param executions
	 *          The list of executions.
	 */
	public void setExecutions(final List<Execution> executions) {
		this.executions = executions;
	}

	/**
	 * Get whether the JSON should be unwrapped.
	 *
	 * @return Whether the JSON should be unwrapped.
	 */
	public boolean getUnwrapJson() {
		return unwrapJson;
	}

	/**
	 * Set whether the JSON should be unwrapped.
	 *
	 * @param unwrapJson
	 *          Whether the JSON should be unwrapped.
	 */
	public void setUnwrapJson(final boolean unwrapJson) {
		this.unwrapJson = unwrapJson;
	}
}
