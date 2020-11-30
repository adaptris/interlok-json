package com.adaptris.core.jwt.secrets;

public class InvalidSecretException extends Exception
{
  InvalidSecretException(Exception e)
  {
    super(e);
  }
}
