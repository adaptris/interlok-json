package com.adaptris.core.json.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class BatchJsonArrayInsertTest extends JdbcJsonInsertCase {

  @Test
  public void testAccumulate() throws Exception {
    int[] rc = {1, 2, Statement.EXECUTE_FAILED};
    try {
      BatchInsertJsonArray.accumulate(rc);
    } catch (SQLException expected) {

    }
    int[] rc2 = {1, 2, Statement.SUCCESS_NO_INFO};
    assertEquals(3, BatchInsertJsonArray.accumulate(rc2));
  }

  @Test
  public void testService() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("3", msg.getMetadataValue("rowsAffected"));
    doAssert(3);
  }

  @Test
  public void testService_NotArray() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_InvalidColumns() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN_ARRAY);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_LowBatchWindow() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService());
    service.setBatchWindow(1);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

  @Override
  protected BatchInsertJsonArray createService() {
    return new BatchInsertJsonArray();
  }
}
