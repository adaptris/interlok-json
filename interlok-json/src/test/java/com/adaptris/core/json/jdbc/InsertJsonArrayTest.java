package com.adaptris.core.json.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;

public class InsertJsonArrayTest extends JdbcJsonInsertCase {

  @Test
  public void testService() throws Exception {
    createDatabase();
    InsertJsonArray service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("3", msg.getMetadataValue("rowsAffected"));
    doAssert(3);
  }

  @Test
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

  @Test
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
    return new InsertJsonArray().withJsonStyle(JsonStyle.JSON_ARRAY);
  }

}
