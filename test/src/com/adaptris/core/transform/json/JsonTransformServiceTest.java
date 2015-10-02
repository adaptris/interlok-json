package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.common.MetadataDataDestination;
import com.adaptris.core.common.PayloadDataDestination;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.transform.TransformServiceExample;

public class JsonTransformServiceTest extends TransformServiceExample {
  
  private static final String METADATA_KEY = "data-key";
  
  private JsonTransformService service;
  private PayloadDataDestination payloadDataDestination;
  private MetadataDataDestination metadataDataDestination;
  
  private AdaptrisMessage message;

  public JsonTransformServiceTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    service = new JsonTransformService();
    payloadDataDestination = new PayloadDataDestination();
    metadataDataDestination = new MetadataDataDestination();
    metadataDataDestination.setMetadataKey(METADATA_KEY);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testSimpleTransform() throws Exception {
    service.setSourceJsonDestination(payloadDataDestination);
    service.setSourceSpecDestination(metadataDataDestination);
    service.setTargetResultDestination(payloadDataDestination);
    
    message.setContent(sampleInput, message.getCharEncoding());
    message.addMetadata(METADATA_KEY, sampleSpec);
    
    service.doService(message);
    
    assertEquals(sampleOutput, message.getContent());
  }
  
  public void testSimpleTransformWithVarSubButNoMetadatMatches() throws Exception {
    service.setSourceJsonDestination(payloadDataDestination);
    service.setSourceSpecDestination(metadataDataDestination);
    service.setTargetResultDestination(payloadDataDestination);
    
    service.setMetadataFilter(new NoOpMetadataFilter());
    
    message.setContent(sampleInput, message.getCharEncoding());
    message.addMetadata(METADATA_KEY, sampleSpec);
    message.addMetadata("SomeKey", "SomeValue");
    
    service.doService(message);
    
    assertEquals(sampleOutput, message.getContent());
  }
  
  public void testSimpleTransformWithVarSub() throws Exception {
    service.setSourceJsonDestination(payloadDataDestination);
    service.setSourceSpecDestination(metadataDataDestination);
    service.setTargetResultDestination(payloadDataDestination);
    
    service.setMetadataFilter(new NoOpMetadataFilter());
    
    message.setContent(sampleInput, message.getCharEncoding());
    message.addMetadata(METADATA_KEY, sampleSpecVarSub);
    message.addMetadata("tertiary-ratings", "TertiaryRatings");
    
    service.doService(message);
    
    assertEquals(sampleOutputVarSub, message.getContent());
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.setSourceJsonDestination(payloadDataDestination);
    service.setSourceSpecDestination(metadataDataDestination);
    service.setTargetResultDestination(new PayloadDataDestination());
    
    return service;
  }

  private String sampleInput = ""
      + "{" + 
    "\"rating\": {" + 
    "    \"primary\": {" + 
    "        \"values\": 3" +
    "    }," +
    "    \"quality\": {" +
    "        \"values\": 3" +
    "    }" +
    "}" +
    "}";
  
  private String sampleSpec = ""
      + "[" +
    "{" + 
    "    \"operation\": \"shift\"," + 
    "    \"spec\": {" + 
    "        \"rating\": {" + 
    "            \"primary\": {" + 
    "                \"values\": \"Rating\"" +
    "            }," +
    "            \"*\": {" +
    "                \"values\": \"SecondaryRatings.&1.Value\"," +
    "                \"$\": \"SecondaryRatings.&.Id\"" + 
    "            }" +
    "        }" +
    "    }" +
    "}," +
    "{" +
    "    \"operation\": \"default\"," + 
    "    \"spec\": {" +
    "        \"Range\" : 5," +
    "        \"SecondaryRatings\" : {" +
    "            \"*\" : {" +
    "                \"Range\" : 5" +
    "            }" +
    "        }" +
    "    }" +
    "}" +
    "]";
  
  private String sampleSpecVarSub = ""
      + "[" +
    "{" + 
    "    \"operation\": \"shift\"," + 
    "    \"spec\": {" + 
    "        \"rating\": {" + 
    "            \"primary\": {" + 
    "                \"values\": \"Rating\"" +
    "            }," +
    "            \"*\": {" +
    "                \"values\": \"${tertiary-ratings}.&1.Value\"," +
    "                \"$\": \"${tertiary-ratings}.&.Id\"" + 
    "            }" +
    "        }" +
    "    }" +
    "}," +
    "{" +
    "    \"operation\": \"default\"," + 
    "    \"spec\": {" +
    "        \"Range\" : 5," +
    "        \"${tertiary-ratings}\" : {" +
    "            \"*\" : {" +
    "                \"Range\" : 5" +
    "            }" +
    "        }" +
    "    }" +
    "}" +
    "]";
  
  private String sampleOutput = "{\"Rating\":3,\"SecondaryRatings\":{\"quality\":{\"Id\":\"quality\",\"Value\":3,\"Range\":5}},\"Range\":5}";
  
  private String sampleOutputVarSub = "{\"Rating\":3,\"TertiaryRatings\":{\"quality\":{\"Id\":\"quality\",\"Value\":3,\"Range\":5}},\"Range\":5}";
}
