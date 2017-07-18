package com.adaptris.core.json.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.services.jdbc.ResultSetTranslator;

public abstract class JsonResultSetTranslatorImpl implements ResultSetTranslator {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
  }

}
