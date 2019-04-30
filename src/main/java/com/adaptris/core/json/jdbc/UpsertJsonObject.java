package com.adaptris.core.json.jdbc;

import java.sql.Connection;

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.jdbc.JdbcMapUpsert;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.text.NullConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting/updating a JSON object into a database.
 * 
 * <p>
 * Creates an insert or update statement based on the contents of the JSON object.
 * {@code { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03", "id": "1234"}}
 * will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob,id) VALUES (?,?,?,?)} or
 * {@code UPDATE table SET firstname=?, lastname=?, dob=? WHERE id = ?;} if {@code 1234} already exists as a row.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be
 * fail. Additionally, nested JSON objects will be rendered as strings before being passed into the appropriate statement; so
 * {@code { "firstname":"alice", "lastname":"smith", "address": { "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}}} would
 * still be 3 parameters, the address parameter will be {@code '{ "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}'}
 * </p>
 * 
 * @config json-jdbc-upsert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert/Update a JSON object into a database", tag = "service,json,jdbc", since = "3.6.5")
@XStreamAlias("json-jdbc-upsert")
@DisplayOrder(order = {"table", "idField"})
public class UpsertJsonObject extends JdbcMapUpsert {

  @Valid
  @AdvancedConfig
  private NullConverter nullConverter;

  public UpsertJsonObject() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      handleUpsert(table(msg), conn, JsonUtil.mapifyJson(msg, getNullConverter()));
      JdbcUtil.commit(conn, msg);
    } catch (Exception e) {
      JdbcUtil.rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
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
