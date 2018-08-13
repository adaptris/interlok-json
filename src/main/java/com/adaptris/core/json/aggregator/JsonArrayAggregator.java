/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.json.aggregator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageAggregator} implementation that adds each message to a JSON array.
 * 
 * <p>
 * The pre-split message is always ignored; the payloads from the collection are assumed to be JSON objects, and will be aggregated
 * together as a single JSON array. Messages that are not JSON objects will be ignored (JSON Arrays will also be ignored).
 * </p>
 * 
 * @config json-array-aggregator
 * @since 3.6.5
 */
@XStreamAlias("json-array-aggregator")
public class JsonArrayAggregator extends MessageAggregatorImpl {
  private transient ObjectMapper mapper = new ObjectMapper();
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public JsonArrayAggregator() {

  }

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
    try (Writer w = new BufferedWriter(original.getWriter()); JsonGenerator generator = mapper.getFactory().createGenerator(w)) {
      generator.writeStartArray();
      for (AdaptrisMessage msg : messages) {
        write(msg, generator);
        overwriteMetadata(msg, original);
      }
      generator.writeEndArray();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  private void write(final AdaptrisMessage msg, JsonGenerator generator) throws IOException {
    try (BufferedReader buf = new BufferedReader(msg.getReader())) {
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        generator.writeTree(mapper.readTree(parser));
      } else {
        log.trace("Ignoring [{}], not JSON object", msg.getUniqueId());
      }
    } catch (JsonParseException e) {
      // ignore it.
    }
    return;
  }

}
