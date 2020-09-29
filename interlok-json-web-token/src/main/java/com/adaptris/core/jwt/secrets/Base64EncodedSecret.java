package com.adaptris.core.jwt.secrets;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@XStreamAlias("base64-encoded-secret")
public class Base64EncodedSecret implements SecretConfigurator
{
  @Getter
  @Setter
  @NotBlank
  private String secret;

  @Override
  public JwtBuilder configure(JwtBuilder builder)
  {
    return builder.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)));
  }

  @Override
  public JwtParserBuilder configure(JwtParserBuilder builder)
  {
    return builder.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)));
  }
}
