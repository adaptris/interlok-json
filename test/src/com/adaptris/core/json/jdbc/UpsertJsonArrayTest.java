package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class UpsertJsonArrayTest extends UpsertJsonCase {

  public UpsertJsonArrayTest(String arg0) {
    super(arg0);
  }

  @Override
  protected JdbcJsonInsert retrieveObjectForSampleConfig() {
    return configureForExamples(createService().withJsonId("id").withTable("myTable"));
  }


  @Override
  protected UpsertJsonArray createService() {
    return new UpsertJsonArray();
  }

  public void testService_InsertArray() throws Exception {
    createDatabase();
    JdbcJsonUpsert service = configureForTests(createService().withJsonId(ID_ELEMENT_VALUE));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

  public void testService_UpdateArray() throws Exception {
    createDatabase();
    populateDatabase();
    JdbcJsonUpsert service = configureForTests(createService().withJsonId(ID_ELEMENT_VALUE));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    execute(service, msg);
    doAssert(3);
    checkDob(CAROL, DOB);
  }

  public void testService_BrokenColumn() throws Exception {
    createDatabase();
    JdbcJsonUpsert service = configureForTests(createService().withJsonId(ID_ELEMENT_VALUE));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN_ARRAY);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  public void testService_NotJsonArray() throws Exception {
    createDatabase();
    UpsertJsonObject service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }
}
