package com.adaptris.core.json.jdbc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcDataQueryService;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.services.jdbc.StyledResultTranslatorImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ResultSetTranslator} that outputs the first result set as json.
 * <p>
 * Takes the first result set and writes out each row as part of a json array. You should use this output implementation if you
 * are executing a SELECT via {@link JdbcDataQueryService}.
 * </p>
 * <pre>
   {@code
   [{   "firstName": "John",
        "lastName": "Doe"
    }, 
    {
        "firstName": "Anna",
        "lastName": "Smith"
    },
    {
        "firstName": "Peter",
        "lastName": "Jones"
   }]
   }  
 * </pre>
 * 
 * @config jdbc-json-first-resultset-output
 */
@XStreamAlias("jdbc-json-first-resultset-output")
@ComponentProfile(summary = "Output the first resultset as JSON", tag = "json,jdbc")
public class JdbcJsonOutput extends StyledResultTranslatorImp {

  protected transient ObjectMapper mapper = new ObjectMapper();

  public JdbcJsonOutput() {
    mapper = new ObjectMapper();
  }

  @Override
  public void translate(final JdbcResult source, final AdaptrisMessage target) throws SQLException, ServiceException {
    try (Writer w = new BufferedWriter(target.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      writeResultSet(firstResultSet(source), generator);
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected void writeResultSet(JdbcResultSet result, JsonGenerator generator) throws IOException {
    generator.writeStartArray();
    for (final JdbcResultRow row : result.getRows()) {
      Map<String, Object> jsonObject = new LinkedHashMap<>();

      for (final String field : row.getFieldNames()) {
        jsonObject.put(getColumnNameStyle().format(field), row.getFieldValue(field));
      }
      generator.writeObject(jsonObject);
    }
    generator.writeEndArray();
  }

  protected JdbcResultSet firstResultSet(JdbcResult result) {
    if (result.isHasResultSet()) {
      return result.getResultSet(0);
    }
    return new JdbcResultSet() {
      public Iterable<JdbcResultRow> getRows() {
        return Collections.EMPTY_LIST;
      }

      public void close() {

      }
    };
  }

  public JdbcJsonOutput withColumnStyle(ColumnStyle b) {
    setColumnNameStyle(b);
    return this;
  }
}
