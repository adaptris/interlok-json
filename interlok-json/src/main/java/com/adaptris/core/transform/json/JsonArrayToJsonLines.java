package com.adaptris.core.transform.json;

import static com.adaptris.interlok.util.CloseableIterable.ensureCloseable;
import java.io.BufferedReader;
import java.io.PrintWriter;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.splitter.json.JsonProvider;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonObjectProvider;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.CloseableIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Transform from a JSON Array to JSON lines.
 *
 * @config json-array-to-lines-transform
 */
@XStreamAlias("json-array-to-lines-transform")
@ComponentProfile(summary = "Transform from a JSON Array to JSON lines", tag = "json,transform",
    since = "3.11.1")
@NoArgsConstructor
public class JsonArrayToJsonLines extends ServiceImp {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ObjectMapper mapper = new ObjectMapper();
    JsonObjectProvider jsonProvider = JsonProvider.JsonStyle.JSON_ARRAY;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      try (CloseableIterable<AdaptrisMessage> jsonObjects =  ensureCloseable(jsonProvider.createIterator(msg));
          PrintWriter pw = new PrintWriter(msg.getWriter())) {

        for (AdaptrisMessage m : jsonObjects) {
          try (BufferedReader buf = new BufferedReader(m.getReader())) {
            JsonParser parser = mapper.getFactory().createParser(buf);
            String line = mapper.writeValueAsString(mapper.readTree(parser));
            pw.println(line);
          }
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }


}
