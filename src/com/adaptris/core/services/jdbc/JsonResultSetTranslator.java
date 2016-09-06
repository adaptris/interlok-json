package com.adaptris.core.services.jdbc;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service to translate a JDBC result set to JSON.
 *
 * @author Ashley Anderson <ashley.anderson@reedbusiness.com>
 */
@XStreamAlias("jdbc-json-payload-translator")
public class JsonResultSetTranslator implements ResultSetTranslator {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonResultSetTranslator.class.getName());

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void init() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void start() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void stop() {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void close() {
		/* unused/empty method */
	}

	/**
	 * Unused method. For more information see {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException {
		/* unused/empty method */
	}

	/**
	 * Perform JDBC data set to JSON translation.
	 *
	 * {@inheritDoc}.
	 */
	@Override
	public void translate(final JdbcResult source, final AdaptrisMessage target) {
		final JSONObject json = new JSONObject();

		for (final JdbcResultSet result : source.getResultSets()) {

			/* add result set to JSON array */
			final JSONArray jsonArray = new JSONArray();
			for (final JdbcResultRow row : result.getRows()) {

				/* add row data to JSON object */
				final JSONObject jsonRow = new JSONObject();
				for (final String field : row.getFieldNames()) {

					try {
						jsonRow.put(field, row.getFieldValue(field));
					} catch (final JSONException e) {
						LOGGER.warn("Could not create JSON from object for field : " + field, e);
					}

				}

				jsonArray.put(jsonRow);
			}

			try {
				json.put("result", jsonArray);
			} catch (final JSONException e) {
				LOGGER.error("Could not create JSON result", e);
			}

		}

		target.setPayload(json.toString().getBytes());
	}
}
