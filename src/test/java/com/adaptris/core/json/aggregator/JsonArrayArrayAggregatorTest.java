package com.adaptris.core.json.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.json.JsonArraySplitter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonArrayArrayAggregatorTest extends ServiceCase {

  protected static final String OBJECT_CONTENT_1 = "{ \"firstname\":\"alice\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";
  protected static final String OBJECT_CONTENT_2 = "{ \"firstname\":\"bob\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";
  protected static final String OBJECT_CONTENT_3 = "{ \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }";

  private Configuration jsonConfig;


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
  }


  @Test
  public void testAggregate() throws Exception {
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    List<AdaptrisMessage> msgs = create(String.format("[%s, %s]", OBJECT_CONTENT_1, OBJECT_CONTENT_2), OBJECT_CONTENT_3);
    JsonArrayArrayAggregator aggr = new JsonArrayArrayAggregator();
    aggr.joinMessage(original, msgs);
    assertNotSame("Hello", original.getContent());
    // Should be in order.
    ReadContext context = JsonPath.parse(original.getInputStream(), jsonConfig);
    assertNotNull(context.read("$[0].firstname"));
    assertEquals("alice", context.read("$[0].firstname"));
    assertEquals("bob", context.read("$[1].firstname"));
    assertEquals("carol", context.read("$[2].firstname"));
  }

  @Test
  public void testAggregate_WithFilter() throws Exception {
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    List<AdaptrisMessage> msgs = create(String.format("[%s, %s]", OBJECT_CONTENT_1, OBJECT_CONTENT_2), OBJECT_CONTENT_3);
    JsonArrayArrayAggregator aggr = new JsonArrayArrayAggregator();
    aggr.setFilterCondition(new JsonArrayAggregatorTest.FilterOutBobCondition());
    aggr.joinMessage(original, msgs);
    assertNotSame("Hello", original.getContent());
    // Should be in order.
    ReadContext context = JsonPath.parse(original.getInputStream(), jsonConfig);
    assertNotNull(context.read("$[0].firstname"));
    assertEquals("carol", context.read("$[0].firstname"));
  }

  @Test
  public void testAggregator_NotJson() throws Exception {
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    List<AdaptrisMessage> msgs = create("not", "valid", "json");
    JsonArrayArrayAggregator aggr = new JsonArrayArrayAggregator();
    aggr.joinMessage(original, msgs);
    assertNotSame("Hello", original.getContent());
    assertEquals("[]", StringUtils.deleteWhitespace(original.getContent()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new JsonArraySplitter());
    service.setAggregator(new JsonArrayArrayAggregator());
    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-JsonArrayArrayAggregator";
  }

  protected static ServiceCollection wrap(Service... services) {
    return new ServiceList(services);
  }

  private List<AdaptrisMessage> create(String... contents) {
    List<AdaptrisMessage> result = new ArrayList<>();
    for (String s : contents) {
      result.add(AdaptrisMessageFactory.getDefaultInstance().newMessage(s));
    }
    return result;
  }
}
