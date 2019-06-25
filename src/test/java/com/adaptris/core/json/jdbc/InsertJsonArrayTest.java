package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class InsertJsonArrayTest extends JdbcJsonInsertCase {


  public InsertJsonArrayTest(String arg0) {
    super(arg0);
  }

  public void testService() throws Exception {
    createDatabase();
    InsertJsonArray service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("3", msg.getMetadataValue("rowsAffected"));
    doAssert(3);
  }

  public void testService_NotArray() throws Exception {
    createDatabase();
    InsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  public void testService_BrokenColumn() throws Exception {
    createDatabase();
    InsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN_ARRAY);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected InsertJsonArray createService() {
    return new InsertJsonArray();
  }

}
