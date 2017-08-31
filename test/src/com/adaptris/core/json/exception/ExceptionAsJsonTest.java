package com.adaptris.core.json.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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
  @Rule
  public TestName testName = new TestName();
  private Configuration jsonConfig;

  @Before
  public void setUp() {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

  }

  @After
  public void tearDown() {

  }

  @Test
  public void testSerialize() throws Exception {
    Exception e = new Exception(testName.getMethodName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(testName.getMethodName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithEncoding() throws Exception {
    Exception e = new Exception(testName.getMethodName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setContentEncoding("ISO-8859-1");
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(testName.getMethodName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("ISO-8859-1", msg.getContentEncoding());

  }

  @Test
  public void testSerialize_AllMetadata() throws Exception {
    Exception e = new Exception(testName.getMethodName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, testName.getMethodName() + "_workflow");
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE, testName.getMethodName() + "_cause");
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    System.out.println(msg.getContent());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(testName.getMethodName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertTrue(ctx.read(JSONPATH_WORKFLOW).toString().startsWith(testName.getMethodName()));
    assertTrue(ctx.read(JSONPATH_EXCEPTION_LOCATION).toString().startsWith(testName.getMethodName()));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithException() throws Exception {
    Exception e = new Exception(testName.getMethodName());
    ExceptionAsJson serialize = new ExceptionAsJson();
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, testName.getMethodName() + "_workflow");
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE, testName.getMethodName() + "_cause");
    try {
      serialize.serialize(e, msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testSerialize_WithStackTrace() throws Exception {
    Exception e = new Exception(testName.getMethodName());
    ExceptionWithStacktrace serialize = new ExceptionWithStacktrace();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    serialize.serialize(e, msg);
    assertNotNull(msg.getPayload());
    ReadContext ctx = createJsonContext(msg);
    assertEquals(testName.getMethodName(), ctx.read(JSONPATH_EXCEPTION_MESSAGE));
    assertNotNull(ctx.read(JSONPATH_STACKTRACE));
    assertNull(ctx.read(JSONPATH_WORKFLOW));
    assertNull(ctx.read(JSONPATH_EXCEPTION_LOCATION));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  private ReadContext createJsonContext(AdaptrisMessage msg) throws Exception {
    return JsonPath.parse(msg.getInputStream(), jsonConfig);
  }
}
