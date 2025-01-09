package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.jwt.secrets.JWKSecret;
import com.adaptris.core.jwt.secrets.RSAEncodedSecret;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.password.Password;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JWKSecretTest {
  private static final String PUBLIC_KEY_PATH = RSAEncodedSecretTest.PUBLIC_KEY_PATH;
  private static final String PRIVATE_KEY_PATH = RSAEncodedSecretTest.PRIVATE_KEY_PATH;
  private static final String VALIDATE_JWT_REGEX = "(^[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*$)";


  private static final String VALID_STRING_SOURCE = """
    {"keys":
      [{
        "kty": "RSA",
        "use": "sig",
        "alg": "RS256",
        "kid": "8d77a483-7616-4279-9449-300f80946b3b",
        "d": "BjlWvdJqVAtwZ2QrCa00kRzTnJ_nfLki6Jxs18fkLCx85oIkL64se4MeJ1EA1ifizahYPCOqYhAO9h4wNZlEhb0WAYG1nD6w19ZOhJezy5JBV2FY6n2rZ15YSLCflZii1OPGiy9wfCAsZrFykeLa3GNiDj8eaYvkGkPqveL4-zszrUUU3E47RHggRL28SDLwn0nHwTWTJZkKAflpcrM9DA-2aWVrJ1zNjAWRkHkiU93OuD2dX0qLKt8aPZRGzExKs9HUxBFyUpCJKMUaZ9b0RniqIGDeJ_cA3bHAwVPHuPLY4nQhWp6p_fDdPvzzAj1vs3QvMHeUdnVmC1N0DRuyuQ",
        "n": "0G6IMmIhBmEfHHYMiaWCdizVJwy0FH0CMNYzCzcQ4iFd7EC2VEpSoeS_TfwYe5Tb94Q8wqLMjh6-fGwYCqiAlugHJDKjxW92t0mxoVIR9QSypGP6vxATfvxsL7wDbgrIV7lJgvDp1fbpe5kVwi5nxB6rDeORrmmmi3LasVgmfE2JJqux1oiUyQcm-G0gRPpM5-niTgutwoHfnuXOAIYHYvTz-FJv8WmmI0GDohZb6p4jgMT37jN59yrv_fsxiTcNn__i6HJc1ZWr8lKS0pwGmEmxOnN-YXZpT_41FH2RSul29d_3Ig1OVXFJGxNt2bniQOcWlvQjLDI6s7mPBz9Tgw",
        "e": "AQAB",
        "p": "8_24XiCvVi5fPpXgFSEwIpV8e9JxTfsRn9dlIczij5tTiQ_i5hwUywXL7GrEUrQif865ztrAaLnX5yxsKzQq3_LfteF97l7cmFHBwM-0Fxy8saIP1v9SJmhtIT0hr27lNGofI5-95YKeaP9d2Ll764X9NlvEeVFqITYq8-X1okc",
        "q": "2rDD9LCapCdbzyfQzSStJfmvEARjx1wl3lKAWxVUwHjpsezGU3GodPk1i_X0I4GNOPI6kLEW6lV9RbAiRS2Hy9BgEQvl3DbGMayQ7Z7JkmtWgJIcbAQyisKYvJQunVlkKtRaKeDBIuP6njK07crDZnVWGr_9s8qUO2grZpnMhuU",
        "dp": "gcz7tU-sF6wJzXDMSlFimzNtiy30SykDQkbDrYyjBwT6FtOVwRc1VKCb0QUntlvQcq_Wl542eEBOBNTUItPY71T1AHyakyJyNrJf4iRdSWJnXBS8WoNv_7DURbeQ4RSc6Rp_wGLuElg_z9ny4iYjL7QTdkSxrFzMmiiIB0LSP2c",
        "dq": "Idify1wV4vgQdoREBav6fFkjZgg35R_24GzGXtJzVoKgMKBjpYB-wPPc_LdpPKGNWNXsiH5P_J6Uj9eNxkUgskOiyqCbx7RWglOh7pTXv6iLL8TtTy5rIBy0BOOuPz4ZiaNp_PdDDQJZxGg2pmLuUQox9FvoiQ3-WR9rMCr3g50",
        "qi": "SJM31DzoHpaRYOxxgQ838cwHaEFYOjNpz2BcM97ElkhFSLxDbyFpDoTogAUYvcjFLTEh49HLbg1EElj3nsN7j5daHJHNwusdddHAZplzos4ivwGbwvoBCShXplUk3qzi6US64UxA5I-2tfcuxJe7QCsK6hlfLVpkyx9paaEqghY"
       }]
     }
  """;
  private static final String INVALID_STRING_SOURCE = "{}";

  private static final String JWT_HEADER = """
  {
    "typ": "JWT",
    "kid": "8d77a483-7616-4279-9449-300f80946b3b"
  }
  """;

  private static final String JWT_CLAIMS = "{\r\n" + "  \"sub\": \"1234567890\",\r\n" + "  \"name\": \"John Doe\",\r\n"
      + "  \"admin\": true\r\n" + "}";

  private JWTEncoder encoder;

  private JWTDecoder decoder;

  private JWKSecret secret;

  private AdaptrisMessage message;

  private String plainTextPassword;

  private String cryptodPassword;

  static {
    System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
  }

  @RegisterExtension
  static WireMockExtension wm1 = WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort())
          .build();

  @BeforeEach
  public void setUp() throws Exception {
    secret = new JWKSecret();
    RSAEncodedSecret encoderSecret = new RSAEncodedSecret();
    encoderSecret.setPrivateKeyFilePath(RSAEncodedSecretTest.toFile(PRIVATE_KEY_PATH));
    encoderSecret.setPublicKeyFilePath(RSAEncodedSecretTest.toFile(PUBLIC_KEY_PATH));
    encoderSecret.setAlgorithm(RSAEncodedSecret.RSAAlgorithms.RS256);
    secret.setEncoderSecret(encoderSecret);

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

  @AfterEach
  public void tearDown() {
    wm1.resetMappings();
    LifecycleHelper.stopAndClose(encoder);
    LifecycleHelper.stopAndClose(decoder);
  }

  @AfterAll
  public static void tearDownAll() {
    wm1.shutdownServer();
  }

  @Test
  public void testValidStringSource() throws Exception {
    secret.setSource(VALID_STRING_SOURCE);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }

  @Test
  public void testInvalidStringSource() throws Exception {
    secret.setSource(INVALID_STRING_SOURCE);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    assertThrows(ServiceException.class, () -> decoder.doService(message));
    assertNull(message.getMetadataValue("header_out"));
  }


  @Test
  public void testValidUrlSource() throws Exception {
    final String VALID_URL_SOURCE = wm1.getRuntimeInfo().getHttpBaseUrl();

    wm1.stubFor(get("/.well-known/jwks.json").willReturn(okJson(VALID_STRING_SOURCE)));
    secret.setSource(VALID_URL_SOURCE);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    decoder.doService(message);
    assertTrue(message.getMetadataValue("header_out").contains("RS256"));
  }

  @Test
  public void testInvalidUrlSource() throws Exception {
    final String INVALID_URL_SOURCE = "http://localhost1";
    secret.setSource(INVALID_URL_SOURCE);
    encoder.doService(message);
    assertNotNull(message.getContent());
    assertTrue(message.getContent().matches(VALIDATE_JWT_REGEX));

    assertThrows(ServiceException.class, () -> decoder.doService(message));
    assertNull(message.getMetadataValue("header_out"));
  }

}
