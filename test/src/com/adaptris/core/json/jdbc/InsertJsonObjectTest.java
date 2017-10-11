package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.jdbc.InsertJsonObject;

public class InsertJsonObjectTest extends JdbcJsonInsertCase {


  public InsertJsonObjectTest(String arg0) {
    super(arg0);
  }

  public void testService() throws Exception {
    createDatabase();
    InsertJsonObject service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
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

  protected InsertJsonObject createService() {
    return new InsertJsonObject();
  }

}
