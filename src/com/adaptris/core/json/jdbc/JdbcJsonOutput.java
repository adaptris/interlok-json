package com.adaptris.core.json.jdbc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ResultSetTranslator} that outputs as json.
 * 
 * @config jdbc-json-output
 */
@XStreamAlias("jdbc-json-output")
public class JdbcJsonOutput extends JsonResultSetTranslatorImpl {

  private transient ObjectMapper mapper = new ObjectMapper();

  public JdbcJsonOutput() {
    mapper = new ObjectMapper();
  }

	@Override
	public void translate(final JdbcResult source, final AdaptrisMessage target)throws SQLException, ServiceException {
    try (Writer w = new BufferedWriter(target.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      generator.writeStartArray();
      // now we can iterate
      for (final JdbcResultSet result : source.getResultSets()) {
        generator.writeStartObject();
        generator.writeFieldName("result");
        generator.writeStartArray();
        for (final JdbcResultRow row : result.getRows()) {
          Map<String, Object> jsonObject = new HashMap<String, Object>();
          for (final String field : row.getFieldNames()) {
            jsonObject.put(field, row.getFieldValue(field));
          }
          generator.writeObject(jsonObject);
        }
        generator.writeEndArray();
        generator.writeEndObject();
      }
      generator.writeEndArray();
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
	}

}
