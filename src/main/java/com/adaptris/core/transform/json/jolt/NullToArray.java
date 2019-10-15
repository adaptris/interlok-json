package com.adaptris.core.transform.json.jolt;

import com.bazaarvoice.jolt.Transform;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;

/**
 * {@link Transform} implementation returns an empty array when the object is null.
 *
 * <p>This exists because it is not easily achievable using {@code "operation": "default"}.</p>
 */
public class NullToArray implements Transform {

  @Override
  public Object transform(Object input) {
    return ObjectUtils.defaultIfNull(input, new ArrayList<>());
  }
}
