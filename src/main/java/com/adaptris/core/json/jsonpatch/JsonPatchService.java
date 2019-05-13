package com.adaptris.core.json.jsonpatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.PayloadStreamOutputParameter;
import com.adaptris.interlok.types.InterlokMessage.MessageWrapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract impl for patch services.
 * 
 */
public abstract class JsonPatchService extends ServiceImp {
  @AutoPopulated
  @Valid
  @InputFieldDefault(value = "PayloadStreamOutputParameter")
  private MessageWrapper<OutputStream> output;

  protected transient ObjectMapper mapper;

  @Override
  protected void initService() throws CoreException {
    mapper = new ObjectMapper();
  }

  @Override
  protected void closeService() {}


  protected JsonNode readAndClose(InputStream in) throws IOException {
    try (InputStream i = in) {
      return mapper.readTree(i);
    }
  }

  protected void writeAndClose(JsonNode patch, OutputStream out) throws IOException {
    try (OutputStream o = out; JsonGenerator generator = mapper.getFactory().createGenerator(o)) {
      generator.writeTree(patch);
    }
  }
  
  public MessageWrapper<OutputStream> getOutput() {
    return output;
  }

  /**
   * Specify where the results of the operation is going to be stored.
   * 
   * @param output the output; default is {@link PayloadStreamOutputParameter} if not specified.
   */
  public void setOutput(MessageWrapper<OutputStream> output) {
    this.output = output;
  }

  public <T extends JsonPatchService> T withOutput(MessageWrapper<OutputStream> o) {
    setOutput(o);
    return (T) this;
  }

  protected MessageWrapper<OutputStream> output() {
    return ObjectUtils.defaultIfNull(getOutput(), new PayloadStreamOutputParameter());
  }

}
