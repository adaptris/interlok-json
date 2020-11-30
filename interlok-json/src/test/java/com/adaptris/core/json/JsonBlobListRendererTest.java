package com.adaptris.core.json;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonBlobListRendererTest {

  @Test
  public void testRender() throws Exception {
    JsonBlobListRenderer render = new JsonBlobListRenderer();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<RemoteBlob> blobs = createBlobs(10);
    render.render(blobs, msg);
    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    System.err.println(msg.getContent());
    ReadContext context = JsonPath.parse(msg.getContent(), jsonConfig);
    assertEquals(Integer.valueOf(10), context.read("$.length()"));
    assertEquals("bucket", context.read("$[0].bucket"));
  }

  @Test(expected = CoreException.class)
  public void testRender_Fail() throws Exception {
    JsonBlobListRenderer render = new JsonBlobListRenderer();
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.OUTPUT).newMessage();
    Collection<RemoteBlob> blobs = createBlobs(10);
    render.render(blobs, msg);
  }

  private static Collection<RemoteBlob> createBlobs(int count) {
    List<RemoteBlob> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      result.add(new RemoteBlob.Builder().setBucket("bucket").setLastModified(new Date().getTime()).setName("File_" + i)
          .setSize(10L).build());
    }
    return result;
  }
}
