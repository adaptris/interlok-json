package com.adaptris.core.json.aggregator;

import java.util.Collection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;

public abstract class JsonAggregatorImpl extends MessageAggregatorImpl {

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages)
      throws CoreException {
    aggregate(original, messages);
  }

  @Override
  public abstract void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
      throws CoreException;
}
