package com.adaptris.core.jwt.secrets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.validation.constraints.NotBlank;

import com.adaptris.core.fs.FsHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import lombok.Getter;
import lombok.Setter;

@XStreamAlias("pgp-secret")
public class PGPSecret implements SecretConfigurator {

  @Getter
  @Setter
  @NotBlank
  private String path;

  @Getter
  @Setter
  @NotBlank
  private String password;

  @Override
  public JwtBuilder configure(JwtBuilder builder) throws InvalidSecretException {
    try {
      PGPSecretKey pgpSecretKey = readSecretKey();
      String keyId = Long.toString(pgpSecretKey.getKeyID());
      PrivateKey key = decodePrivateKey(pgpSecretKey);
      return builder.header().keyId(keyId).and().signWith(key);
    } catch (Exception e) {
      throw new InvalidSecretException(e);
    }
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder) throws InvalidSecretException {
    try {
      PGPSecretKey pgpSecretKey = readSecretKey();
      PublicKey key = decodePublicKey(pgpSecretKey);
      return builder.verifyWith(key);
    } catch (Exception e) {
      throw new InvalidSecretException(e);
    }
  }

  private PrivateKey decodePrivateKey(PGPSecretKey pgpSecretKey) throws PasswordException, PGPException {
    String p = Password.decode(ExternalResolver.resolve(password));
    PBESecretKeyDecryptor decryptorFactory = new JcePBESecretKeyDecryptorBuilder().setProvider(PROVIDER).build(p.toCharArray());
    PGPPrivateKey pgpPrivateKey = pgpSecretKey.extractPrivateKey(decryptorFactory);
    JcaPGPKeyConverter converter = new JcaPGPKeyConverter();
    converter.setProvider(new BouncyCastleProvider());
    return converter.getPrivateKey(pgpPrivateKey);
  }

  private PublicKey decodePublicKey(PGPSecretKey pgpSecretKey) throws PGPException {
    PGPPublicKey pgpPublicKey = pgpSecretKey.getPublicKey();
    JcaPGPKeyConverter converter = new JcaPGPKeyConverter();
    converter.setProvider(new BouncyCastleProvider());
    return converter.getPublicKey(pgpPublicKey);
  }

  private PGPSecretKey readSecretKey() throws IOException, PGPException {
    try (InputStream inputStream = new FileInputStream(FsHelper.toFile(path, new File(path)))) {
      PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(inputStream, new JcaKeyFingerprintCalculator());
      /*
       * we just loop through the collection till we find a key suitable for signing, in the real world you would probably want to be a bit
       * smarter about this.
       */
      for (PGPSecretKeyRing secretKeyRing : pgpSec) {
        for (PGPSecretKey secretKey : secretKeyRing) {
          if (secretKey.isSigningKey()) {
            return secretKey;
          }
        }
      }
      throw new IllegalArgumentException("Cannot find signing key in key ring");
    }
  }

}
