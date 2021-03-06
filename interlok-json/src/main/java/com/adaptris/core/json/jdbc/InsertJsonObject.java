package com.adaptris.core.json.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.jdbc.JdbcMapInsert;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.text.NullConverter;
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
 * fail. Additionally, nested JSON objects will be rendered as strings before being passed into the appropriate statement; so
 * {@code { "firstname":"alice", "lastname":"smith", "address": { "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}}} would
 * still be 3 parameters, the address parameter will be {@code '{ "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}'}
 * </p>
 * 
 * @config json-jdbc-insert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert a JSON object into a database", tag = "service,json,jdbc", since = "3.6.5")
@XStreamAlias("json-jdbc-insert")
@DisplayOrder(order = {"table"})
public class InsertJsonObject extends JdbcMapInsert {

  @AdvancedConfig
  @Valid
  private NullConverter nullConverter;

  public InsertJsonObject() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      addUpdatedMetadata(handleInsert(table(msg), conn, JsonUtil.mapifyJson(msg, getNullConverter())), msg);
      JdbcUtil.commit(conn, msg);
    } catch (Exception e) {
      JdbcUtil.rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
      JdbcUtil.closeQuietly(stmt);
    }
  }

  /**
   * @return the nullConverter
   */
  public NullConverter getNullConverter() {
    return nullConverter;
  }

  /**
   * Specify the behaviour when a {@code NullNode} is encountered.
   * 
   * @param nc the NullConverter to set, the default is effectively the string {@code "null"} as returned by
   *        {@code NullNode#asText()}
   */
  public void setNullConverter(NullConverter nc) {
    this.nullConverter = nc;
  }

}
