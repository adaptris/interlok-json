package com.adaptris.core.json;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.DeserializerCase.TypeKey;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class VerifyIsJsonTest extends ExampleServiceCase {

  private Map<TypeKey, AdaptrisMessage> messageTypes = DeserializerCase.createMessageFlavours();

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = messageTypes.get(TypeKey.Object);
    VerifyIsJson service = new VerifyIsJson();
    execute(service, msg);
  }

  @Test
  public void testService_NotJson() throws Exception {
    AdaptrisMessage msg = messageTypes.get(TypeKey.Invalid_Array);
    VerifyIsJson service = new VerifyIsJson();
    assertThrows(ServiceException.class, ()->{
      execute(service, msg);
    }, "Failed, not Json");
  }

  @Test
  public void testService_Basic() throws Exception {
    AdaptrisMessage m1 = messageTypes.get(TypeKey.Object);
    AdaptrisMessage m2 = messageTypes.get(TypeKey.Invalid_Array);
    VerifyIsJson service = new VerifyIsJson().withDeserializer(new BasicJsonDeserializer());
    execute(service, m1);
    execute(service, m2);
  }

  @Override
  protected VerifyIsJson retrieveObjectForSampleConfig() {
    return new VerifyIsJson();
  }
}
