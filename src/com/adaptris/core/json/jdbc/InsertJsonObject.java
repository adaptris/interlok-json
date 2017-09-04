package com.adaptris.core.json.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting a JSON object into a database.
 * 
 * <p>
 * Creates an insert statement based on the contents of the JSON object inside the array.
 * {@code { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }}
 * will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob) VALUES (?,?,?)}.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be
 * fail.
 * </p>
 * 
 * @config json-jdbc-insert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert a JSON object into a database", tag = "service,json,jdbc")
@XStreamAlias("json-jdbc-insert")
public class InsertJsonObject extends JdbcJsonInsert {

  public InsertJsonObject() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      Map<String, String> json = JsonUtil.mapifyJson(msg);
      StatementWrapper wrapper = new StatementWrapper(json);
      log.trace("Generated [{}]", wrapper.statement);
      stmt = prepareStatement(conn, wrapper.statement);
      wrapper.addParams(stmt, json);
      stmt.executeUpdate();
      commit(conn, msg);
    } catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
      JdbcUtil.closeQuietly(stmt);
    }
  }
}
