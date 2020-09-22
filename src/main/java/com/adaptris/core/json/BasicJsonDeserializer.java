package com.adaptris.core.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Implementation based using the {@code org.json:json} library.
 * <p>
 * Note that this will coerce something like {@code { "key": string}} (note the failure to quote the
 * string into something will pass schema validation but isn't valid JSON.
 * </p>
 *
 * @config basic-json-deserializer
 */
@XStreamAlias("basic-json-deserializer")
@ComponentProfile(summary = "Convert an AdaptrisMessage using 'org.json:json'")
@NoArgsConstructor
public class BasicJsonDeserializer implements JsonDeserializer {

  @Override
  public Object deserialize(String input) throws JSONException {
    try {
      return new JSONObject(new JSONTokener(input));
    } catch (final JSONException e) {
      return new JSONArray(new JSONTokener(input));
    }
  }

}
