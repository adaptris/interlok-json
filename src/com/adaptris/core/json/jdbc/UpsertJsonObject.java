package com.adaptris.core.json.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Convenience service for inserting/updating a JSON object into a database.
 * 
 * <p>
 * Creates an insert or update statement based on the contents of the JSON object inside the array.
 * {@code { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03", "id": "1234"}}
 * will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob,id) VALUES (?,?,?,?)} or
 * {@code UPDATE table SET firstname=?, lastname=?, dob=? WHERE id = ?;} if {@code 1234} already exists as a row.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be
 * fail. Additionally, nested JSON objects will be rendered as strings before being passed into the appropriate statement.
 * </p>
 * 
 * @config json-jdbc-upsert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert/Update a JSON object into a database", tag = "service,json,jdbc")
@XStreamAlias("json-jdbc-upsert")
public class UpsertJsonObject extends JdbcJsonUpsert {

  public UpsertJsonObject() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      handleUpsert(conn, JsonUtil.mapifyJson(msg));
      commit(conn, msg);
    } catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }

  protected void handleUpsert(Connection conn, Map<String, String> json) throws ServiceException {
    PreparedStatement selectStmt = null, insertStmt = null, updateStmt = null;
    ResultSet rs = null;
    try {
      InsertWrapper inserter = new InsertWrapper(json);
      SelectWrapper selector = new SelectWrapper(json);
      UpdateWrapper updater = new UpdateWrapper(json);
      log.trace("SELECT [{}]", selector.statement);
      log.trace("INSERT [{}]", inserter.statement);
      log.trace("UPDATE [{}]", updater.statement);
      selectStmt = selector.addParams(prepareStatement(conn, selector.statement), json);
      rs = selectStmt.executeQuery();
      if (rs.next()) {
        updateStmt = updater.addParams(prepareStatement(conn, updater.statement), json);
        updateStmt.executeUpdate();
      } else {
        insertStmt = inserter.addParams(prepareStatement(conn, inserter.statement), json);
        insertStmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(selectStmt);
      JdbcUtil.closeQuietly(insertStmt);
      JdbcUtil.closeQuietly(updateStmt);
    }
  }
}
