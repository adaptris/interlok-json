package com.adaptris.core.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.util.CloseableIterable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Render a list of {@link RemoteBlob} as a JSON Array, one object per
 * line.
 *
 * @config remote-blob-list-as-json-lines
 */
@XStreamAlias("remote-blob-list-as-json-lines")
@ComponentProfile(summary = "Render a list of RemoteBlob objects as a JSON Array, one line per object", since = "3.9.2", tag = "cloud,aws,jclouds,blob,lines")
public class JsonBlobListRendererLines extends JsonBlobListRenderer {

  private static final String ROOT_SEPARATOR = ",\n";

  @Override
  public void render(Iterable<RemoteBlob> blobs, InterlokMessage msg) throws InterlokException {
    try (CloseableIterable<RemoteBlob> list = CloseableIterable.ensureCloseable(blobs);
        Writer w = new BufferedWriter(msg.getWriter());
        JsonGenerator generator = new ObjectMapper().getFactory().createGenerator(w)) {

      List<Map<String, Object>> jsonArray = new ArrayList<>();

      for (RemoteBlob blob : list) {
        Map<String, Object> obj = new HashMap<>();
        mapInsert(obj, "bucket", blob.getBucket());
        mapInsert(obj, "lastModified", blob.getLastModified());
        mapInsert(obj, "name", blob.getName());
        mapInsert(obj, "size", blob.getSize());
        jsonArray.add(obj);
      }

      if (jsonArray.size() > 1) {
        generator.setPrettyPrinter(new MinimalPrettyPrinter(ROOT_SEPARATOR));
        w.write('[');
      }
      for (Map<String, Object> jsonObject : jsonArray) {
        generator.writeObject(jsonObject);
      }
      if (jsonArray.size() > 1) {
        w.write(']');
      }

    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
 }
}
