package com.adaptris.core.json.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * {@link MessageAggregator} implementation that merges each message to a JSON object or array.
 *
 * <p>
 * The payloads from the collection are assumed to be JSON objects, and will be aggregated into the pre-split message be it an object or array.
 * Messages that are not JSON objects will be ignored but an error will be thrown should the pre-split message not be valid.
 * </p>
 *
 * @config json-merge-aggregator
 */
@XStreamAlias("json-merge-aggregator")
public class JsonMergeAggregator extends MessageAggregatorImpl {
  protected transient ObjectMapper mapper = new ObjectMapper();
  protected String mergeMetadataKey;

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
    JsonNode rootNode;
    try {
      rootNode = mapper.readTree(original.getContent());
    } catch (IOException e) {
      throw new CoreException("Failed to merge into original payload as it is not valid json", e);
    }

    try (Writer w = new BufferedWriter(original.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      for (AdaptrisMessage msg : filter(messages)) {
        final String mergeKeyName = msg.getMetadataValue(mergeMetadataKey);
        try {
          mergeChildEntryIntoParent(rootNode, mergeKeyName, msg);
          overwriteMetadata(msg, original);
        } catch (CoreException ce) {
          // log error and continue with the remaining msgs
          log.error("Failed to merge the following message: [{}] into parent payload", mergeKeyName);
        }
      }
      generator.writeTree(rootNode);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private void mergeChildEntryIntoParent(JsonNode parentNode, String keyname, AdaptrisMessage childMessage) throws CoreException {
    JsonNode childNode = null;
    try {
      childNode = mapper.readTree(childMessage.getContent());
    } catch (IOException e) {
      throw new CoreException("Spilt message is not a valid JSON document");
    }
    if (parentNode.isObject()) {
      // We might be overwriting something here, we could potentially add a flag to control this
      ((ObjectNode)parentNode).set(keyname, childNode);
    }
    else if (parentNode.isArray()) {
      ((ArrayNode)parentNode).add(childNode);
    }
    else {
      throw new CoreException("Unable to merge into unknown json type");
    }
  }

  public String getMergeMetadataKey() {
    return mergeMetadataKey;
  }

  public void setMergeMetadataKey(String mergeMetadataKey) {
    this.mergeMetadataKey = mergeMetadataKey;
  }

}
