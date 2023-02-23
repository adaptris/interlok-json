package com.adaptris.core.jwt.secrets;

import java.io.File;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;

@XStreamAlias("rsa-encoded-secret")
public class RSAEncodedSecret implements SecretConfigurator {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Setter
  @Getter
  private String privateKeyFilePath;

  @Setter
  @Getter
  private String publicKeyFilePath;

  @Setter
  @Getter
  @InputFieldHint(style = "PASSWORD", external=true)
  private String privateKeyPassphrase;

  @Getter
  @Setter
  private RSAAlgorithms algorithm;

  public enum RSAAlgorithms {
    /**
     * JWA algorithm name for {@code RSASSA-PKCS-v1_5 using SHA-256}
     */
    RS256,

    /**
     * JWA algorithm name for {@code RSASSA-PKCS-v1_5 using SHA-384}
     */
    RS384,

    /**
     * JWA algorithm name for {@code RSASSA-PKCS-v1_5 using SHA-512}
     */
    RS512;
  }

  @Override
  public JwtBuilder configure(JwtBuilder builder) throws InvalidSecretException {
    try {
      PrivateKey pk = readPrivateKey(getPrivateKeyFilePath(), Password.decode(getPrivateKeyPassphrase()));
      return builder.signWith(pk, SignatureAlgorithm.valueOf(getAlgorithm().name()));
    } catch (Exception ex) {
      log.error("Error loading private key.", ex);
      throw new InvalidSecretException(ex);
    }
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder) throws InvalidSecretException {
    try {
      return builder.setSigningKey(readPublicKey(getPublicKeyFilePath()));
    } catch (Exception e) {
      log.error("Error loading public key.", e);
      throw new InvalidSecretException(e);
    }
  }

  private PrivateKey readPrivateKey(String filePath, String passphrase) throws Exception {
    PrivateKeyInfo pki;

    try (PEMParser pemParser = new PEMParser(new FileReader(new File(filePath)))) {

      Object pemObject = pemParser.readObject();

      if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
        PKCS8EncryptedPrivateKeyInfo epki = (PKCS8EncryptedPrivateKeyInfo) pemObject;
        JcePKCSPBEInputDecryptorProviderBuilder builder = new JcePKCSPBEInputDecryptorProviderBuilder()
            .setProvider("BC");

        InputDecryptorProvider idp = builder.build(passphrase.toCharArray());

        pki = epki.decryptPrivateKeyInfo(idp);
      } else if (pemObject instanceof PEMEncryptedKeyPair) {

        PEMEncryptedKeyPair epki = (PEMEncryptedKeyPair) pemObject;
        PEMKeyPair pkp = epki.decryptKeyPair(new BcPEMDecryptorProvider(passphrase.toCharArray()));

        pki = pkp.getPrivateKeyInfo();

      } else if (pemObject instanceof PEMKeyPair) {
        pki = ((PEMKeyPair) pemObject).getPrivateKeyInfo();
      } else {
        throw new PKCSException("Invalid private key class: " + pemObject.getClass().getName());
      }

      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
      return converter.getPrivateKey(pki);
    }
  }

  public RSAPublicKey readPublicKey(String filePath) throws Exception {
    KeyFactory factory = KeyFactory.getInstance("RSA");

    try (FileReader keyReader = new FileReader(new File(filePath)); PemReader pemReader = new PemReader(keyReader)) {

      PemObject pemObject = pemReader.readPemObject();
      byte[] content = pemObject.getContent();
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
      return (RSAPublicKey) factory.generatePublic(pubKeySpec);
    }
  }
}
