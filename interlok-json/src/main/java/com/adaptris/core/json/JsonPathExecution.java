package com.adaptris.core.json;

import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.common.Execution;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.util.text.NullConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("json-path-execution")
@ComponentProfile(summary = "Extract a JSON Path from the source and write it into the target", tag = "json")
public class JsonPathExecution extends Execution {

  private static NullConverter NULL_TO_NULL_CONSTANT_STRING = new NullConverter() {
    @Override
    public <T> T convert(T t) {
      return (T) ObjectUtils.defaultIfNull(t, "null");
    }
  };

  @InputFieldDefault(value = "false")
  @AdvancedConfig
  private Boolean suppressPathNotFound;

  @Valid
  @AdvancedConfig
  private NullConverter nullConverter;

  public JsonPathExecution() {
    super();
  }

  public JsonPathExecution(DataInputParameter<String> source, DataOutputParameter<String> target) {
    super(source, target);
  }

  /**
   * @return true or false.
   */
  public Boolean getSuppressPathNotFound() {
    return suppressPathNotFound;
  }

  /**
   * Suppress exceptions caused by {@code PathNotFoundException}.
   *
   * @param b to suppress exceptions arising from a json path not being found; default is null (false).
   */
  public void setSuppressPathNotFound(Boolean b) {
    this.suppressPathNotFound = b;
  }

  public boolean suppressPathNotFound() {
    return BooleanUtils.toBooleanDefaultIfNull(getSuppressPathNotFound(), false);
  }

  /**
   * @return the nullConverter
   */
  public NullConverter getNullConverter() {
    return nullConverter;
  }

  /**
   * Specify the behaviour when a null is encountered during json path execution.
   * 
   * @param nc the NullConverter to set, the default is return "null" as the value.
   */
  public void setNullConverter(NullConverter nc) {
    this.nullConverter = nc;
  }

  public NullConverter nullConverter() {
    return ObjectUtils.defaultIfNull(getNullConverter(), NULL_TO_NULL_CONSTANT_STRING);
  }

  public <T extends JsonPathExecution> T withNullConverter(NullConverter nc) {
    setNullConverter(nc);
    return (T) this;
  }

  public <T extends JsonPathExecution> T withSuppressPathNotFound(Boolean b) {
    setSuppressPathNotFound(b);
    return (T) this;
  }

}
