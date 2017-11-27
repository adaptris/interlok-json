package com.adaptris.core.json.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.NullPassThroughConverter;

public class UpsertJsonObjectTest extends UpsertJsonCase {

  public UpsertJsonObjectTest(String arg0) {
    super(arg0);
  }

  @Override
  protected UpsertJsonObject retrieveObjectForSampleConfig() {
    return (UpsertJsonObject) configureForExamples(createService().withId("id").withTable("myTable"));
  }


  @Override
  protected UpsertJsonObject createService() {
    return new UpsertJsonObject();
  }

  public void testSetNullConverter() throws Exception {
    UpsertJsonObject service = createService();
    assertNull(service.getNullConverter());
    service.setNullConverter(new NullPassThroughConverter());
    assertEquals(NullPassThroughConverter.class, service.getNullConverter().getClass());
    service.setNullConverter(null);
    assertNull(service.getNullConverter());
  }


  public void testService_Insert() throws Exception {
    createDatabase();
    UpsertJsonObject service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    doAssert(1);
  }

  public void testService_Update() throws Exception {
    createDatabase();
    populateDatabase();
    UpsertJsonObject service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    doAssert(1);
    checkDob(CAROL, DOB);
  }



  public void testService_Array() throws Exception {
    createDatabase();
    UpsertJsonObject service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }
}
