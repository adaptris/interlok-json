package com.adaptris.core.transform.json;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convert all metadata values to a JSON object in the message payload.
 *
 * @author driesg
 */
@XStreamAlias("metadata-to-json-service")
@ComponentProfile(summary="Convert metadata values to a JSON object that will be set as the payload of the message")
public class MetadataToJsonService extends ServiceImp {

  private static final MetadataFilter DEFAULT_FILTER = new NoOpMetadataFilter();

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean convertNumeric;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean addTrailingNewline;

  @AdvancedConfig
  @InputFieldDefault(value = "no-op-metadata-filter")
  private MetadataFilter metadataFilter;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Map<String, Object> result = mapify(metadataFilter().filter(msg));
      write(result, msg);
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

  private Map<String, Object> mapify(MetadataCollection collection) {
    Map<String, Object> result = new HashMap<>();
    for (MetadataElement ele : collection) {
      String value = ele.getValue();
      if (convertNumeric() && NumberUtils.isCreatable(value)) {
        result.put(ele.getKey(), Double.valueOf(value));
      } else {
        result.put(ele.getKey(), value);
      }
    }
    return result;
  }

  private void write(Map<String, Object> json, AdaptrisMessage msg) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(json);
    try (PrintWriter pw = new PrintWriter(msg.getWriter(StandardCharsets.UTF_8.name()))) {
      if (addTrailingNewline()) {
        pw.println(jsonString);
      } else {
        pw.print(jsonString);
      }
    }
  }

  /**
   * Make an attempt to convert numerics into their number form rather than leaving as strings.
   */
  public void setConvertNumeric(Boolean convertNumeric) {
    this.convertNumeric = convertNumeric;
  }

  public Boolean getConvertNumeric() {
    return convertNumeric;
  }

  private boolean convertNumeric() {
    return BooleanUtils.toBooleanDefaultIfNull(getConvertNumeric(), false);
  }

  public MetadataToJsonService withConvertNumeric(boolean convertNumerics) {
    setConvertNumeric(convertNumerics);
    return this;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Set the metadata filter to be used when generating keys for the JSON payload.
   * 
   * @param metadataFilter the filter, default is {@link NoOpMetadataFilter} if not specified.
   */
  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

  public MetadataToJsonService withMetadataFilter(MetadataFilter metadataFilter) {
    setMetadataFilter(metadataFilter);
    return this;
  }

  private MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), DEFAULT_FILTER);
  }

  /**
   * Add a trailing new line to the payload.
   * <p>
   * The JSON object is emitted without beautification; this allows you to add a new line to the end
   * of the JSON object if this is required. The newline will be platform dependent c.f.
   * {@code System.lineSeparator()}.
   * </p>
   * 
   * @param b true to add a trailing new line, default is {@code false} if not otherwise specified.
   */
  public void setAddTrailingNewline(Boolean b) {
    this.addTrailingNewline = b;
  }

  public Boolean getAddTrailingNewline() {
    return addTrailingNewline;
  }

  public MetadataToJsonService withAddTrailingNewline(boolean b) {
    setAddTrailingNewline(b);
    return this;
  }

  private boolean addTrailingNewline() {
    return BooleanUtils.toBooleanDefaultIfNull(getAddTrailingNewline(), false);
  }
}