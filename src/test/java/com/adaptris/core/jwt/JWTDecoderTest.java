package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class JWTDecoderTest extends JWTCommonTest
{

  @Test
  public void testDecode() throws Exception
  {
    JWTDecoder service = (JWTDecoder)retrieveObjectForSampleConfig();
    AdaptrisMessage message = message();

    service.doService(message);

    assertEquals(HEADER, new JSONObject(message.getMetadataValue("header")), false);
    assertEquals(CLAIMS, new JSONObject(message.getContent()), false);
  }

  @Test
  public void testInvalidKey()
  {
    try
    {
      JWTDecoder service = (JWTDecoder)retrieveObjectForSampleConfig();
      service.setSecret(new ConstantDataInputParameter("invalid key"));
      AdaptrisMessage message = message();

      service.doService(message);

      fail();
    }
    catch (ServiceException e)
    {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig()
  {
    JWTDecoder decoder = new JWTDecoder();
    decoder.setJwtString(new ConstantDataInputParameter(JWT));
    decoder.setSecret(new ConstantDataInputParameter(KEY));
    decoder.setHeader(new MetadataDataOutputParameter("header"));
    decoder.setClaims(new StringPayloadDataOutputParameter());
    return decoder;
  }
}
