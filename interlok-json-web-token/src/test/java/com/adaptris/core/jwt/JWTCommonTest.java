package com.adaptris.core.jwt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.jwt.secrets.PGPSecret;
import com.adaptris.core.jwt.secrets.SecretConfigurator;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public abstract class JWTCommonTest extends ExampleServiceCase {
  protected String pgpPath = null;
  protected String wrongKey = null;
  protected static final String PASSPHRASE = "passphrase";

  private static final String ID = "email@example.com";

  protected static final String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJCb2IiLCJhdWQiOlsieW91Il0sIm5iZiI6MTU3NzgzNjgwMCwiaXNzIjoibWUiLCJleHAiOjIyNDA1MjQ4MDAsImlhdCI6MTU3NzgzNjgwMCwianRpIjoiNGYwNDQzMjItNWRiMy00NGQyLWE2OTgtMTViNzU0YmQ3YTA1In0.-HZlEDmHJwgg3cVxfcV13I7nx-hjoDJ-kxxCV_M94ONk_n2yjJujneo1x1waa28rfpeeR_1Hej3Yn-ieEfYhLw";
  protected static final String KEY = "lJMnnsrA5PhBnRXE/QnVzoIACiiUMwGNKVVDtvuAcEQR7MMXVFAceSnZPubva1n5xOxPe/O8f0AO3DBHokky3A==";

  protected static final JSONObject HEADER = new JSONObject("{\"alg\": \"HS512\"}");
  protected static final JSONObject CLAIMS = new JSONObject(
      "{\"sub\": \"Bob\", \"aud\": [\"you\"], \"nbf\": 1577836800, \"iss\": \"me\", \"exp\": 2240524800, \"iat\": 1577836800, \"jti\": \"4f044322-5db3-44d2-a698-15b754bd7a05\"}");

  protected AdaptrisMessage message() {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.setContentEncoding(Charset.defaultCharset().name());
    return message;
  }

  @BeforeEach
  public void setUp() throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", SecretConfigurator.PROVIDER);
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();
    PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
    PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());
    PGPSecretKey privateKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, keyPair, ID, sha1Calc, null, null,
        new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
        new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calc).setProvider(SecretConfigurator.PROVIDER)
            .build(PASSPHRASE.toCharArray()));

    File keyFile = File.createTempFile(this.getClass().getName().concat("-"), null);
    try (OutputStream outputStream = new FileOutputStream(keyFile)) {
      privateKey.encode(outputStream);
    }
    pgpPath = keyFile.getPath();

    PGPPublicKey publicKey = privateKey.getPublicKey();
    keyFile = File.createTempFile(this.getClass().getName().concat("-"), null);
    try (OutputStream outputStream = new FileOutputStream(keyFile)) {
      publicKey.encode(outputStream);
    }
    wrongKey = keyFile.getPath();
  }

  protected PGPSecret getPGPSecret() {
    PGPSecret pgpSecret = new PGPSecret();
    pgpSecret.setPath(pgpPath);
    pgpSecret.setPassword(PASSPHRASE);
    return pgpSecret;
  }

}
