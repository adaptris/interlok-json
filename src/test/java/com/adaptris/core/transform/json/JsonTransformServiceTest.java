package com.adaptris.core.transform.json;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.transform.TransformServiceExample;

public class JsonTransformServiceTest extends TransformServiceExample {
  
  private static final String METADATA_KEY = "data-key";
  
  private JsonTransformService service;
  private StringPayloadDataInputParameter payloadInput;
  private StringPayloadDataOutputParameter payloadOutput;
  private MetadataDataInputParameter metadataInput;
  private ConstantDataInputParameter constantInput;
  
  private AdaptrisMessage message;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  
  @Before
  public void setUp() throws Exception {
    service = new JsonTransformService();
    payloadInput = new StringPayloadDataInputParameter();
    metadataInput = new MetadataDataInputParameter(METADATA_KEY);
    payloadOutput = new StringPayloadDataOutputParameter();
    constantInput = new ConstantDataInputParameter(sampleSpec);
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Test
  public void testSimpleTransform_MetadataInputMapping() throws Exception {
    service.setSourceJson(payloadInput);
    service.setMappingSpec(metadataInput);
    service.setTargetJson(payloadOutput);
    
    message.setContent(sampleInput, message.getContentEncoding());
    message.addMetadata(METADATA_KEY, sampleSpec);
    
    service.doService(message);
    
    assertEquals(sampleOutput, message.getContent());
  }
  
  @Test
  public void testSimpleTransform_ConstantInputMapping() throws Exception {
    service.setSourceJson(payloadInput);
    service.setMappingSpec(constantInput);
    service.setTargetJson(payloadOutput);

    message.setContent(sampleInput, message.getContentEncoding());
    message.addMetadata(METADATA_KEY, sampleSpec);

    service.doService(message);

    assertEquals(sampleOutput, message.getContent());
  }

  @Test
  public void testSimpleTransformWithVarSubButNoMetadatMatches() throws Exception {
    service.setSourceJson(payloadInput);
    service.setMappingSpec(metadataInput);
    service.setTargetJson(payloadOutput);
    
    service.setMetadataFilter(new NoOpMetadataFilter());
    
    message.setContent(sampleInput, message.getContentEncoding());
    message.addMetadata(METADATA_KEY, sampleSpec);
    message.addMetadata("SomeKey", "SomeValue");
    
    service.doService(message);
    
    assertEquals(sampleOutput, message.getContent());
  }
  
  @Test
  public void testSimpleTransformWithVarSub() throws Exception {
    service.setSourceJson(payloadInput);
    service.setMappingSpec(metadataInput);
    service.setTargetJson(payloadOutput);
    
    service.setMetadataFilter(new NoOpMetadataFilter());
    
    message.setContent(sampleInput, message.getContentEncoding());
    message.addMetadata(METADATA_KEY, sampleSpecVarSub);
    message.addMetadata("tertiary-ratings", "TertiaryRatings");
    
    service.doService(message);
    
    assertEquals(sampleOutputVarSub, message.getContent());
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.setSourceJson(payloadInput);
    FileDataInputParameter in = new FileDataInputParameter();
    in.setDestination(new ConfiguredDestination("file:///path/to/my/mapping.json"));
    service.setMappingSpec(in);
    service.setTargetJson(payloadOutput);
    
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
