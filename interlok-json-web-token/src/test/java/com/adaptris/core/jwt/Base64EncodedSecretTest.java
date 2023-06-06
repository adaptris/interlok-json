package com.adaptris.core.jwt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.Base64EncodedSecret;
import com.adaptris.core.util.LifecycleHelper;

public class Base64EncodedSecretTest {

  private static final String SECRET = "YRttfaMhurlwXuHjMH4WaJyVKrXW/agESnbivR0cnAw=";
  private static final String VALIDATE_JWT_REGEX = "(^[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*$)";

  private static final String JWT_HEADER = "{\r\n" + "  \"typ\": \"JWT\"\r\n" + "}";

  private static final String JWT_CLAIMS = "{\r\n" + "  \"sub\": \"1234567890\",\r\n" + "  \"name\": \"John Doe\",\r\n"
      + "  \"admin\": true\r\n" + "}";

  private JWTEncoder encoder;

  private JWTDecoder decoder;

  private Base64EncodedSecret secret;

  private AdaptrisMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    secret = new Base64EncodedSecret();
    secret.setSecret(SECRET);

    encoder = new JWTEncoder();
    encoder.setClaims(new MetadataDataInputParameter("claims"));
    encoder.setHeader(new MetadataDataInputParameter("header"));
    encoder.setSecret(secret);
    encoder.setJwtOutput(new StringPayloadDataOutputParameter());

    decoder = new JWTDecoder();
    decoder.setClaims(new MetadataDataOutputParameter("claims_out"));
    decoder.setHeader(new MetadataDataOutputParameter("header_out"));
    decoder.setSecret(secret);
    decoder.setJwtString(new StringPayloadDataInputParameter());

    message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMessageHeader("claims", JWT_CLAIMS);
    message.addMessageHeader("header", JWT_HEADER);

    LifecycleHelper.initAndStart(encoder);
    LifecycleHelper.initAndStart(decoder);
  }

  @AfterEach
  public void tearDown() {
    LifecycleHelper.stopAndClose(encoder);
    LifecycleHelper.stopAndClose(decoder);
  }

  @Test
  public void testHmac256() throws Exception {
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("HS256"));
  }

  @Test
  public void testEncodingWithInvalidBitLengthSecret() {
    secret.setSecret("invalid");

    try {
      encoder.doService(message);
      fail("Should have thrown an exception with an invalid secret bit length for the encoder.");
    } catch (ServiceException ex) {
    }
  }

  @Test
  public void testDecodingWithInvalidBitLengthSecret() {

    try {
      encoder.doService(message);

      secret.setSecret("invalid");
      decoder.doService(message);
      fail("Should have thrown an exception with an invalid secret bit length for the decoder.");
    } catch (ServiceException ex) {
    }
  }

  @Test
  public void testDecodingWithIncorrectSecret() {

    try {
      encoder.doService(message);

      secret.setSecret("YRttfaMhurlwXuHjMH4WaJyVKrXW/agESnbivR0cnAw9");
      decoder.doService(message);
      fail("Should have thrown an exception with an incorrect secret when decoding.");
    } catch (ServiceException ex) {
    }
  }
}
