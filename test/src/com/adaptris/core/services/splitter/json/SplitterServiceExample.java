package com.adaptris.core.services.splitter.json;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.splitter.AdvancedMessageSplitterService;
import com.adaptris.core.services.splitter.BasicMessageSplitterService;
import com.adaptris.core.services.splitter.MessageSplitter;
import com.adaptris.core.services.splitter.MessageSplitterServiceImp;

public abstract class SplitterServiceExample extends ServiceCase {

  private static final String BASE_DIR_KEY = "SplitterServiceExamples.baseDir";

  public SplitterServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  static BasicMessageSplitterService createBasic(MessageSplitter ms) {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    service.setConnection(new NullConnection());
    service.setProducer(new NullMessageProducer());
    service.setSplitter(ms);
    return service;
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms, StandaloneProducer p) {
    return createAdvanced(ms, new Service[]
    {
      p
    });
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms, Service[] services) {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    ServiceList sl = new ServiceList(services);
    service.setSplitter(ms);
    service.setService(sl);
    return service;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(createSplitter());
  }

  abstract MessageSplitter createSplitter();

  static List<Service> createExamples(MessageSplitter ms) {
    List<Service> services = new ArrayList<Service>();
    services.add(createBasic(ms));

    AdvancedMessageSplitterService ams = new AdvancedMessageSplitterService();
    ServiceList sl = new ServiceList();
    sl.addService(new WaitService());
    sl.addService(new StandaloneProducer());
    ams.setSplitter(ms);
    ams.setService(sl);

    services.add(ams);

    return services;
  }

  @Override
  protected String createBaseFileName(Object object) {
    MessageSplitterServiceImp splitService = (MessageSplitterServiceImp) object;
    return super.createBaseFileName(object) + "-" + splitService.getSplitter().getClass().getSimpleName();
  }

}
