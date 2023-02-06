package com.adaptris.core.jwt.secrets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

@XStreamAlias("base64-encoded-secret")
public class Base64EncodedSecret implements SecretConfigurator
{
  @Getter
  @Setter
  @NotBlank
  private String secret;
  
  @Getter
  @Setter
  @NotBlank
  @Pattern(regexp = "NONE|HS256|HS384|HS512|RS256|RS384|RS512|ES256|ES384|ES512|PS256|PS384|PS512")
  private String signingAlgorithm;
  
  @Override
  public JwtBuilder configure(JwtBuilder builder)
  {
//    SecretKey hmacShaKeyFor = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    SignatureAlgorithm algorithmToUse = SignatureAlgorithm.valueOf(getSigningAlgorithm());
    SecretKey hmacShaKeyFor = new SecretKeySpec(Decoders.BASE64.decode(secret), algorithmToUse.getJcaName());
    
    return builder.signWith(hmacShaKeyFor);
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder)
  {
    return builder.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)));
  }
}
