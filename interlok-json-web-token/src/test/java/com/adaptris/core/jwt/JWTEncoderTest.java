package com.adaptris.core.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.Base64EncodedSecret;

public class JWTEncoderTest extends JWTCommonTest {

  @Test
  public void testEncode() throws Exception {
    JWTEncoder service = (JWTEncoder) retrieveObjectForSampleConfig();
    AdaptrisMessage message = message();

    service.doService(message);

    String s = message.getContent();
    assertEquals(JWT, s);
  }

  @Test
  public void testBadSecret() {
    JWTEncoder service = (JWTEncoder) retrieveObjectForSampleConfig();
    service.setSecret(new Base64EncodedSecret());
    AdaptrisMessage message = message();

    assertThrows(ServiceException.class, () -> service.doService(message));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JWTEncoder encoder = new JWTEncoder();
    Base64EncodedSecret secret = new Base64EncodedSecret();
    secret.setSecret(KEY);
    encoder.setSecret(secret);
    encoder.setHeader(new ConstantDataInputParameter(HEADER.toString()));
    encoder.setClaims(new ConstantDataInputParameter(CLAIMS.toString()));
    encoder.setJwtOutput(new StringPayloadDataOutputParameter());
    return encoder;
  }

}
