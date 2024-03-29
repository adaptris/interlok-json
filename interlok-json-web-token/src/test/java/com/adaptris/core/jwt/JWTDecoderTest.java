package com.adaptris.core.jwt;

import static org.junit.jupiter.api.Assertions.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.Calendar;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.Base64EncodedSecret;
import com.adaptris.core.jwt.secrets.PGPSecret;

public class JWTDecoderTest extends JWTCommonTest {

  @Test
  public void testDecode() throws Exception {
    System.out.println(Calendar.getInstance().getTimeZone());
    JWTDecoder service = (JWTDecoder) retrieveObjectForSampleConfig();
    AdaptrisMessage message = message();

    service.doService(message);

    assertEquals(HEADER, new JSONObject(message.getMetadataValue("header")), false);
    assertEquals(CLAIMS, new JSONObject(message.getContent()), false);
  }

  @Test
  public void testInvalidKey() {
    try {
      JWTDecoder service = (JWTDecoder) retrieveObjectForSampleConfig();
      PGPSecret secret = getPGPSecret();
      secret.setPath(wrongKey);
      service.setSecret(secret);
      AdaptrisMessage message = message();

      service.doService(message);

      fail();
    } catch (ServiceException e) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JWTDecoder decoder = new JWTDecoder();
    decoder.setJwtString(new ConstantDataInputParameter(JWT));
    Base64EncodedSecret secret = new Base64EncodedSecret();
    secret.setSecret(KEY);
    decoder.setSecret(secret);
    decoder.setHeader(new MetadataDataOutputParameter("header"));
    decoder.setClaims(new StringPayloadDataOutputParameter());
    return decoder;
  }

}
