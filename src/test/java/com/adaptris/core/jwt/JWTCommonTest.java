package com.adaptris.core.jwt;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import org.json.JSONObject;

import java.nio.charset.Charset;

public abstract class JWTCommonTest extends ServiceCase
{
	protected static final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.hhEuJw4rM6XeYHesiLj_RNR-ONRQp34XgwDZIEp5hFLYiXRB8PDvMaZPyAcq9u3_QwWbMthvQU7yORuKOesoHQ";
	protected static final String KEY = "lJMnnsrA5PhBnRXE/QnVzoIACiiUMwGNKVVDtvuAcEQR7MMXVFAceSnZPubva1n5xOxPe/O8f0AO3DBHokky3A==";

	protected static final JSONObject HEADER = new JSONObject("{\"alg\":\"HS512\",\"typ\":\"JWT\"}");
	protected static final JSONObject CLAIMS = new JSONObject("{\"name\":\"John Doe\",\"sub\":\"1234567890\",\"iat\":1516239022}");

	protected AdaptrisMessage message()
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
		message.setContentEncoding(Charset.defaultCharset().name());
		return message;
	}
}
