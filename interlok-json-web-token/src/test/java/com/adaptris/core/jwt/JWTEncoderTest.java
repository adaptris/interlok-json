package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.Base64EncodedSecret;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JWTEncoderTest extends JWTCommonTest
{


  @Test
  public void testEncode() throws Exception
  {
    JWTEncoder service = (JWTEncoder)retrieveObjectForSampleConfig();
    AdaptrisMessage message = message();

    service.doService(message);

    String s = message.getContent();
    assertEquals(JWT, s);
  }

  @Test
  public void testBadSecret()
  {
    try
    {
      JWTEncoder service = (JWTEncoder)retrieveObjectForSampleConfig();
      service.setSecret(new Base64EncodedSecret());
      AdaptrisMessage message = message();

      service.doService(message);

      fail();
    }
    catch (ServiceException e)
    {
      /* expected */
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig()
  {
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
