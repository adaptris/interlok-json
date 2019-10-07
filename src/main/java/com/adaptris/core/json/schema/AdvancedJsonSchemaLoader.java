package com.adaptris.core.json.schema;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.BooleanUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * {@link JsonSchemaLoader} implementation that allows for more settings.
 * 
 * <p>
 * If you have a schema defined such that supporting files for your schema are only available on the
 * classpath, rather than as a {@code file://} or {@code http://} URL. :
 * 
 * <pre>
 * {@code
    {
      "type": "array",
      "items": {
          "$ref": "classpath://com/adaptris/core/json/schema/test_schema.json"
      }
     }
 * }
 * </pre>
 * 
 * then you need to enable the classpath aware functionality of the schema builder; this is disabled
 * by default with the expectation that generally either all schemas as self-contained, or they are
 * available explicitly as a standard URL resource.
 * 
 * @author mcwarman
 *
 */
@XStreamAlias("advanced-json-schema-loader")
@DisplayOrder(order = {"classPathAwareClient"})
public class AdvancedJsonSchemaLoader implements JsonSchemaLoader {


  @InputFieldDefault(value = "false")
  private Boolean classPathAwareClient;

  private String resolutionScope;

  @Override
  public Schema loadSchema(JSONObject rawSchema) {
    SchemaLoader.SchemaLoaderBuilder builder = SchemaLoader.builder()
        .schemaJson(rawSchema);
    if(classPathAwareClient()) {
      builder.schemaClient(SchemaClient.classPathAwareClient());
    }
    if(!isEmpty(getResolutionScope())){
      String rs;
      if(getResolutionScope().startsWith("file")) {
        rs = getResolutionScope().replace("\\", "/");
      } else {
        rs = getResolutionScope();
      }
      builder.resolutionScope(rs);
    }
    SchemaLoader schemaLoader = builder.build();
    return schemaLoader.load().build();
  }

  /**
   * Toggle the classpath aware functionality of the schema builder.
   * @param classPathAwareClient control classpath aware functionality
   */
  public void setClassPathAwareClient(Boolean classPathAwareClient) {
    this.classPathAwareClient = classPathAwareClient;
  }

  /**
   * Returns whether or not to enable the classpath aware functionality of the schema builder.
   * @return whether or not to enable the classpath aware functionality
   */
  public Boolean getClassPathAwareClient() {
    return classPathAwareClient;
  }

  private boolean classPathAwareClient() {
    return BooleanUtils.toBooleanDefaultIfNull(getClassPathAwareClient(), false);
  }

  AdvancedJsonSchemaLoader withClassPathAwareClient(Boolean classPathAwareClient) {
    setClassPathAwareClient(classPathAwareClient);
    return this;
  }

  /**
   * Sets the initial resolution scope of the schema.
   * @param resolutionScope the initial (absolute) URI, used as the resolution scope.
   */
  public void setResolutionScope(String resolutionScope) {
    this.resolutionScope = Args.notEmpty(resolutionScope, "resolutionScope");
  }

  /**
   * The initial resolution scope of the schema.
   * @return The (absolute) URI, used as the resolution scope.
   */
  public String getResolutionScope() {
    return resolutionScope;
  }

  AdvancedJsonSchemaLoader withResolutionScope(String resolutionScope) {
    setResolutionScope(resolutionScope);
    return this;
  }
}
