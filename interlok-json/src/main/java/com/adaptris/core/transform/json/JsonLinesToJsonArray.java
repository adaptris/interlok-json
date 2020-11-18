package com.adaptris.core.transform.json;

import static com.adaptris.interlok.util.CloseableIterable.ensureCloseable;
import java.io.Writer;
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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Transform from JSON lines to a JSON Array
 *
 * @config json-lines-to-json-array
 */
@XStreamAlias("json-lines-to-json-array")
@ComponentProfile(summary = "Transform from JSON lines to a JSON Array", tag = "json,transform",
    since = "3.11.1")
@NoArgsConstructor
public class JsonLinesToJsonArray extends ServiceImp {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ObjectMapper mapper = new ObjectMapper();
    JsonObjectProvider jsonProvider = JsonProvider.JsonStyle.JSON_LINES;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      try (CloseableIterable<AdaptrisMessage> jsonObjects = ensureCloseable(jsonProvider.createIterator(msg));
          Writer w = msg.getWriter();
          JsonGenerator generator =  mapper.getFactory().createGenerator(w).useDefaultPrettyPrinter()) {

        generator.writeStartArray();
        for (AdaptrisMessage m : jsonObjects) {
          generator.writeObject(JsonUtil.mapifyJson(m));
        }
        generator.writeEndArray();

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
