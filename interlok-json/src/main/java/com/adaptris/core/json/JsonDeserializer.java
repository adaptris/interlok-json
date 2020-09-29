package com.adaptris.core.json;

import com.adaptris.core.AdaptrisMessage;

/**
 * Allows us to plugin various implementations that handle JSON
 * <p>
 * While possible to directly configure, the behavioural semantics will largely depend on the
 * wrapping component that uses it.
 * </p>
 *
 */
@FunctionalInterface
public interface JsonDeserializer<T> {

  T deserialize(String m) throws Exception;

  default T deserialize(AdaptrisMessage m) throws Exception {
    return deserialize(m.getContent());
  }
}
