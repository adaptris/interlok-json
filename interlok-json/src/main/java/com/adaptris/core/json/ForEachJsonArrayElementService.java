package com.adaptris.core.json;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@XStreamAlias("for-each-json-array-element-service")
@ComponentProfile(
    summary = "Executes a service for each Json array element.")
public class ForEachJsonArrayElementService extends ServiceImp {

  @Getter
  @Setter
  private DataInputParameter<String> jsonArraySource;
  
  @Getter
  @Setter
  private DataOutputParameter<String> perElementDestination;

  @Getter
  @Setter
  private Service forEachElementService;
  
  @InputFieldDefault(value="Permissive")
  @AdvancedConfig(rare=true)
  @Getter
  @Setter
  private JsonParseModeEnum jsonParseMode;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    final JSONParser jsonParser = new JSONParser(getDefaultJsonParseMode().parserMode());
    Object object;
    try {
      object = jsonParser.parse(jsonArraySource.extract(msg));

      if (object instanceof JSONObject) {
        throw new ServiceException("Object is not a Json Array.");
      } else if (object instanceof JSONArray) {
        final JSONArray array = (JSONArray) object;

        for (final Object element : array) {
          perElementDestination.insert(((JSONObject) element).toJSONString(), msg);
          forEachElementService.doService(msg);
        }
      }
    } catch (Exception ex) {
      ExceptionHelper.rethrowServiceException(ex);
    }
  }

  private JsonParseModeEnum getDefaultJsonParseMode() {
    return getJsonParseMode() == null ? JsonParseModeEnum.Permissive : getJsonParseMode();
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
