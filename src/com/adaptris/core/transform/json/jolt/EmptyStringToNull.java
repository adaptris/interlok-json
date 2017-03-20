package com.adaptris.core.transform.json.jolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bazaarvoice.jolt.Transform;

/**
 * {@link Transform} implementation that turns all empty strings (@code ""} into null.
 * <p>
 * You can use this by specifying the fully qualified class name as the {@code operation} in your jolt specification
 * </p>
 * <pre>
 * {@code [{"operation": ...skipped for brevity }, { "operation": "com.adaptris.core.transform.json.jolt.EmptyStringToNull"}]}
 * </pre>
 * 
 */
public class EmptyStringToNull implements Transform {


  @Override
  public Object transform(Object input) {
    return convertEmptyToNull(input);
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T convertEmptyToNull(T input) {
    if(input instanceof Map<?, ?>) {
      // Json Object
      return (T) convertEmptyToNull((Map<?, ?>) input);
    } else
    if(input instanceof List<?>) {
      // Json Array
      return (T) convertEmptyToNull((List<?>) input);
    } else {
      // Literal Value
      if ("".equals(input)) {
        return null;
      } else {
        return input;
      }
    }
  }
  
  private static <T> List<T> convertEmptyToNull(List<T> input) {
    List<T> result = new ArrayList<>(input.size());
    for(T item: input) {
      result.add(convertEmptyToNull(item));
    }
    return result;
  }
  
  private static <K, V> Map<K, V> convertEmptyToNull(Map<K, V> input) {
    Map<K, V> result = new HashMap<>(input.size());
    for(K key: input.keySet()) {
      result.put(key, convertEmptyToNull(input.get(key)));
    }
    return result;
  }
  
}
