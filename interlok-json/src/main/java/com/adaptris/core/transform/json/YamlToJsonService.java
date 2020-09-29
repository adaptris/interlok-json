package com.adaptris.core.transform.json;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This service allows you to transform YAML to JSON.
 * 
 * @config yaml-to-json
 */
@XStreamAlias("yaml-to-json")
@AdapterComponent
@ComponentProfile(summary = "Transform a YAML document into JSON", tag = "service,transform,json,yaml")
public class YamlToJsonService extends ServiceImp {

	@NotNull
	@Valid
	@AutoPopulated
  private DataInputParameter<String> source;

	@NotNull
	@Valid
	@AutoPopulated
  private DataOutputParameter<String> target;

  private transient ObjectMapper yamlReader;
  private transient ObjectMapper jsonWriter;

  public YamlToJsonService() {
    setSource(new StringPayloadDataInputParameter());
    setTarget(new StringPayloadDataOutputParameter());
  }

	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException {
		try {
      String yaml = source.extract(message);
      target.insert(jsonWriter.writeValueAsString(yamlReader.readValue(yaml, Object.class)), message);
		} catch (final Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
		}
	}


	@Override
	public void prepare() throws CoreException {
	}

	@Override
	protected void closeService() {
	}

	@Override
	protected void initService() throws CoreException {
    yamlReader = new ObjectMapper(new YAMLFactory());
    jsonWriter = new ObjectMapper();
	}

	/**
	 * Get the target JSON.
	 *
	 * @return The target JSON.
	 */
	public DataOutputParameter<String> getTarget() {
		return target;
	}

	/**
	 * Set the target JSON.
	 *
	 * @param targetJson
	 *          The target JSON.
	 */
	public void setTarget(final DataOutputParameter<String> targetJson) {
		this.target = Args.notNull(targetJson, "Target");
	}

	/**
   * Get the source.
   *
   * @return The source.
   */
	public DataInputParameter<String> getSource() {
		return source;
	}

	/**
	 * Set the source JSON.
	 *
	 * @param sourceJson
	 *          The source JSON.
	 */
	public void setSource(final DataInputParameter<String> sourceJson) {
		this.source = Args.notNull(sourceJson, "Source");
	}
}
