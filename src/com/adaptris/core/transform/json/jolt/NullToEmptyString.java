package com.adaptris.core.transform.json.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.Transform;

/**
 * {@link Transform} implementation that turns all nulls into the empty string {@code ""}.
 * <p>
 * You can use this by specifying the fully qualified class name as the {@code operation} in your jolt specification
 * </p>
 * <pre>
 * {@code [{"operation": ...skipped for brevity }, { "operation": "com.adaptris.core.transform.json.jolt.NullToEmptyString"}]}
 * </pre>
 * 
 */
public class NullToEmptyString implements Transform {
  @Override
  public Object transform(Object input) {
    return convertNullToEmpty(input);
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T convertNullToEmpty(T input) {
    if(input instanceof Map<?, ?>) {
      // Json Object
      return (T) convertNullToEmpty((Map<?, ?>) input);
    } else
    if(input instanceof List<?>) {
      // Json Array
      return (T) convertNullToEmpty((List<?>) input);
    } else {
      // Literal Value
      if (input == null) {
        return (T) "";
      } else {
        return input;
      }
    }
  }
  
  private static <T> List<T> convertNullToEmpty(List<T> input) {
    List<T> result = new ArrayList<>(input.size());
    for(T item: input) {
      result.add(convertNullToEmpty(item));
    }
    return result;
  }
  
  private static <K, V> Map<K, V> convertNullToEmpty(Map<K, V> input) {
    Map<K, V> result = new HashMap<>(input.size());
    for(K key: input.keySet()) {
      result.put(key, convertNullToEmpty(input.get(key)));
    }
    return result;
  }

}
