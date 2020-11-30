package com.adaptris.core.jwt.secrets;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;

public interface SecretConfigurator
{
  String PROVIDER = "BC";

  JwtBuilder configure(JwtBuilder builder) throws InvalidSecretException;

  JwtParserBuilder configure(JwtParserBuilder builder) throws InvalidSecretException;
}
