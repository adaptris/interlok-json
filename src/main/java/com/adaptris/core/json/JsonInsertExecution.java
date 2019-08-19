package com.adaptris.core.json;

import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@XStreamAlias("json-insert-execution")
public class JsonInsertExecution
{
	@NotNull
	@Valid
	private DataInputParameter<String> jsonPath;

	@NotNull
	@Valid
	private DataInputParameter<String> newValue;

	public JsonInsertExecution()
	{
		/* default constructor */
	}

	public JsonInsertExecution(DataInputParameter<String> jsonPath, DataInputParameter<String> newValue)
	{
		this();
		setJsonPath(jsonPath);
		setNewValue(newValue);
	}

	public DataInputParameter<String> getJsonPath()
	{
		return jsonPath;
	}

	public void setJsonPath(DataInputParameter<String> jsonPath)
	{
		this.jsonPath = Args.notNull(jsonPath, "JSON Path");
	}

	public DataInputParameter<String> getNewValue()
	{
		return newValue;
	}

	public void setNewValue(DataInputParameter<String> newValue)
	{
		this.newValue = Args.notNull(newValue, "New Value");
	}
}
