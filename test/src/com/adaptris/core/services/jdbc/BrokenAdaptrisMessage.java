package com.adaptris.core.services.jdbc;

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.util.GuidGenerator;

public class BrokenAdaptrisMessage extends DefaultAdaptrisMessageImp {

  public BrokenAdaptrisMessage() throws RuntimeException {
    super(new GuidGenerator(), AdaptrisMessageFactory.getDefaultInstance());
    setPayload(new byte[0]);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new ErroringOutputStream();
  }

  private class ErroringOutputStream extends OutputStream {

    protected ErroringOutputStream() {
      super();
    }

    @Override
    public void write(int b) throws IOException {
      throw new IOException("Failed to write");
    }
  }
}
