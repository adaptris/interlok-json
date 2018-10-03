package com.adaptris.core.json;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.common.Execution;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("json-path-execution")
public class JsonPathExecution extends Execution
{
	@InputFieldDefault(value = "false")
	@AdvancedConfig
	private Boolean suppressPathNotFound;

	public JsonPathExecution()
	{
		super();
	}

	public JsonPathExecution(DataInputParameter<String> source, DataOutputParameter<String> target)
	{
		super(source, target);
	}

	/**
	 * @return true or false.
	 */
	public Boolean getSuppressPathNotFound()
	{
		return suppressPathNotFound;
	}

	/**
	 * Suppress exceptions caused by {@code PathNotFoundException}.
	 *
	 * @param b to suppress exceptions arising from a json path not being found; default is null (false).
	 */
	public void setSuppressPathNotFound(Boolean b)
	{
		this.suppressPathNotFound = b;
	}

	public boolean suppressPathNotFound()
  {
    return BooleanUtils.toBooleanDefaultIfNull(getSuppressPathNotFound(), false);
	}
}
