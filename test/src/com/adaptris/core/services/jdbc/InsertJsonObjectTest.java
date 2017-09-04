package com.adaptris.core.services.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.json.jdbc.InsertJsonObject;
import com.adaptris.util.TimeInterval;

public class InsertJsonObjectTest extends JdbcJsonInsertCase {


  public InsertJsonObjectTest(String arg0) {
    super(arg0);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    InsertJsonObject service = createService();
    service.setTable("myTable");
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    return service;
  }


  public void testService() throws Exception {
    createDatabase();
    InsertJsonObject service = configure(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    doAssert(1);
  }

  public void testService_Array() throws Exception {
    createDatabase();
    InsertJsonObject service = configure(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
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
