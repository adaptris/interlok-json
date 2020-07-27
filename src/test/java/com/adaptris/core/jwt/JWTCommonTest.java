package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import org.json.JSONObject;

import java.nio.charset.Charset;

public abstract class JWTCommonTest extends ServiceCase
{
  protected static final String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJCb2IiLCJhdWQiOiJ5b3UiLCJuYmYiOjE1Nzc4MzY4MDAsImlzcyI6Im1lIiwiZXhwIjoyMjQwNTI0ODAwLCJpYXQiOjE1Nzc4MzY4MDAsImp0aSI6IjRmMDQ0MzIyLTVkYjMtNDRkMi1hNjk4LTE1Yjc1NGJkN2EwNSJ9.7BQ0AQLS3_2ywUAtRHgWjn6UK04yvRRi_Epll4hwjuuUw7xKVqDvo-WJlt2s-4MhpAaiHi8sJAuP2ZyOPjmwqQ";
  protected static final String KEY = "lJMnnsrA5PhBnRXE/QnVzoIACiiUMwGNKVVDtvuAcEQR7MMXVFAceSnZPubva1n5xOxPe/O8f0AO3DBHokky3A==";

  protected static final JSONObject HEADER = new JSONObject("{\"alg\": \"HS512\"}");
  protected static final JSONObject CLAIMS = new JSONObject("{\"sub\": \"Bob\", \"aud\": \"you\", \"nbf\": 1577836800, \"iss\": \"me\", \"exp\": 2240524800, \"iat\": 1577836800, \"jti\": \"4f044322-5db3-44d2-a698-15b754bd7a05\"}");

  protected AdaptrisMessage message()
  {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.setContentEncoding(Charset.defaultCharset().name());
    return message;
  }

  @Override
  public boolean isAnnotatedForJunit4()
  {
    return true;
  }
}
