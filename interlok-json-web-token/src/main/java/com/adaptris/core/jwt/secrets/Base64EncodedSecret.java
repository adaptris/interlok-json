package com.adaptris.core.jwt.secrets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.adaptris.annotation.InputFieldDefault;
import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
  @InputFieldDefault(value = "AUTO")
  private JwtSigningAlgorithm signingAlgorithm;

  @Override
  public JwtBuilder configure(JwtBuilder builder) {
    SecretKey hmacShaKeyFor = null;
    if (getSigningAlgorithm() == JwtSigningAlgorithm.AUTO) {
      hmacShaKeyFor = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    } else {
      SignatureAlgorithm algorithmToUse = SignatureAlgorithm.valueOf(getSigningAlgorithm().name());
      hmacShaKeyFor = new SecretKeySpec(Decoders.BASE64.decode(secret), algorithmToUse.getJcaName());
    }

    return builder.signWith(hmacShaKeyFor);
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder) {
    return builder.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)));
  }
}
