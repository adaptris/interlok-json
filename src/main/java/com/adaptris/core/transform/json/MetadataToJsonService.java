package com.adaptris.core.transform.json;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.*;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Convert all metadata values to a JSON object in the message payload.
 *
 * @author driesg
 */
@XStreamAlias("metadata-to-json-service")
@ComponentProfile(summary="Convert metadata values to a JSON object that will be set as the payload of the message")
public class MetadataToJsonService extends ServiceImp {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean convertNumeric;

  @AdvancedConfig
  private MetadataFilter metadataFilter = new NoOpMetadataFilter();

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    JSONObject result = new JSONObject();
    for(MetadataElement ele: getMetadataFilter().filter(msg)) {
      String value = ele.getValue();
      if (convertNumeric() && NumberUtils.isNumber(value)){
        result.put(ele.getKey(), Double.valueOf(value));
      } else {
        result.put(ele.getKey(), value);
      }
    }
    msg.setContent(result.toString(), "utf-8");
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

  public void setConvertNumeric(Boolean convertNumeric) {
    this.convertNumeric = convertNumeric;
  }

  public Boolean getConvertNumeric() {
    return convertNumeric;
  }

  private Boolean convertNumeric() {
    return getConvertNumeric() != null ? getConvertNumeric() : false;
  }

  public MetadataToJsonService withConvertNumeric(boolean convertNumerics) {
    setConvertNumeric(convertNumerics);
    return this;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

  public MetadataToJsonService withMetadataFilter(MetadataFilter metadataFilter) {
    setMetadataFilter(metadataFilter);
    return this;
  }
}