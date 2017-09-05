package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.jdbc.BatchInsertJsonArray;

public class BatchJsonArrayInsertTest extends JdbcJsonInsertCase {


  public BatchJsonArrayInsertTest(String arg0) {
    super(arg0);
  }

  public void testService() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

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

  public void testService_LowBatchWindow() throws Exception {
    createDatabase();
    BatchInsertJsonArray service = configureForTests(createService());
    service.setBatchWindow(1);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

  protected BatchInsertJsonArray createService() {
    return new BatchInsertJsonArray();
  }
}
