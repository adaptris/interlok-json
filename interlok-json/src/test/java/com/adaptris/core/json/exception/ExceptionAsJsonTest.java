package com.adaptris.core.json.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.EnumSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class ExceptionAsJsonTest {
  private static final String JSONPATH_EXCEPTION_MESSAGE = "$.exceptionMessage";
  private static final String JSONPATH_EXCEPTION_LOCATION = "$.exceptionLocation";
  private static final String JSONPATH_WORKFLOW = "$.workflow";
  private static final String JSONPATH_STACKTRACE = "$.stacktrace";

  private Configuration jsonConfig;

  @BeforeEach
  public void setUp() {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void testSerialize(TestInfo info) throws Exception {
    Exception e = new Exception(info.getDisplayName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(info.getDisplayName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithEncoding(TestInfo info) throws Exception {
    Exception e = new Exception(info.getDisplayName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setContentEncoding("ISO-8859-1");
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(info.getDisplayName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("ISO-8859-1", msg.getContentEncoding());

  }

  @Test
  public void testSerialize_AllMetadata(TestInfo info) throws Exception {
    Exception e = new Exception(info.getDisplayName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, info.getDisplayName() + "_workflow");
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE, info.getDisplayName() + "_cause");
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    System.out.println(msg.getContent());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(info.getDisplayName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertTrue(ctx.read(JSONPATH_WORKFLOW).toString().startsWith(info.getDisplayName()));
    assertTrue(ctx.read(JSONPATH_EXCEPTION_LOCATION).toString().startsWith(info.getDisplayName()));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithException(TestInfo info) throws Exception {
    Exception e = new Exception(info.getDisplayName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, info.getDisplayName() + "_workflow");
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE, info.getDisplayName() + "_cause");
    try {
      serialize.serialize(e, msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testSerialize_WithStackTrace(TestInfo info) throws Exception {
    Exception e = new Exception(info.getDisplayName());
    ExceptionWithStacktrace serialize = new ExceptionWithStacktrace();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(info.getDisplayName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNotNull(ctx.read(JSONPATH_STACKTRACE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  private ReadContext createJsonContext(AdaptrisMessage msg) throws Exception {
    return JsonPath.parse(msg.getInputStream(), jsonConfig);
  }
}
