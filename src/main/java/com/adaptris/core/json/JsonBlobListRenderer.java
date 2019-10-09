package com.adaptris.core.json;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.types.InterlokMessage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Render a list of {@link RemoteBlob} as a JSON Array.
 * 
 * @config remote-blob-list-as-json
 */
@XStreamAlias("remote-blob-list-as-json")
@ComponentProfile(summary = "Render a list of RemoteBlob objects as a JSON Array", since = "3.9.2", tag = "cloud,aws,jclouds,blob")
public class JsonBlobListRenderer implements BlobListRenderer {

  @Override
  public void render(Collection<RemoteBlob> list, InterlokMessage msg) throws InterlokException {
    try (Writer w = new BufferedWriter(msg.getWriter());
        JsonGenerator generator = new ObjectMapper().getFactory().createGenerator(w)) {
      generator.writeObject(toListOfMap(list));
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
 }

  private List<Map<String, Object>> toListOfMap(Collection<RemoteBlob> list) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (RemoteBlob blob : list) {
      Map<String, Object> obj = new HashMap<>();
      obj.put("bucket", blob.getBucket());
      obj.put("lastModified", blob.getLastModified());
      obj.put("name", blob.getName());
      obj.put("size", blob.getSize());
      result.add(obj);
    }
    return result;
  }
}
