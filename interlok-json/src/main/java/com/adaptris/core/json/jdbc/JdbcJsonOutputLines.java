package com.adaptris.core.json.jdbc;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcDataQueryService;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ResultSetTranslator} that outputs the first
 * result set as json.
 *
 * <p>
 * Takes the first result set and writes out each row as part of a json
 * array as a new line. You should use this output implementation if
 * you are executing a SELECT via {@link JdbcDataQueryService}.
 * </p>
 *
 * <pre>
 {@code
    [ { "firstName": "John", "lastName": "Doe" },
      { "firstName": "Anna", "lastName": "Smith" },
      { "firstName": "Peter", "lastName": "Jones" } ]
 }
 * </pre>
 *
 * @config jdbc-json-first-resultset-line-output
 */
@XStreamAlias("jdbc-json-first-resultset-line-output")
@ComponentProfile(summary = "Output the first resultset as JSON line-by-line", tag = "json,jdbc,line")
public class JdbcJsonOutputLines extends JdbcJsonOutput {

  private static final String ROOT_SEPARATOR = "\n";

  @Override
  public void translate(final JdbcResult source, final AdaptrisMessage target) throws ServiceException {
    try (Writer w = new BufferedWriter(target.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {

      generator.setPrettyPrinter(new MinimalPrettyPrinter(ROOT_SEPARATOR));

      for (final JdbcResultRow row : firstResultSet(source).getRows()) {
        Map<String, Object> jsonObject = new LinkedHashMap<>();
        for (final String field : row.getFieldNames()) {
          jsonObject.put(getColumnNameStyle().format(field), row.getFieldValue(field));
        }
        generator.writeObject(jsonObject);
      }
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
