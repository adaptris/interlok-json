package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import io.jsonwebtoken.Claims;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JWTCreatorTest extends JWTCommonTest
{
  private SimpleDateFormat PARSER = new SimpleDateFormat("yyyy-MM-dd");

  @Test
  public void testCreate() throws Exception
  {
    JWTCreator service = (JWTCreator)retrieveObjectForSampleConfig();
    service.setId("4f044322-5db3-44d2-a698-15b754bd7a05");
    service.setIssuedAt(PARSER.parse("2020-01-01"));
    AdaptrisMessage message = message();

    service.doService(message);

    assertEquals(JWT, message.getContent());
  }

  @Test
  public void testCreateClaims() throws Exception
  {
    JWTCreator service = (JWTCreator)retrieveObjectForSampleConfig();

    KeyValuePairSet claims = new KeyValuePairSet();
    claims.addKeyValuePair(new KeyValuePair("custom-claim-1", "value-1"));
    claims.addKeyValuePair(new KeyValuePair("custom-claim-2", "value-2"));
    service.setCustomClaims(claims);

    AdaptrisMessage message = message();

    service.doService(message);

    JWTDecoder decoder = new JWTDecoder();
    decoder.setJwtString(new StringPayloadDataInputParameter());
    decoder.setSecret(new ConstantDataInputParameter(KEY));
    decoder.setHeader(new MetadataDataOutputParameter("header"));
    decoder.setClaims(new StringPayloadDataOutputParameter());

    decoder.doService(message);

    JSONObject json = new JSONObject(message.getContent());

    assertEquals(CLAIMS.get(Claims.SUBJECT), json.get(Claims.SUBJECT));
    assertEquals(CLAIMS.get(Claims.AUDIENCE), json.get(Claims.AUDIENCE));
    assertEquals(CLAIMS.get(Claims.ISSUER), json.get(Claims.ISSUER));
    assertEquals(CLAIMS.get(Claims.EXPIRATION), json.get(Claims.EXPIRATION));
    assertEquals(CLAIMS.get(Claims.NOT_BEFORE), json.get(Claims.NOT_BEFORE));
    assertTrue(json.has(Claims.ISSUED_AT));
    assertTrue(json.has(Claims.ID));
    assertEquals("value-1", json.getString("custom-claim-1"));
    assertEquals("value-2", json.getString("custom-claim-2"));
  }

  @SneakyThrows
  @Override
  protected Object retrieveObjectForSampleConfig()
  {
    JWTCreator creator = new JWTCreator();
    creator.setIssuer("me");
    creator.setSubject("Bob");
    creator.setAudience("you");
    creator.setExpiration(PARSER.parse("2040-12-31"));
    creator.setNotBefore(PARSER.parse("2020-01-01"));
    creator.setSecret(KEY);
    return creator;
  }
}
