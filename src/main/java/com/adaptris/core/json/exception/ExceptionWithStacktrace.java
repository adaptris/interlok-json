package com.adaptris.core.json.exception;

import java.util.Map;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.exception.ExceptionReportService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Write the exception as a JSON object using {@code jackson-databind} when using a {@link ExceptionReportService}.
 * <p>
 * Note that this implementation doesn't write any of the exception causes, as it is possible that they are self-referential, which
 * will cause a problem when trying to serialize them using jackson; it just captures the stack-trace in addition to information
 * captured by {@link ExceptionAsJson}.
 * </p>
 * 
 * @config exception-as-json-with-stacktrace
 */
@XStreamAlias("exception-as-json-with-stacktrace")
@ComponentProfile(summary = "Serialize an exception as JSON, along with the stack trace, when using ExceptionReportService",
    tag = "json")
public class ExceptionWithStacktrace extends ExceptionAsJson {

  public ExceptionWithStacktrace() {
  }

  @Override
  protected Map<String, Object> createReport(Exception exc, AdaptrisMessage msg) {
    Map<String, Object> result = super.createReport(exc, msg);
    result.put("stacktrace", exc.getStackTrace());
    return result;
  }

}
