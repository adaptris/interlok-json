package com.adaptris.core.jwt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.RSAEncodedSecret;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.password.Password;

public class RSAEncodedSecretTest {

  private static final String PUBLIC_KEY_PATH = "./src/test/resources/rsa.public";
  private static final String PRIVATE_KEY_PATH = "./src/test/resources/rsa.private";
  
  private static final String INVALID_PRIVATE_KEY = "./src/test/resources/privateKey.pkk";
  
  private static final String JWT_HEADER = "{\r\n" + 
      "  \"typ\": \"JWT\"\r\n" + 
      "}";
  
  private static final String JWT_CLAIMS = "{\r\n" + 
      "  \"sub\": \"1234567890\",\r\n" + 
      "  \"name\": \"John Doe\",\r\n" + 
      "  \"admin\": true\r\n" + 
      "}";
  
  private JWTEncoder encoder;
  
  private JWTDecoder decoder;
  
  private RSAEncodedSecret secret;
  
  private AdaptrisMessage message;
  
  private String plainTextPassword;
  
  private String cryptodPassword;
  
  @Before
  public void setUp() throws Exception {
    secret = new RSAEncodedSecret();
    secret.setPrivateKeyFilePath(PRIVATE_KEY_PATH);
    secret.setPublicKeyFilePath(PUBLIC_KEY_PATH);
    secret.setAlgorithm(RSAEncodedSecret.RSAAlgorithms.RS256);
    
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
    
    plainTextPassword = "thePassword";
    cryptodPassword = Password.encode(plainTextPassword, Password.PORTABLE_PASSWORD);
    
    LifecycleHelper.initAndStart(encoder);
    LifecycleHelper.initAndStart(decoder);
  }
  
  @After
  public void tearDown() {
    LifecycleHelper.stopAndClose(encoder);
    LifecycleHelper.stopAndClose(decoder);
  }
  
  @Test
  public void testRsa256() throws Exception {
    encoder.doService(message);
    assertNotNull(message.getContent());
    
    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }
  
  //TODO: Test RSA 384 / 512
  
  //TODO: Password test with correct and incorrect passwords.
  
  @Test
  public void testInvalidPrivateKeys() {
    secret.setPrivateKeyFilePath(INVALID_PRIVATE_KEY);
    
    try {
      encoder.doService(message);
      fail("Should have thrown an exception with an invalid private key.");
     } catch (ServiceException ex) {
      // this is a test pass
    }
  }
  
}
