package com.adaptris.core.json;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.util.CloseableIterable;
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
  public void render(Iterable<RemoteBlob> blobs, InterlokMessage msg) throws InterlokException {
    try (CloseableIterable<RemoteBlob> list = CloseableIterable.ensureCloseable(blobs);
        Writer w = new BufferedWriter(msg.getWriter());
        JsonGenerator generator = new ObjectMapper().getFactory().createGenerator(w)) {
      generator.writeStartArray();
      for (RemoteBlob blob : list) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("bucket", blob.getBucket());
        obj.put("lastModified", blob.getLastModified());
        obj.put("name", blob.getName());
        obj.put("size", blob.getSize());
        generator.writeObject(obj);
      }
      generator.writeEndArray();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
 }

}
