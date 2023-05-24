package com.adaptris.core.services.jdbc;

import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.createContext;
import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.createJdbcResult;
import static com.adaptris.core.services.jdbc.JsonResultSetTranslatorTest.execute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.jdbc.JdbcJsonArrayOutput;
import com.jayway.jsonpath.ReadContext;


public class JsonArrayOutputTest {

	@Test
  public void testTranslate() throws Exception {
    JdbcJsonArrayOutput jsonTranslator = new JdbcJsonArrayOutput();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    execute(jsonTranslator, createJdbcResult(), message);

    System.out.println(message.getContent());
    ReadContext ctx = createContext(message);
    assertNotNull(ctx.read("$.[0].[0]"));
    assertNotNull(ctx.read("$.[1].[0]"));
    assertEquals("Anna", ctx.read("$.[0].[1].firstName"));
	}

  @Test
  public void testTranslate_IOException() throws Exception {
    JdbcJsonArrayOutput jsonTranslator = new JdbcJsonArrayOutput();
    AdaptrisMessage message = new BrokenAdaptrisMessage();
    try {
      execute(jsonTranslator, createJdbcResult(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

}
