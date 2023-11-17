package com.adaptris.core.json.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapUpsert;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;

public class UpsertJsonArrayTest extends UpsertJsonCase {


  @Override
  protected UpsertJsonArray retrieveObjectForSampleConfig() {
    return (UpsertJsonArray) configureForExamples(createService().withId("id").withTable("myTable"));
  }


  @Override
  protected UpsertJsonArray createService() {
    return new UpsertJsonArray().withJsonStyle(JsonStyle.JSON_ARRAY);
  }

  @Test
  public void testService_InsertArray() throws Exception {
    createDatabase();
    UpsertJsonArray service =
        configureForTests(createService()).withId(ID_ELEMENT_VALUE).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("3", msg.getMetadataValue("rowsAffected"));
    doAssert(3);
  }

  @Test
  public void testService_UpdateArray() throws Exception {
    createDatabase();
    populateDatabase();
    UpsertJsonArray service = configureForTests(createService()).withId(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
    checkDob(CAROL, DOB);
  }

  @Test
  public void testService_BrokenColumn() throws Exception {
    createDatabase();
    JdbcMapUpsert service = (JdbcMapUpsert) configureForTests(createService().withId(ID_ELEMENT_VALUE));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN_ARRAY);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_NotJsonArray() throws Exception {
    createDatabase();
    UpsertJsonArray service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }
}
