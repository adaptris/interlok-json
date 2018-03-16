package com.adaptris.core.services.splitter.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.CloseableIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Split an arbitrarily large JSON array.
 * 
 * <p>
 * Note: tested with an 85Mb file containing an array of >15k JSON objects
 * </p>
 * 
 * @config large-json-array-splitter
 */
@XStreamAlias("large-json-array-splitter")
public class LargeJsonArraySplitter extends MessageSplitterImp {
  private transient static final int DEFAULT_BUFFER_SIZE = 8192;

  @AdvancedConfig
  private Integer bufferSize;

  public LargeJsonArraySplitter() {

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
    this.bufferSize = b;
  }

  int bufferSize() {
    return getBufferSize() != null ? getBufferSize().intValue() : DEFAULT_BUFFER_SIZE;
  }

  public LargeJsonArraySplitter withBufferSize(Integer i) {
    setBufferSize(i);
    return this;
  }

  public LargeJsonArraySplitter withMessageFactory(AdaptrisMessageFactory fac) {
    setMessageFactory(fac);
    return this;
  }


  @Override
  public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
    try {
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if(parser.nextToken() != JsonToken.START_ARRAY) {
        throw new CoreException("Expected an array");
      }
      return createSplitter(new GeneratorConfig().withJsonParser(parser).withObjectMapper(mapper).withOriginalMessage(msg));
    } catch (IOException e) {
      throw new CoreException(e);
    }
  }

  protected JsonSplitGenerator createSplitter(GeneratorConfig cfg) {
    return new JsonSplitGenerator(cfg);
  }

  protected class GeneratorConfig {
    ObjectMapper mapper;
    JsonParser parser;
    AdaptrisMessage originalMessage;

    GeneratorConfig withJsonParser(JsonParser p) {
      parser = p;
      return this;
    }

    GeneratorConfig withObjectMapper(ObjectMapper m) {
      mapper = m;
      return this;
    }

    GeneratorConfig withOriginalMessage(AdaptrisMessage msg) {
      originalMessage = msg;
      return this;
    }

  }

  protected class JsonSplitGenerator implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    protected JsonParser parser;
    protected transient ObjectMapper mapper;

    private AdaptrisMessageFactory factory;
    private transient AdaptrisMessage originalMsg;
    private transient AdaptrisMessage nextMessage;

    protected JsonSplitGenerator(GeneratorConfig cfg) {
      this.mapper = cfg.mapper;
      this.parser = cfg.parser;
      this.originalMsg = cfg.originalMessage;
      this.factory = selectFactory(originalMsg);
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = constructAdaptrisMessage();
        } catch (IOException e) {
          throw new RuntimeException("Could not construct next AdaptrisMessage", e);
        }
      }
      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    @SuppressWarnings("deprecation")
    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      AdaptrisMessage tmpMessage = newMessage();
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = mapper.readTree(parser);
        tmpMessage.setStringPayload(node.toString());
        return addMetadata(tmpMessage);
      }
      return null;
    }

    protected AdaptrisMessage newMessage() {
      return factory.newMessage();
    }

    protected AdaptrisMessage addMetadata(AdaptrisMessage splitMsg) {
      copyMetadata(originalMsg, splitMsg);
      return splitMsg;
    }

    @Override
    public void close() throws IOException {
      parser.close();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  };


}
