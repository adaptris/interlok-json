package com.adaptris.core.json.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.NullPassThroughConverter;

public class UpsertJsonObjectTest extends UpsertJsonCase {


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected UpsertJsonObject retrieveObjectForSampleConfig() {
    return (UpsertJsonObject) configureForExamples(createService().withId("id").withTable("myTable"));
  }


  @Override
  protected UpsertJsonObject createService() {
    return new UpsertJsonObject();
  }

  @Test
  public void testSetNullConverter() throws Exception {
    UpsertJsonObject service = createService();
    assertNull(service.getNullConverter());
    service.setNullConverter(new NullPassThroughConverter());
    assertEquals(NullPassThroughConverter.class, service.getNullConverter().getClass());
    service.setNullConverter(null);
    assertNull(service.getNullConverter());
  }


  @Test
  public void testService_Insert() throws Exception {
    createDatabase();
    UpsertJsonObject service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_Update() throws Exception {
    createDatabase();
    populateDatabase();
    UpsertJsonObject service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(OBJECT_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("1", msg.getMetadataValue("rowsAffected"));
    doAssert(1);
    checkDob(CAROL, DOB);
  }



  @Test
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
