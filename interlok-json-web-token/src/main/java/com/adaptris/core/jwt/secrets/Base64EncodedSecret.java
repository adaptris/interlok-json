package com.adaptris.core.jwt.secrets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.Getter;
import lombok.Setter;

@XStreamAlias("base64-encoded-secret")
public class Base64EncodedSecret implements SecretConfigurator {
  @Getter
  @Setter
  @NotBlank
  private String secret;

  @Getter
  @Setter
  private RSAAlgorithms algorithm;

  private enum RSAAlgorithms {
    /**
     * JWA algorithm name for {@code HMAC using SHA-256}
     */
    HS256,

    /**
     * JWA algorithm name for {@code HMAC using SHA-384}
     */
    HS384,

    /**
     * JWA algorithm name for {@code HMAC using SHA-512}
     */
    HS512;
  }

  @Override
  public JwtBuilder configure(JwtBuilder builder) {    
    SignatureAlgorithm algorithmToUse = SignatureAlgorithm.valueOf(getAlgorithm().name());
    SecretKey hmacShaKeyFor = new SecretKeySpec(Decoders.BASE64.decode(secret), algorithmToUse.getJcaName());

    return builder.signWith(hmacShaKeyFor);
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder) {
    SignatureAlgorithm algorithmToUse = SignatureAlgorithm.valueOf(getAlgorithm().name());
    SecretKey hmacShaKeyFor = new SecretKeySpec(Decoders.BASE64.decode(secret), algorithmToUse.getJcaName());
    
    return builder.setSigningKey(hmacShaKeyFor);
  }
}
