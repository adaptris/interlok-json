package com.adaptris.core.services.splitter.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message splitter implementation that splits a JSON array so that each element forms a new message.
 * 
 * <p>
 * If the message cannot be parsed as JSON then an exception will be thrown; If the message is a JSON object but not a JSON array,
 * then the original message is returned. Note that because it operates on the entire payload, size of message considerations may be
 * in order.
 * </p>
 * <p>
 * For instance the JSON array <code>
[{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}]</code>
 * would be split into 4 messages whereas
 * <code>{"colours" : [{colour: "red",value: "#f00"},{colour: "green",value: "#0f0"},{colour: "blue",value: "#00f"},{colour: "black",value: "#000"}] }</code>
 * would remain a single message.
 * 
 * @config json-array-splitter
 * @author lchan
 * 
 */
@XStreamAlias("json-array-splitter")
public class JsonArraySplitter extends MessageSplitterImp {

  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<>();
    try {
      String original = msg.getStringPayload();
      JSON jsonRoot = JSONSerializer.toJSON(original);
      if (jsonRoot.isArray()) {
        JSONArray array = (JSONArray) jsonRoot;
        for (Iterator i = array.iterator(); i.hasNext();) {
          result.add(createSplitMessage((JSONObject) i.next(), msg));
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


  private AdaptrisMessage createSplitMessage(JSONObject src, AdaptrisMessage original) throws IOException {
    AdaptrisMessageFactory factory = selectFactory(original);
    AdaptrisMessage dest = factory.newMessage();
    try (StringReader in = new StringReader(src.toString()); Writer out = dest.getWriter()) {
      IOUtils.copy(in, out);
      copyMetadata(original, dest);
    }
    return dest;
  }
}
