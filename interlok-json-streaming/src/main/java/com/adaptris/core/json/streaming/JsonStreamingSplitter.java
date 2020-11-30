package com.adaptris.core.json.streaming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import javax.validation.constraints.NotBlank;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.stax.lms.StaxSplitGenerator;
import com.adaptris.stax.lms.StaxSplitGeneratorConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.JsonXMLStreamConstants;

/**
 * Splitter implementation that splits based on JSON streaming events.
 * <p>
 * Note that this is only a pseudo-xpath evaluator as it only allows simple element traversal and not any XPath functions.
 * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
 * {@link XMLEventReader} and navigates based on {@link StartElement} events only.
 * </p>
 *
 * @config json-streaming-splitter
 * @author mwarman
 */
@XStreamAlias("json-streaming-splitter")
@DisplayOrder(order = {"path", "bufferSize", "suppressPathNotFound", "wrapWithArray", "jsonStreamingConfig"})
@ComponentProfile(since = "3.8.2")
public class JsonStreamingSplitter extends MessageSplitterImp {

  private transient static final int DEFAULT_BUFFER_SIZE = 8192;

  @NotBlank
  private String path;

  @AdvancedConfig
  private JsonStreamingConfigBuilder jsonStreamingConfig;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean suppressPathNotFound;

  @AdvancedConfig
  private Integer bufferSize;

  @AdvancedConfig
  private Boolean wrapWithArray;

  public JsonStreamingSplitter() {
    //set defaults for backwards compatibility reasons
    jsonStreamingConfig = new JsonStreamingConfigBuilder().withAutoArray(true).withAutoPrimitive(true);
  }

  public JsonStreamingSplitter(String path) {
    this();
    setPath(path);
  }
  @Override
  public Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {
      String thePath = msg.resolve(getPath());
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      XMLEventReader reader = new JsonXMLInputFactory().createXMLEventReader(buf);
      JsonXMLConfig config = getJsonStreamingConfig().build();
      XMLEventFactory eventFactory = XMLEventFactory.newInstance();
      return new JsonStreamingSplitGenerator(
          new JsonStreamingSplitGeneratorConfig()
              .withOriginalMessage(msg)
              .withJsonXMLConfig(config)
              .withXMLEventFactory(eventFactory)
              .withWrapWithArray(wrapWithArray())
              .withXmlEventReader(reader)
              .withSuppressPathNotFound(suppressPathNotFound())
              .withPath(thePath)
              .withInputReader(buf));
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  /**
   * Set the internal buffer size.
   * <p>
   * This is used when; the default buffer size matches the default buffer size in {@link BufferedReader} and {@link BufferedWriter}
   * , changes to the buffersize will impact performance and memory usage depending on the underlying operating system/disk.
   * </p>
   *
   * @param b the buffer size (default is 8192).
   */
  public void setBufferSize(Integer b) {
    bufferSize = b;
  }

  protected int bufferSize() {
    return getBufferSize() != null ? getBufferSize().intValue() : DEFAULT_BUFFER_SIZE;
  }

  public String getPath() {
    return path;
  }

  public void setWrapWithArray(Boolean wrapWithArray) {
    this.wrapWithArray = wrapWithArray;
  }

  public Boolean getWrapWithArray() {
    return wrapWithArray;
  }

  protected boolean wrapWithArray() {
    return BooleanUtils.toBooleanDefaultIfNull(getWrapWithArray(), false);
  }

  public Boolean getSuppressPathNotFound() {
    return suppressPathNotFound;
  }

  public void setSuppressPathNotFound(Boolean suppressPathNotFound) {
    this.suppressPathNotFound = suppressPathNotFound;
  }

  private boolean suppressPathNotFound(){
    return BooleanUtils.toBooleanDefaultIfNull(getSuppressPathNotFound(), false);
  }

  /**
   * Set the xpath-alike path to the element on which you want to split.
   * <p>
   * Note that this is only a pseudo-xpath evaluator as it only allows simple element traversal and not any XPath functions.
   * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
   * {@link XMLEventReader} and navigates based on {@link StartElement} events only.
   * </p>
   *
   * @param path the path.
   */
  public void setPath(String path) {
    this.path = Args.notBlank(path, "path");
  }

  public JsonStreamingConfigBuilder getJsonStreamingConfig() {
    return jsonStreamingConfig;
  }

  public void setJsonStreamingConfig(JsonStreamingConfigBuilder jsonStreamingConfig) {
    this.jsonStreamingConfig = Args.notNull(jsonStreamingConfig, "jsonStreamingConfig");
  }

  private class JsonStreamingSplitGeneratorConfig extends StaxSplitGeneratorConfig {
    AdaptrisMessage originalMessage;
    JsonXMLConfig jsonXMLConfig;
    XMLEventFactory xmlEventFactory;
    boolean wrapWithArray;

    JsonStreamingSplitGeneratorConfig withOriginalMessage(AdaptrisMessage msg) {
      originalMessage = msg;
      return this;
    }
    JsonStreamingSplitGeneratorConfig withJsonXMLConfig(JsonXMLConfig jsonXMLConfig) {
      this.jsonXMLConfig = jsonXMLConfig;
      return this;
    }
    JsonStreamingSplitGeneratorConfig withXMLEventFactory(XMLEventFactory xmlEventFactory) {
      this.xmlEventFactory = xmlEventFactory;
      return this;
    }

    JsonStreamingSplitGeneratorConfig withWrapWithArray(boolean wrapWithArray) {
      this.wrapWithArray = wrapWithArray;
      return this;
    }

  }
  private class JsonStreamingSplitGenerator extends StaxSplitGenerator<JsonStreamingSplitGeneratorConfig, AdaptrisMessage> {
    private transient AdaptrisMessageFactory factory;

    public JsonStreamingSplitGenerator(JsonStreamingSplitGeneratorConfig cfg) throws Exception {
      super(cfg);
    }

    @Override
    public void init(JsonStreamingSplitGeneratorConfig cfg){
      factory = selectFactory(cfg.originalMessage);
    }

    @Override
    public AdaptrisMessage generateNextMessage(XMLEvent event, String elementName) throws Exception {
      if (event == null) {
        return null;
      }
      AdaptrisMessage splitMsg = factory.newMessage();
      try (OutputStream output = splitMsg.getOutputStream()){
        XMLEventWriter writer = new JsonXMLOutputFactory(getConfig().jsonXMLConfig).createXMLEventWriter(output);
        writer.add(getConfig().xmlEventFactory.createStartDocument());
        if(getConfig().wrapWithArray) {
          writer.add(getConfig().xmlEventFactory.createProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, event.asStartElement().getName().getLocalPart()));
        }
        while (isNotEndElement(event, elementName) && getConfig().getXmlEventReader().hasNext()){
          writer.add(event);
          event = getConfig().getXmlEventReader().nextEvent();
        }
        writer.add(getConfig().xmlEventFactory.createEndDocument());
      }
      copyMetadata(getConfig().originalMessage, splitMsg);
      return splitMsg;
    }
  }
}
