package com.adaptris.core.json.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ArrayUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.splitter.json.LargeJsonArraySplitter;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting a JSON array into a database.
 * 
 * <p>
 * This creates insert statements based on the contents of each JSON object inside the array. The actual insert statement will only
 * be generated once based on the first JSON object in the array and executed the appropriate number of times.
 * </p>
 * <pre>
 * {@code 
 * [
 *   { "firstname":"alice", "lastname":"smith", "dob":"2017-01-01" },
 *   { "firstname":"bob", "lastname":"smith", "dob":"2017-01-02" },
 *   { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }
 * ]}
 * </pre>
 * will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob) VALUES (?,?,?)} 3 times;
 * batching as required using {@link PreparedStatement#addBatch()} / {@link PreparedStatement#executeBatch()}.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be
 * fail. Additionally, nested JSON objects will be rendered as strings before being passed into the appropriate statement; so
 * {@code { "firstname":"alice", "lastname":"smith", "address": { "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}}} would
 * still be 3 parameters, the address parameter will be {@code '{ "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}'}
 * </p>
 * 
 * @config json-array-jdbc-batch-insert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert a JSON array into a database", tag = "service,json,jdbc", since = "3.6.5")
@XStreamAlias("json-array-jdbc-batch-insert")
@DisplayOrder(order = {"table", "batchWindow"})
public class BatchInsertJsonArray extends InsertJsonObject {

  private static final InheritableThreadLocal<AtomicInteger> counter = new InheritableThreadLocal<AtomicInteger>() {
    @Override
    protected synchronized AtomicInteger initialValue() {
      return new AtomicInteger();
    }
  };

  public static final int DEFAULT_BATCH_WINDOW = 1024;


  @AdvancedConfig
  @InputFieldDefault(value = "1024")
  private Integer batchWindow = null;

  public BatchInsertJsonArray() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      // Use the already existing LargeJsonArraySplitter, but force it with a default-mf
      LargeJsonArraySplitter splitter =
          new LargeJsonArraySplitter().withMessageFactory(AdaptrisMessageFactory.getDefaultInstance());
      InsertWrapper wrapper = null;
      for (AdaptrisMessage m : splitter.splitMessage(msg)) {
        Map<String, String> json = JsonUtil.mapifyJson(m, getNullConverter());
        if (wrapper == null) {
          wrapper = new InsertWrapper(json);
          log.trace("Generated [{}]", wrapper.statement());
          stmt = prepareStatement(conn, wrapper.statement());
        }
        wrapper.addParams(stmt, json);
        execute(stmt);
      }
      finish(stmt);
      commit(conn, msg);
    } catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
      JdbcUtil.closeQuietly(stmt);
    }
  }


  private void execute(PreparedStatement insert) throws SQLException {
    int count = counter.get().incrementAndGet();
    insert.addBatch();
    if (count % batchWindow() == 0) {
      log.trace("BatchWindow reached, executeBatch()");
      executeBatch(insert);
    }
  }

  private void finish(PreparedStatement insert) throws SQLException {
    executeBatch(insert);
    counter.set(new AtomicInteger());
  }

  private void executeBatch(PreparedStatement insert) throws SQLException {
    int[] rc = insert.executeBatch();
    List<Integer> result = Arrays.asList(ArrayUtils.toObject(rc));
    if (result.contains(Statement.EXECUTE_FAILED)) {
      throw new SQLException("Batch Execution Failed.");
    }
  }

  /**
   * @return the batchWindow
   */
  public Integer getBatchWindow() {
    return batchWindow;
  }

  /**
   * Set the batch window for operations.
   * 
   * @param i the batchWindow to set; default is {@value #DEFAULT_BATCH_WINDOW} if not specified.
   */
  public void setBatchWindow(Integer i) {
    this.batchWindow = i;
  }

  int batchWindow() {
    return getBatchWindow() != null ? getBatchWindow().intValue() : DEFAULT_BATCH_WINDOW;
  }

}
