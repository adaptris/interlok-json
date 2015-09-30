package com.adaptris.core.services.path;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.config.PayloadDataDestination;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.license.License;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * This service allows you to search JSON content and the results are then set back into the message.
 * </p>
 * <p>
 * The searching works in much the same way as XPath, for more information on how to build a JSON path see here;
 * {@link https://github.com/jayway/JsonPath}
 * </p>
 * <p>
 * By configuring the "source-destination" and "target-destination" ({@link Destination}) you can specify where the JSON content is sourced from and where the results of the search should be set.
 * </p>
 * <p>
 * You may configure multiple target destinations.  This allows you to essentially perform multiple searches, rather than configuring multiple instances of this service.
 * </br>
 * For example, if you have a message with the following payload;
 * <pre>
 * {@code
 * {
 *   "store": {
 *       "book": [
 *           {
 *               "category": "reference",
 *               "author": "Nigel Rees",
 *               "title": "Sayings of the Century",
 *               "price": 8.95
 *           },
 *           {
 *               "category": "fiction",
 *               "author": "Evelyn Waugh",
 *               "title": "Sword of Honour",
 *               "price": 12.99
 *           },
 *           {
 *               "category": "fiction",
 *               "author": "Herman Melville",
 *               "title": "Moby Dick",
 *               "isbn": "0-553-21311-3",
 *               "price": 8.99
 *           },
 *           {
 *               "category": "fiction",
 *               "author": "J. R. R. Tolkien",
 *               "title": "The Lord of the Rings",
 *               "isbn": "0-395-19395-8",
 *               "price": 22.99
 *           }
 *       ],
 *       "bicycle": {
 *           "color": "red",
 *           "price": 19.95
 *       }
 *   },
 *   "expensive": 10
 * }
 * }
 * </pre>
 * 
 * You could configure 2 target destinations, each one creating a new metadata item with the results of the specified search, like this;
 * 
 * <pre>
 * {@code
 *  <target-destination class="json-metadata-destination">
 *    <configured-json-path class="constant-json-path">
 *      <json-path>$.store.book[0].title</json-path>
 *    </configured-json-path>
 *    <key>metadata-key-1</key>
 *  </target-destination>
 *  <target-destination class="json-metadata-destination">
 *    <configured-json-path class="constant-json-path">
 *      <json-path>$.store.book[1].title</json-path>
 *    </configured-json-path>
 *    <key>metadata-key-2</key>
 *  </target-destination>
 *  }
 * </pre>
 * 
 * The first target above searches for the first book title, the second target searches for the second book title.
 * Each target-destination will be executed in the order they are configured and therefore with the two targets shown here, your message, after the
 * service has run, will include two new metadata items;
 * 
 * <ul>
 *  <li>metadata-key-1 = "Sayings of the Century"</li>
 *  <li>metadata-key-2 = "Sword of Honour"</li>
 * </ul>
 * </p>
 * <p>
 * If you do configure multiple target destinations, you may need to be a little careful, realizing that you may be modifying the source destination as well.</br>
 * For example if you were to specify the payload as the source, then configure 2 targets, the first one a payload destination and the second one a metadata destination,
 * the first target will take the payload, perform the search and set the result back into the payload.  When the second target is destination is run, the source destination,
 * the payload, has now been changed.
 * </p>
 * 
 * @author amcgrath
 * @config json-path-service
 * @license BASIC
 */

@XStreamAlias("json-path-service")
public class JsonPathService extends ServiceImp {
    
  private DataDestination sourceDestination;
  
  @XStreamImplicit(itemFieldName="json-path-execution")
  private List<Execution> executions;
  
  public JsonPathService() {
    sourceDestination = new PayloadDataDestination();
    executions = new ArrayList<>();
  }
  
  static {
    Configuration.setDefaults(new Configuration.Defaults() {
        private final JsonProvider jsonProvider = new JacksonJsonProvider();
        private final MappingProvider mappingProvider = new JacksonMappingProvider();
        private final Set<Option> options = EnumSet.noneOf(Option.class);

        public JsonProvider jsonProvider() {
            return jsonProvider;
        }

        @Override
        public MappingProvider mappingProvider() {
            return mappingProvider;
        }

        @Override
        public Set<Option> options() {
            return options;
        }
    });
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      DocumentContext parsedJsonContent = JsonPath.parse(this.getSourceDestination().getData(message).toString());
      
      for(Execution execution : this.getExecutions()) {
        execution.getTargetDataDestination().setData(message, 
            parsedJsonContent.read((String) 
                execution.getSourceJsonPathExpression().getData(message)).toString());
      }
    } catch (InterlokException ex) {
      throw new ServiceException(ex);
    }
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void close() {    
  }

  @Override
  public void init() throws CoreException {    
  }

  public DataDestination getSourceDestination() {
    return sourceDestination;
  }

  public void setSourceDestination(DataDestination sourceDestination) {
    this.sourceDestination = sourceDestination;
  }

  public List<Execution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<Execution> executions) {
    this.executions = executions;
  }
}
