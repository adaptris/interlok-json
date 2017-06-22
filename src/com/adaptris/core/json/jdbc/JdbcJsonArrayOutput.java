package com.adaptris.core.json.jdbc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultSet;
import com.fasterxml.jackson.core.JsonGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ResultSetTranslator} that iterates over all resultsets as a json array.
 * <p>
 * This differs from {@link JdbcJsonOutput} in that it will iterate over all possible result sets returned by the JDBC operation;
 * generally speaking for a select, it will only be 1, but for a Stored procedured it could be multiple; and outputs each result set
 * as an element of a array. So if you have multiple result sets then this would be the output:
 * </p>
 * <pre>
   {@code
[{
    "result": [{
        "firstName": "John",
        "lastName": "Doe"
    }, {
        "firstName": "Anna",
        "lastName": "Smith"
    }, {
        "firstName": "Peter",
        "lastName": "Jones"
    }]
}, {
    "result": [{
        "firstName": "Sherlock",
        "lastName": "Holmes"
    }, {
        "firstName": "John",
        "lastName": "Watson"
    }, {
        "firstName": "Charles",
        "lastName": "Darwin"
    }]
}]  
   }
   
 * </pre>
 * 
 * @config jdbc-json-array-output
 */
@XStreamAlias("jdbc-json-array-output")
public class JdbcJsonArrayOutput extends JdbcJsonOutput {

  public JdbcJsonArrayOutput() {
  }

  @Override
  public void translate(final JdbcResult source, final AdaptrisMessage target) throws SQLException, ServiceException {
    try (Writer w = new BufferedWriter(target.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      generator.writeStartArray();
      for (final JdbcResultSet result : source.getResultSets()) {
        writeResultSet(result, generator);
      }
      generator.writeEndArray();
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
