package com.adaptris.core.services.splitter.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Split an arbitrarily large JSON array into smaller batches.
 * 
 * <p>
 * This is a extension to {@link LargeJsonArraySplitter}; each resulting message is <strong>still a JSON array</strong>, it just
 * contains slightly fewer elements than the original message.
 * </p>
 * <p>
 * For instance given the following JSON Array; <pre>
 * {@code
[
{"colour": "red","value": "#f00"},
{"colour": "green","value": "#0f0"},
{"colour": "blue","value": "#00f"},
{"colour": "black","value": "#000"}
]
   }
 * </pre> with a batchSize of {@code 2} then we would expect to split messages; each a JSON array containing two elements. e.g. :
 * {@code [{ "colour": "red", "value": "#f00" }, { "colour": "green", "value": "#0f0"}] } and {@code [{ "colour": "blue","value":
 * "#00f"}, { "colour": "black", "value": "#000"}] }
 * 
 * @config batched-json-array-splitter
 */
@XStreamAlias("batched-json-array-splitter")
public class BatchedJsonArraySplitter extends LargeJsonArraySplitter {
  private static final int DEFAULT_BATCH_SIZE = 10;

  @InputFieldDefault(value = "10")
  private Integer batchSize;

  public BatchedJsonArraySplitter() {

  }

  public BatchedJsonArraySplitter withBufferSize(Integer i) {
    setBufferSize(i);
    return this;
  }

  public BatchedJsonArraySplitter withMessageFactory(AdaptrisMessageFactory fac) {
    setMessageFactory(fac);
    return this;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  /**
   * Set the batch size.
   * 
   * @param batchSize the size, default is 10 if not specified.
   */
  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  int batchSize() {
    int size = getBatchSize() != null ? getBatchSize().intValue() : DEFAULT_BATCH_SIZE;
    return size < 1 ? DEFAULT_BATCH_SIZE : size;
  }

  public BatchedJsonArraySplitter withBatchSize(Integer i) {
    setBatchSize(i);
    return this;
  }

  @Override
  protected JsonSplitGenerator createSplitter(GeneratorConfig cfg) {
    logR.trace("Batches of {} elements", batchSize());
    return new MyJsonSplitGenerator(cfg);
  }

  private class MyJsonSplitGenerator extends JsonSplitGenerator {

    public MyJsonSplitGenerator(GeneratorConfig cfg) {
      super(cfg);
    }

    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      AdaptrisMessage tmpMessage = newMessage();
      int currentBatch = 0;
      try (Writer w = new BufferedWriter(tmpMessage.getWriter());
          JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
        generator.writeStartArray();
        while (currentBatch < batchSize()) {
          JsonToken next = parser.nextToken();
          if (next == JsonToken.START_OBJECT) {
            currentBatch++;
            generator.writeTree(mapper.readTree(parser));
          } else if (next == JsonToken.END_ARRAY) {
            // it's tne end of the array, so theres nothing else.
            break;
          } else if (next == null) {
            // We're past tne end
            break;
          }
        }
        generator.writeEndArray();
      }
      if (currentBatch > 0) {
        return addMetadata(tmpMessage);
      }
      return null;
    }
  }

}
