package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.services.path.json.JsonPathService;
import com.adaptris.core.services.splitter.MessageSplitter;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("json-path-splitter")
public class JsonPathSplitter extends MessageSplitterImp {
  
  private DataInputParameter<String> jsonSource;
  
  private DataInputParameter<String> jsonPath;
  
  private MessageSplitter messageSplitter;

  @Override
  public Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {      
      JsonPathService jsonPathService = new JsonPathService();
      ConstantDataInputParameter constantDataInputParameter = new ConstantDataInputParameter(this.getJsonPath().extract(msg));
      StringPayloadDataOutputParameter payloadOutputParam = new StringPayloadDataOutputParameter();
      Execution singleExec = new Execution(constantDataInputParameter, payloadOutputParam);
      
      jsonPathService.setSource(this.getJsonSource());
      jsonPathService.getExecutions().add(singleExec);
      
      jsonPathService.doService(msg);
      
      return this.getMessageSplitter().splitMessage(msg);
    } catch (InterlokException ex) {
      throw new CoreException(ex);
    }
  }

  public DataInputParameter<String> getJsonSource() {
    return jsonSource;
  }

  public void setJsonSource(DataInputParameter<String> jsonSource) {
    this.jsonSource = jsonSource;
  }

  public DataInputParameter<String> getJsonPath() {
    return jsonPath;
  }

  public void setJsonPath(DataInputParameter<String> jsonPath) {
    this.jsonPath = jsonPath;
  }

  public MessageSplitter getMessageSplitter() {
    return messageSplitter;
  }

  public void setMessageSplitter(MessageSplitter messageSplitter) {
    this.messageSplitter = messageSplitter;
  }

}
