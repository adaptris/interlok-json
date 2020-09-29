package com.adaptris.core.json.streaming;

import com.adaptris.core.ServiceList;
import com.adaptris.core.services.splitter.AdvancedMessageSplitterService;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class JsonSplitterTest extends ExampleServiceCase {


  @Override
  protected AdvancedMessageSplitterService retrieveObjectForSampleConfig() {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    service.setService(new ServiceList());
    service.setSplitter(new JsonStreamingSplitter("/path/to/nested/item"));
    return service;
  }

}
