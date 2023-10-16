package com.adaptris.core.jwt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.nio.file.Path;

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
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

public class RSAEncodedSecretTest {

  private static final String PUBLIC_KEY_PATH = "./rsa.public";
  private static final String PRIVATE_KEY_PATH = "./rsa.private";
  private static final String PUBLIC_KEY_WITH_PASSPHRASE_PATH = "./rsaWithPassword.public";
  private static final String PRIVATE_KEY_WITH_PASSPHRASE_PATH = "./rsaWithPassword.private";
  private static final String VALIDATE_JWT_REGEX = "(^[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*$)";

  private static final String INVALID_PRIVATE_KEY = "./privateKey.ppk";
  private static final String INVALID_PUBLIC_KEY = "./publicKey.ppk";

  private static final String JWT_HEADER = "{\r\n" + "  \"typ\": \"JWT\"\r\n" + "}";

  private static final String JWT_CLAIMS = "{\r\n" + "  \"sub\": \"1234567890\",\r\n" + "  \"name\": \"John Doe\",\r\n"
      + "  \"admin\": true\r\n" + "}";

  private JWTEncoder encoder;

  private JWTDecoder decoder;

  private RSAEncodedSecret secret;

  private AdaptrisMessage message;

  private String plainTextPassword;

  private String cryptodPassword;

  @Before
  public void setUp() throws Exception {
    secret = new RSAEncodedSecret();
    secret.setPrivateKeyFilePath(toFile(PRIVATE_KEY_PATH));
    secret.setPublicKeyFilePath(toFile(PUBLIC_KEY_PATH));
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

    plainTextPassword = "test";
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
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }

  @Test
  public void testRsa384() throws Exception {
    secret.setAlgorithm(RSAEncodedSecret.RSAAlgorithms.RS384);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS384"));
  }

  @Test
  public void testRsa512() throws Exception {
    secret.setAlgorithm(RSAEncodedSecret.RSAAlgorithms.RS512);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS512"));
  }

  @Test
  public void testPrivateKeyWithPlainTextPassPhrase() throws Exception {
    secret.setPrivateKeyPassphrase(plainTextPassword);
    secret.setPrivateKeyFilePath(toFile(PRIVATE_KEY_WITH_PASSPHRASE_PATH));
    secret.setPublicKeyFilePath(toFile(PUBLIC_KEY_WITH_PASSPHRASE_PATH));

    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }

  @Test
  public void testPrivateKeyWithCryptoPassPhrase() throws Exception {
    secret.setPrivateKeyPassphrase(cryptodPassword);
    secret.setPrivateKeyFilePath(toFile(PRIVATE_KEY_WITH_PASSPHRASE_PATH));
    secret.setPublicKeyFilePath(toFile(PUBLIC_KEY_WITH_PASSPHRASE_PATH));

    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }

  @Test
  public void testPrivateKeyWithIncorrectPlainTextPassPhrase() throws URISyntaxException {
    plainTextPassword = "incorrect";
    secret.setPrivateKeyPassphrase(plainTextPassword);
    secret.setPrivateKeyFilePath(toFile(PRIVATE_KEY_WITH_PASSPHRASE_PATH));
    secret.setPublicKeyFilePath(toFile(PUBLIC_KEY_WITH_PASSPHRASE_PATH));

    ServiceException assertThrows = assertThrows(ServiceException.class, () -> encoder.doService(message));
    assertTrue(assertThrows.getMessage().contains("exception using cipher - please check password and data"));
  }

  @Test
  public void testPrivateKeyWithIncorrectCryptoPassPhrase() throws PasswordException, URISyntaxException {
    plainTextPassword = "incorrect";
    cryptodPassword = Password.encode(plainTextPassword, Password.PORTABLE_PASSWORD);
    secret.setPrivateKeyPassphrase(cryptodPassword);
    secret.setPrivateKeyFilePath(toFile(PRIVATE_KEY_WITH_PASSPHRASE_PATH));
    secret.setPublicKeyFilePath(toFile(PUBLIC_KEY_WITH_PASSPHRASE_PATH));

    ServiceException assertThrows = assertThrows(ServiceException.class, () -> encoder.doService(message));
    assertTrue(assertThrows.getMessage().contains("exception using cipher - please check password and data"));
  }

  @Test
  public void testInvalidPrivateKeys() throws URISyntaxException {
    secret.setPrivateKeyFilePath(toFile(INVALID_PRIVATE_KEY));

    assertThrows(ServiceException.class, () -> encoder.doService(message));
  }

  @Test
  public void testInvalidPublicKeys() throws URISyntaxException {
    secret.setPublicKeyFilePath(toFile(INVALID_PUBLIC_KEY));

    assertThrows(ServiceException.class, () -> {
      encoder.doService(message);
      decoder.doService(message);
    });
  }

  private String toFile(String resourceName) throws URISyntaxException {
    return Path.of(getClass().getClassLoader().getResource(resourceName).toURI()).toAbsolutePath().toString();
  }
}
