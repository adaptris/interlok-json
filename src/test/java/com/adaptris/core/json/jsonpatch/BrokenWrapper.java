package com.adaptris.core.json.jsonpatch;

import java.io.InputStream;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.InterlokMessage.MessageWrapper;

public class BrokenWrapper implements MessageWrapper<InputStream> {

  @Override
  public InputStream wrap(InterlokMessage arg0) throws Exception {
    throw new Exception();
  }

}
