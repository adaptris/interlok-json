package com.adaptris.core.json.aggregator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.util.Args;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@ComponentProfile(summary = "Merge each message into an existing JSON object.", tag = "json")
@NoArgsConstructor
public class JsonMergeAggregator extends JsonAggregatorImpl {
  protected transient ObjectMapper mapper = new ObjectMapper();
  /**
   * Specify the metadata key that contains the 'key' against which the message will be merged into
   * the original.
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String mergeMetadataKey;

  @Override
  public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> messages)
      throws CoreException {
    JsonNode rootNode;
    try {
      rootNode = mapper.readTree(original.getContent());
    } catch (IOException e) {
      throw new CoreException("Failed to merge into original payload as it is not valid json", e);
    }

    try (Writer w = new BufferedWriter(original.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      for (AdaptrisMessage msg : messages) {
        if (filter(msg)) {
          try {
            final String mergeKeyName =
                Args.notBlank(msg.getMetadataValue(getMergeMetadataKey()), "merge-key-name");
            mergeChildEntryIntoParent(rootNode, mergeKeyName, msg);
            overwriteMetadata(msg, original);
          } catch (Exception ce) {
            log.error("Failed to merge the following message: [{}] into parent payload",
                msg.getUniqueId());
          }
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

}
