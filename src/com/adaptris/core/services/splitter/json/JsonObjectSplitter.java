package com.adaptris.core.services.splitter.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Message splitter implementation that splits a JSON object so each entry forms a new message.
 * 
 * <p>
 * If the message cannot be parsed as JSON then an exception will be thrown; If the message is an empty JSON object then the
 * original message is returned. Note that because it operates on the entire payload, size of message considerations may be in
 * order.
 * </p>
 * <p>
 * For instance the JSON Object <code>
{"entry":[{"location":"Seattle","name":"Production System"},{"location":"New York","name":"R&D sandbox"}],"notes":"Some Notes","version":0.5}</code>
 * would be split into 3 messages (the {@code entry}, {@code notes} and {@code version}). JSON arrays will be split so that each
 * element of the array becomes a separate message, so <code>
[{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}]</code>
 * would be split into 4 messages.
 * </p>
 * 
 * @config json-object-splitter
 * @author lchan
 * 
 */
@XStreamAlias("json-object-splitter")
public class JsonObjectSplitter extends MessageSplitterImp {

  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<>();
    try {
      String original = msg.getContent();
      JSON jsonRoot = JSONSerializer.toJSON(original);
      if (!jsonRoot.isEmpty()) {
        if (jsonRoot.isArray()) {
          result.addAll(splitMessage((JSONArray) jsonRoot, msg));
        }
        else {
          JSONObject obj = (JSONObject) jsonRoot;
          for (Iterator i = obj.keys(); i.hasNext();) {
            String key = (String) i.next();
            JSONObject o = new JSONObject();
            o.put(key, obj.get(key));
            result.add(createSplitMessage(o, msg));
          }
        }
      }
      else {
        result.add(msg);
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }


  List<AdaptrisMessage> splitMessage(JSONArray array, AdaptrisMessage original) throws IOException {
    List<AdaptrisMessage> result = new ArrayList<>();
    for (Iterator i = array.iterator(); i.hasNext();) {
      result.add(createSplitMessage((JSONObject) i.next(), original));
    }
    return result;
  }

  AdaptrisMessage createSplitMessage(JSONObject src, AdaptrisMessage original) throws IOException {
    AdaptrisMessageFactory factory = selectFactory(original);
    AdaptrisMessage dest = factory.newMessage();
    try (StringReader in = new StringReader(src.toString()); Writer out = dest.getWriter()) {
      IOUtils.copy(in, out);
      copyMetadata(original, dest);
    }
    return dest;
  }

}
