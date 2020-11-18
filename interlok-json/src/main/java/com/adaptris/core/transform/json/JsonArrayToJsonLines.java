package com.adaptris.core.transform.json;

import static com.adaptris.interlok.util.CloseableIterable.ensureCloseable;
import java.io.PrintWriter;
import java.util.Map;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.splitter.json.JsonProvider;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonObjectProvider;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.CloseableIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Transform from a JSON Array to JSON lines.
 *
 * @config json-array-to-json-lines
 */
@XStreamAlias("json-array-to-json-lines")
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
          Map<String, String> json = JsonUtil.mapifyJson(m);
          String line = mapper.writeValueAsString(json);
          pw.println(line);
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
