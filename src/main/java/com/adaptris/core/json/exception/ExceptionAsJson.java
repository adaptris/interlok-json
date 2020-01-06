package com.adaptris.core.json.exception;

import static org.apache.commons.lang.StringUtils.isEmpty;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.services.exception.ExceptionReportService;
import com.adaptris.core.services.exception.ExceptionSerializer;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Write the exception as a JSON object using {@code jackson-databind} when using a {@link ExceptionReportService}.
 * <p>
 * Writes the minimal amount of information possible which is the {@link Exception#getMessage()}; the workflow from where the
 * message was being processed (if available); and the location of the error (i.e. the service that threw the exception).
 * </p>
 * 
 * @config exception-as-json
 */
@XStreamAlias("exception-as-json")
@ComponentProfile(summary = "Serialize an exception as JSON when using ExceptionReportService", tag = "json")
public class ExceptionAsJson implements ExceptionSerializer {

  private transient ObjectMapper mapper = new ObjectMapper();

  public ExceptionAsJson() {
  }

  @Override
  public void serialize(Exception exception, AdaptrisMessage msg) throws CoreException {
    try (Writer out = msg.getWriter(encoding(msg))) {
      mapper.writerWithDefaultPrettyPrinter().writeValue(out, createReport(exception, msg));
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private String encoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }

  protected Map<String, Object> createReport(Exception exc, AdaptrisMessage msg) {
    Map<String, Object> result = new TreeMap<String, Object>();
    result.put("workflow", msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY));
    result.put("exceptionLocation", msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
    result.put("exceptionMessage", exc.getMessage());
    return result;
  }
}
