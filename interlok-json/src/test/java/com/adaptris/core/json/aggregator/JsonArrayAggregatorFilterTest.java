package com.adaptris.core.json.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.json.JsonToMetadata;
import com.adaptris.core.services.conditional.conditions.ConditionMetadata;
import com.adaptris.core.services.conditional.operator.Equals;
import com.adaptris.core.services.splitter.PooledSplitJoinService;
import com.adaptris.core.services.splitter.json.JsonArraySplitter;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class JsonArrayAggregatorFilterTest extends ExampleServiceCase {

  private static final String AGX_GROWERS_LIST_URL = "/growers-sync16.json";


  public JsonArrayAggregatorFilterTest() { }


  @Override
  protected PooledSplitJoinService retrieveObjectForSampleConfig() {
    PooledSplitJoinService service = new PooledSplitJoinService();
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

  @Test
  public void testListFilteringWithBooleanMetadata() throws Exception {
    // Read in json as Text
    final String fileContents = IOUtils.toString(
        this.getClass().getResourceAsStream(AGX_GROWERS_LIST_URL),"UTF-8"
        );

    // Create AdaptrisMessage and the SplitJoinService
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(fileContents);
    final Service service = retrieveObjectForSampleConfig();

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

  @Test
  public void testListFilteringWithNullCondition() throws Exception {
    final String fileContents = IOUtils.toString(
        this.getClass().getResourceAsStream(AGX_GROWERS_LIST_URL),"UTF-8"
        );

    // Create AdaptrisMessage and the SplitJoinService
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage(fileContents);
    final PooledSplitJoinService service = retrieveObjectForSampleConfig();
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

  @Test
  public void testRetainFilterExceptionsMessages() throws Exception {
    final JsonArrayAggregator jsonArrayAggregator = new JsonArrayAggregator();
    // Test default
    assertNull(jsonArrayAggregator.getRetainFilterExceptionsMessages());
    // Test flag with true
    jsonArrayAggregator.setRetainFilterExceptionsMessages(true);
    assertEquals(Boolean.TRUE, jsonArrayAggregator.getRetainFilterExceptionsMessages());
    // Test flag with null
    jsonArrayAggregator.setRetainFilterExceptionsMessages(null);
    assertNull(jsonArrayAggregator.getRetainFilterExceptionsMessages());
  }
}
