package com.adaptris.core.json.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.json.JsonArraySplitter;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonMergeAggregatorTest extends ExampleServiceCase {
  protected static final String PARENT_CONTENT   = "{ \"document_type\":\"master\", \"id\":\"101011\", \"creation_date\":\"2017-01-03\" }";
  protected static final String OBJECT_CONTENT_1 = "{ \"firstname\":\"alice\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";
  protected static final String OBJECT_CONTENT_2 = "{ \"firstname\":\"bob\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";
  protected static final String OBJECT_CONTENT_3 = "{ \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";

  private Configuration jsonConfig;

  @Before
  public void setUp() throws Exception {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new JsonArraySplitter());
    final JsonMergeAggregator aggregator = new JsonMergeAggregator();
    aggregator.setMergeMetadataKey("parentMergeKey");
    service.setAggregator(aggregator);
    return service;
  }

  @Test
  public void testAggregate() throws Exception {
    String mergeMetadataKey = "mergeKey";
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(PARENT_CONTENT);
    List<AdaptrisMessage> msgs = create(OBJECT_CONTENT_1, OBJECT_CONTENT_2, OBJECT_CONTENT_3);
    int counter = 0;
    for (AdaptrisMessage msg : msgs) {
      msg.addMetadata(mergeMetadataKey, "mergeAttr" + ++counter);
    }
    JsonMergeAggregator aggr = new JsonMergeAggregator();
    aggr.setMergeMetadataKey(mergeMetadataKey);
    aggr.joinMessage(original, msgs);
    assertNotSame("Hello", original.getContent());
    // Should be in order.
    ReadContext context = JsonPath.parse(original.getInputStream(), jsonConfig);
    assertEquals("master", context.read("$.document_type"));
    assertEquals("alice", context.read("$.mergeAttr1.firstname"));
    assertEquals("bob", context.read("$.mergeAttr2.firstname"));
    assertEquals("carol", context.read("$.mergeAttr3.firstname"));
  }

  @Test
  public void testInvalidParentJson() throws Exception {
    boolean exceptionThrown = false;
    String mergeMetadataKey = "mergeKey";
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("dsadsad{}dsafsdf::''");
    List<AdaptrisMessage> msgs = create(OBJECT_CONTENT_1, OBJECT_CONTENT_2, OBJECT_CONTENT_3);
    int counter = 0;
    for (AdaptrisMessage msg : msgs) {
      msg.addMetadata(mergeMetadataKey, "mergeAttr" + ++counter);
    }
    JsonMergeAggregator aggr = new JsonMergeAggregator();
    aggr.setMergeMetadataKey(mergeMetadataKey);
    try {
      aggr.joinMessage(original, msgs);
      fail("Expecting an excpetion to be throw due to invalid json");
    } catch (CoreException e) {
      exceptionThrown = true;
    } finally {
      assertTrue(exceptionThrown);
    }
  }

  @Test
  public void testSimpleParentJson() throws Exception {
    boolean exceptionThrown = false;
    String mergeMetadataKey = "mergeKey";
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("43");
    List<AdaptrisMessage> msgs = create(OBJECT_CONTENT_1, OBJECT_CONTENT_2, OBJECT_CONTENT_3);
    int counter = 0;
    for (AdaptrisMessage msg : msgs) {
      msg.addMetadata(mergeMetadataKey, "mergeAttr" + ++counter);
    }
    JsonMergeAggregator aggr = new JsonMergeAggregator();
    aggr.setMergeMetadataKey(mergeMetadataKey);
    try {
      aggr.joinMessage(original, msgs);
    } catch (CoreException e) {
      exceptionThrown = false;
    } finally {
      assertFalse(exceptionThrown);
    }
    assertEquals("43", original.getContent());
  }

  @Test
  public void testInvalidChildJson() throws Exception {
    boolean exceptionThrown = false;
    String mergeMetadataKey = "mergeKey";
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(PARENT_CONTENT);
    List<AdaptrisMessage> msgs = create("dsfsdfsdf{}sdf{}::{}fdsf''", OBJECT_CONTENT_2, OBJECT_CONTENT_3);
    int counter = 0;
    for (AdaptrisMessage msg : msgs) {
      msg.addMetadata(mergeMetadataKey, "mergeAttr" + ++counter);
    }
    JsonMergeAggregator aggr = new JsonMergeAggregator();
    aggr.setMergeMetadataKey(mergeMetadataKey);
    try {
      aggr.joinMessage(original, msgs);
      assertNotSame("Hello", original.getContent());
      // Should be in order.
      ReadContext context = JsonPath.parse(original.getInputStream(), jsonConfig);
      assertEquals("master", context.read("$.document_type"));
      try {
        assertEquals("", context.read("$.mergeAttr1.firstname"));
        fail("This child element should not exist");
      } catch (PathNotFoundException pnfe) {}
      assertEquals("bob", context.read("$.mergeAttr2.firstname"));
      assertEquals("carol", context.read("$.mergeAttr3.firstname"));
    } catch (CoreException e) {
      exceptionThrown = true;
    } finally {
      assertFalse(exceptionThrown);
    }
  }

  @Test
  public void testMergeIntoParentArray() throws Exception {
    boolean exceptionThrown = false;
    String mergeMetadataKey = "mergeKey";
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("[" + PARENT_CONTENT + "]");
    List<AdaptrisMessage> msgs = create(OBJECT_CONTENT_1, OBJECT_CONTENT_2, OBJECT_CONTENT_3);
    int counter = 0;
    for (AdaptrisMessage msg : msgs) {
      msg.addMetadata(mergeMetadataKey, "mergeAttr" + ++counter);
    }
    JsonMergeAggregator aggr = new JsonMergeAggregator();
    aggr.setMergeMetadataKey(mergeMetadataKey);
    try {
      aggr.joinMessage(original, msgs);
      assertNotSame("Hello", original.getContent());
      // Should be in order.
      ReadContext context = JsonPath.parse(original.getInputStream(), jsonConfig);
      assertEquals("master", context.read("$[0].document_type"));
    } catch (CoreException e) {
      exceptionThrown = true;
    } finally {
      assertFalse(exceptionThrown);
    }
  }

  private List<AdaptrisMessage> create(String... contents) {
    List<AdaptrisMessage> result = new ArrayList<>();
    for (String s : contents) {
      result.add(AdaptrisMessageFactory.getDefaultInstance().newMessage(s));
    }
    return result;
  }

  protected static ServiceCollection wrap(Service... services) {
    return new ServiceList(services);
  }
}
