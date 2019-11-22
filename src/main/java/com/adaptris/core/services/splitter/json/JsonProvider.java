package com.adaptris.core.services.splitter.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.splitter.LineCountSplitter;
import com.adaptris.core.util.CloseableIterable;

/**
 * Allows switching between JSON arrays and JSON lines when attempting to split a payload.
 * <p>
 * This is generally used by services that want to operate on individual JSON objects, and want to be able to switch between JSON
 * arrays and JSON lines.
 * </p>
 */
public interface JsonProvider {

  enum JsonStyle implements JsonObjectProvider {
    JSON_ARRAY {
      @Override
      public CloseableIterable<AdaptrisMessage> createIterator(AdaptrisMessage t) throws Exception {
        LargeJsonArraySplitter splitter =
            new LargeJsonArraySplitter().withMessageFactory(AdaptrisMessageFactory.getDefaultInstance());
        return splitter.splitMessage(t);
      }
    },

    // JSON_LINES is basically a line count splitter, where a JSON object is prsent on each line...
    // see jsonlines.org
    JSON_LINES {
      @Override
      public CloseableIterable<AdaptrisMessage> createIterator(AdaptrisMessage t) throws Exception {
        LineCountSplitter splitter = new LineCountSplitter(1);
        splitter.setIgnoreBlankLines(true);
        splitter.setMessageFactory(AdaptrisMessageFactory.getDefaultInstance());
        return splitter.splitMessage(t);
      }
    }
  }

  // Could have been Function<AdaptrisMessage, CloseableIterable<AdaptrisMessage>> but that means we can't
  // throw exceptions, or we have to wrap things as RTE, or we @SneakyThrows it...
  public interface JsonObjectProvider {
    CloseableIterable<AdaptrisMessage> createIterator(AdaptrisMessage t) throws Exception;
  }
}
