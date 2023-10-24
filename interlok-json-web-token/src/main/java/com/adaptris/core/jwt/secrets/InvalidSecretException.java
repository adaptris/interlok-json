package com.adaptris.core.jwt.secrets;

public class InvalidSecretException extends Exception {

  private static final long serialVersionUID = -8523235704649595993L;

  InvalidSecretException(Exception e) {
    super(e);
  }

}
