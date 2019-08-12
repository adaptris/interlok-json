package com.adaptris.core.json.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.json.JsonToMetadata;
import com.adaptris.core.services.conditional.conditions.ConditionMetadata;
import com.adaptris.core.services.conditional.operator.Equals;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.json.JsonArraySplitter;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.File;

public class JsonArrayAggregatorFilterTest extends ServiceCase {

  private static final String AGX_GROWERS_LIST_URL = "/growers-sync16.json";


  public JsonArrayAggregatorFilterTest() { super("Json Array Aggregator Filter Test"); }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(new JsonToMetadata());
    service.setSplitter(new JsonArraySplitter());

    final JsonArrayAggregator jsonArrayAggregator = new JsonArrayAggregator();
    final ConditionMetadata conditionMetadata = new ConditionMetadata();
    conditionMetadata.setMetadataKey("IsDeleted");

    final Equals equalsOperation = new Equals();
    equalsOperation.setValue("false");
    conditionMetadata.setOperator(equalsOperation);
    jsonArrayAggregator.setFilterCondition(conditionMetadata);
    service.setAggregator(jsonArrayAggregator);
    return service;
  }

  public void testListFilteringWithBooleanMetadata() throws Exception {
    // Read in json as Text
    final String fileContents = IOUtils.toString(
            this.getClass().getResourceAsStream(AGX_GROWERS_LIST_URL),"UTF-8"
    );

    // Create AdaptrisMessage and the SplitJoinService
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(fileContents);
    final Service service = (Service) retrieveObjectForSampleConfig();

    // run
    execute(service, original);

    // Now check payload to ensure we did filter
    assertNotNull(original.getPayload());
    String resultPayload = new String(original.getPayload(), "UTF8");
    // TODO need to do json array based comparision to show that the arrays are different rather than string difference,
    // which will always be due to pretty printing
    assertEquals(false, fileContents.equals(resultPayload));

    final JSONArray aggregatedJsonResult = new JSONArray(resultPayload);
    assertEquals(62, aggregatedJsonResult.length());

    final JSONArray initialJsonArray = new JSONArray(fileContents);
    assertEquals(146, initialJsonArray.length());
  }

  public void testListFilteringWithNullCondition() throws Exception {
    File file = new File(AGX_GROWERS_LIST_URL);
    final String fileContents = IOUtils.toString(
            this.getClass().getResourceAsStream(AGX_GROWERS_LIST_URL),"UTF-8"
    );

    // Create AdaptrisMessage and the SplitJoinService
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(fileContents);
    final SplitJoinService service = (SplitJoinService) retrieveObjectForSampleConfig();
    ((JsonArrayAggregator)service.getAggregator()).setFilterCondition(null);

    // run
    execute(service, original);

    // Now check payload to ensure ensure no change
    assertNotNull(original.getPayload());
    String resultPayload = new String(original.getPayload(), "UTF8");
    // TODO need to introduce json array based comparison here to ensure original and result are the same

    final JSONArray aggregatedJsonResult = new JSONArray(resultPayload);
    assertEquals(146, aggregatedJsonResult.length());

    final JSONArray initialJsonArray = new JSONArray(fileContents);
    assertEquals(146, initialJsonArray.length());
  }

  public void testRetainFilterExceptionsMessages() throws Exception {
    final JsonArrayAggregator jsonArrayAggregator = new JsonArrayAggregator();
    // Test default
    assertNull(jsonArrayAggregator.getRetainFilterExceptionsMessages());
    assertEquals(Boolean.FALSE, jsonArrayAggregator.retainFilterExceptionsMessages());
    // Test flag with true
    jsonArrayAggregator.setRetainFilterExceptionsMessages(true);
    assertEquals(Boolean.TRUE, jsonArrayAggregator.getRetainFilterExceptionsMessages());
    assertEquals(Boolean.TRUE, jsonArrayAggregator.retainFilterExceptionsMessages());
    // Test flag with null
    jsonArrayAggregator.setRetainFilterExceptionsMessages(null);
    assertNull(jsonArrayAggregator.getRetainFilterExceptionsMessages());
    assertEquals(Boolean.FALSE, jsonArrayAggregator.retainFilterExceptionsMessages());
  }
}
