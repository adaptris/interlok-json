package com.adaptris.core.services.path.json;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.json.JsonPathExecution;
import com.adaptris.interlok.config.DataInputParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is the base for JSON path services, which allows you to search
 * JSON content with the results then being set back into the message.
 * <p>
 * The searching works in much the same way as XPath, for more
 * information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a>
 * documentation.
 * </p>
 * See individual implementations for further documentation.
 */
@NoArgsConstructor
public abstract class JsonPathServiceImpl extends ServiceImp {

  /**
   * The list of jsonpath executions to apply.
   *
   */
  @NotNull
  @Valid
  @AutoPopulated
  @XStreamImplicit
  @Getter
  @Setter
  @NonNull
  protected List<Execution> executions = new ArrayList<>();

  /**
   * Get whether the JSON should be unwrapped removing any leading or trailing square brackets
   * {@code []}.
   * <p>
   * The default is false if not specified.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  protected Boolean unwrapJson;

  protected static String toString(Object json, Execution exec) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Object jsonObject = convertIfNull(json, exec);
    // A JSON Object is effectively a map, so we need to write that out as JSON.
    // If it's a JSONArray, then "toString" works fine.
    if (Map.class.isAssignableFrom(jsonObject.getClass())) {
      return mapper.writeValueAsString(jsonObject);
    }
    if (List.class.isAssignableFrom(jsonObject.getClass())) {
      return mapper.writeValueAsString(jsonObject);
    }
    return jsonObject.toString();
  }

  /**
   * Strip (if necessary) the leading/trailing [] from the JSON.
   *
   * @param json
   *        The JSON string.
   */
  protected static String unwrap(final String json, boolean unwrapJson) {
    if (unwrapJson) {
      if (json.startsWith("[") && json.endsWith("]")) {
        return json.substring(1, json.length() - 1);
      }
    }
    return json;
  }

  @Override
  protected void closeService() {
    /* unused/empty method */
  }

  @Override
  protected void initService() throws CoreException {
    // nothing to do.
  }

  protected boolean unwrapJson() {
    return BooleanUtils.toBooleanDefaultIfNull(getUnwrapJson(), false);
  }

  protected boolean suppressPathNotFound(Execution exec) {
    if (exec instanceof JsonPathExecution) {
      return ((JsonPathExecution) exec).suppressPathNotFound();
    }
    return false;
  }

  protected static Object convertIfNull(Object o, Execution exec) {
    if (exec instanceof JsonPathExecution) {
      return ((JsonPathExecution) exec).nullConverter().convert(o);
    }
    return o;
  }

}
