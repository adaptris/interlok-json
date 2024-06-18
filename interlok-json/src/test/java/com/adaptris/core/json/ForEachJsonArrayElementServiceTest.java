package com.adaptris.core.json;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessageImp;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class ForEachJsonArrayElementServiceTest extends ExampleServiceCase{

  private String jsonSource = "[\r\n"
      + "    {\r\n"
      + "      \"name\": \"string\",\r\n"
      + "      \"type\": \"SAXON_XSLT\",\r\n"
      + "      \"url\": \"http://localhost:8090/custom-test/transform\"\r\n"
      + "    },\r\n"
      + "    {\r\n"
      + "      \"name\": \"string\",\r\n"
      + "      \"type\": \"XPATH_METADATA\",\r\n"
      + "      \"metadataKey\": \"string\",\r\n"
      + "      \"xpath\": \"string\"\r\n"
      + "    },\r\n"
      + "    {\r\n"
      + "      \"name\": \"string\",\r\n"
      + "      \"type\": \"SomethingElse\",\r\n"
      + "      \"url\": \"http://localhost:8090/custom-test/transform\"\r\n"
      + "    }\r\n"
      + "  ]";
  
  private String notAnArrayJsonSource = "{\r\n"
      + "      \"name\": \"string\",\r\n"
      + "      \"type\": \"SAXON_XSLT\",\r\n"
      + "      \"url\": \"http://localhost:8090/custom-test/transform\"\r\n"
      + "    }";
  
  private ForEachJsonArrayElementService service;
  private AdaptrisMessage message;
    
  @Mock private Service mockService;
  
  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new ForEachJsonArrayElementService();
    service.setForEachElementService(mockService);
    service.setJsonArraySource(new StringPayloadDataInputParameter());
    service.setPerElementDestination(new StringPayloadDataOutputParameter());
    message = DefaultMessageFactory.getDefaultInstance().newMessage(jsonSource);
  }
  
  @Test
  public void testMultipleElements() throws Exception {
    service.doService(message);
    
    verify(mockService, times(3)).doService(message);
  }
  
  @Test
  public void testMultipleElementsSimpleMode() throws Exception {
    service.setJsonParseMode(JsonParseModeEnum.Json_Simple);
    service.doService(message);
    
    verify(mockService, times(3)).doService(message);
  }
  
  @Test
  public void testMultipleElementsRFCRFC4627Mode() throws Exception {
    service.setJsonParseMode(JsonParseModeEnum.RFC4627);
    service.doService(message);
    
    verify(mockService, times(3)).doService(message);
  }
  
  @Test
  public void testWithMultiPayload() throws Exception {
    message = new MultiPayloadMessageFactory().newMessage("json", jsonSource.getBytes());
    ((MultiPayloadAdaptrisMessageImp)message).switchPayload("json");
    
    service.doService(message);
    
    verify(mockService, times(3)).doService(message);
  }
  
  @Test
  public void testNoArray() {
    try {
      message.setContent(notAnArrayJsonSource, message.getContentEncoding());
      service.doService(message);
      fail("Payload does not contain an array, should fail");
    } catch (ServiceException ex) {
      // expected
    }
  }
  
  @Test
  public void testEachPayload() throws Exception {
    service.setForEachElementService(new TestService());
    service.doService(message);
  }
  
  @Test
  public void testNoJson() {
    try {
      message.setContent("<xml-element>", message.getContentEncoding());
      service.setJsonParseMode(JsonParseModeEnum.Strict);
      service.doService(message);
      fail("Payload does not contain json, should fail");
    } catch (ServiceException ex) {
      // expected
    }
  }
  
  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.setForEachElementService(new LogMessageService());
    return service;
  }
  
  class TestService extends ServiceImp {

    static List<String> expectedPayloads;
    static {
      expectedPayloads = new ArrayList<>();
      expectedPayloads.add("SAXON_XSLT");
      expectedPayloads.add("XPATH_METADATA");
      expectedPayloads.add("SomethingElse");
    }
    
    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {      
      int index = expectedPayloads.indexOf(msg.resolve("%payload{jsonpath:$.type}"));
      if(index >= 0) {
        expectedPayloads.remove(index);
      } else {
        throw new ServiceException("Payload not expected.");
      }
    }

    @Override
    public void prepare() throws CoreException {
      // TODO Auto-generated method stub
      
    }

    @Override
    protected void initService() throws CoreException {
      // TODO Auto-generated method stub
      
    }

    @Override
    protected void closeService() {
      // TODO Auto-generated method stub
      
    }
    
  }

}
