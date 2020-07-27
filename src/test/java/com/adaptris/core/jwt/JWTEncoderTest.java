package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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
  public void testRandomKey() throws Exception
  {
    JWTEncoder service = (JWTEncoder)retrieveObjectForSampleConfig();
    AdaptrisMessage message = message();

    service.setGenerateKey(true);
    service.setKeyOutput(new MetadataDataOutputParameter("key"));

    service.doService(message);

    JWTDecoder decoder = new JWTDecoder();
    decoder.setJwtString(new StringPayloadDataInputParameter());
    decoder.setSecret(new MetadataDataInputParameter("key"));
    decoder.setHeader(new MetadataDataOutputParameter("header"));
    decoder.setClaims(new StringPayloadDataOutputParameter());

    decoder.doService(message);

    JSONAssert.assertEquals(HEADER, new JSONObject(message.getMetadataValue("header")), false);
    JSONAssert.assertEquals(CLAIMS, new JSONObject(message.getContent()), false);
  }

  @Test
  public void testNoKeyOutput()
  {
    try
    {
      JWTEncoder service = (JWTEncoder)retrieveObjectForSampleConfig();
      AdaptrisMessage message = message();

      service.setGenerateKey(true);

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
    JWTEncoder encoder = new JWTEncoder();
    encoder.setSecret(new ConstantDataInputParameter(KEY));
    encoder.setHeader(new ConstantDataInputParameter(HEADER.toString()));
    encoder.setClaims(new ConstantDataInputParameter(CLAIMS.toString()));
    encoder.setJwtOutput(new StringPayloadDataOutputParameter());
    return encoder;
  }
}
