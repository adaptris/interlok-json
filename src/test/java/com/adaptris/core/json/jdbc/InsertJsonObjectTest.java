package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.NullPassThroughConverter;

public class InsertJsonObjectTest extends JdbcJsonInsertCase {


  public InsertJsonObjectTest(String arg0) {
    super(arg0);
  }

  public void testSetNullConverter() throws Exception {
    InsertJsonObject service = createService();
    assertNull(service.getNullConverter());
    service.setNullConverter(new NullPassThroughConverter());
    assertEquals(NullPassThroughConverter.class, service.getNullConverter().getClass());
    service.setNullConverter(null);
    assertNull(service.getNullConverter());
  }

  public void testService() throws Exception {
    createDatabase();
    InsertJsonObject service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("1", msg.getMetadataValue("rowsAffected"));
    doAssert(1);
  }

  public void testService_Array() throws Exception {
    createDatabase();
    InsertJsonObject service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected InsertJsonObject createService() {
    return new InsertJsonObject();
  }

}
