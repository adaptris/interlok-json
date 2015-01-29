package com.adaptris.core.services.splitter.json;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;

public class JsonArraySplitterTest extends BaseCase {

  public JsonArraySplitterTest(String name) {
    super(name);
  }

  private static final String JSON_ARRAY = "[{colour: \"red\",value: \"#f00\"},{colour: \"green\",value: \"#0f0\"},{colour: \"blue\",value: \"#00f\"},{colour: \"black\",value: \"#000\"}]";
  private static final String JSON_OBJ = "{\"colours\" : [{colour: \"red\",value: \"#f00\"},{colour: \"green\",value: \"#0f0\"},{colour: \"blue\",value: \"#00f\"},{colour: \"black\",value: \"#000\"}] }";

  public void testSplitArray() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    assertEquals(4, s.splitMessage(src).size());
  }

  public void testSplitNotArray() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_OBJ);
    assertEquals(1, s.splitMessage(src).size());
  }

  public void testSplitNotJson() throws Exception {
    JsonArraySplitter s = new JsonArraySplitter();
    AdaptrisMessage src = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    try {
      List<AdaptrisMessage> msgs = s.splitMessage(src);
      fail();
    }
    catch (CoreException expected) {

    }
  }
}
