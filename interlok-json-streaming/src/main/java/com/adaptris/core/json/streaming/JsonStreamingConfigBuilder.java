package com.adaptris.core.json.streaming;

import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wraps {@code de.odysseus.staxon.json.JsonXMLConfigBuilder} for marshalling purposes.
 *
 * @config json-streaming-config
 */
@XStreamAlias("json-streaming-config")
@DisplayOrder(order =
{
    "prettyPrint", "autoArray", "autoPrimitive", "namespaceDeclarations", "virtualRoot", "multipleProcessingInstruction",
    "namespaceMappings", "namespaceSeparator", "repairingNamespaces"
})
@NoArgsConstructor
public class JsonStreamingConfigBuilder {

  /**
   * Trigger json arrays automatically; default is false if not specified
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  private Boolean autoArray;
  /**
   * whether to convert element text to number/boolean/null primitives automatically; default is
   * false if not specified.
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  private Boolean autoPrimitive;
  /**
   * emit "&lt;?xml-multiple ?>" in the XML; default true if not specified.
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  private Boolean multipleProcessingInstruction;
  /**
   * whether to write namespace declarations; default is true if not specified.
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "true")
  private Boolean namespaceDeclarations;
  /**
   *
   * namespace separator; default is ':' if not specified.
   */
  @Getter
  @Setter
  @InputFieldDefault(value = ":")
  @AdvancedConfig
  private Character namespaceSeparator;
  /**
   * pretty print or not; default is false if not specified.
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean prettyPrint;
  /**
   * Whether or not to repair namespaces when writing; default is false if not specified.
   */
  @InputFieldDefault(value = "false")
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean repairingNamespaces;

  /**
   * Set the virtual root
   * <p>
   * JSON documents may have have multiple root properties. However, XML requires a single root
   * element. This property specifies the root as a "virtual" element, which will be removed from
   * the stream when writing and added to the stream when reading.
   * </p>
   *
   */
  @InputFieldDefault(value = "null")
  @Getter
  @Setter
  private String virtualRoot;
  /**
   * Any specific namespace mappings.
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private KeyValuePairSet namespaceMappings;

  public JsonXMLConfig build() {
    return new JsonXMLConfigBuilder().autoArray(autoArray()).autoPrimitive(autoPrimitive()).multiplePI(multiplePI())
        .namespaceDeclarations(namespaceDeclarations()).namespaceSeparator(namespaceSeparator()).prettyPrint(prettyPrint())
        .repairingNamespaces(repairingNamespaces()).virtualRoot(virtualRoot()).namespaceMappings(namespaceMappings()).build();
  }

  public JsonStreamingConfigBuilder withAutoArray(Boolean b) {
    setAutoArray(b);
    return this;
  }

  public boolean autoArray() {
    return BooleanUtils.toBooleanDefaultIfNull(getAutoArray(), JsonXMLConfig.DEFAULT.isAutoArray());
  }

  public JsonStreamingConfigBuilder withAutoPrimitive(Boolean b) {
    setAutoPrimitive(b);
    return this;
  }

  public boolean autoPrimitive() {
    return BooleanUtils.toBooleanDefaultIfNull(getAutoPrimitive(), JsonXMLConfig.DEFAULT.isAutoPrimitive());
  }

  public JsonStreamingConfigBuilder withNamespaceDeclarations(Boolean b) {
    setNamespaceDeclarations(b);
    return this;
  }

  public boolean namespaceDeclarations() {
    return BooleanUtils.toBooleanDefaultIfNull(getNamespaceDeclarations(), JsonXMLConfig.DEFAULT.isNamespaceDeclarations());
  }


  public JsonStreamingConfigBuilder withNamespaceSeparator(Character b) {
    setNamespaceSeparator(b);
    return this;
  }

  public char namespaceSeparator() {
    return getNamespaceSeparator() != null ? getNamespaceSeparator().charValue() : JsonXMLConfig.DEFAULT.getNamespaceSeparator();
  }

  public JsonStreamingConfigBuilder withPrettyPrint(Boolean b) {
    setPrettyPrint(b);
    return this;
  }

  public boolean prettyPrint() {
    return BooleanUtils.toBooleanDefaultIfNull(getPrettyPrint(), JsonXMLConfig.DEFAULT.isPrettyPrint());
  }


  public JsonStreamingConfigBuilder withVirtualRoot(String b) {
    setVirtualRoot(b);
    return this;
  }

  public QName virtualRoot() {
    return getVirtualRoot() != null ? QName.valueOf(getVirtualRoot()) : JsonXMLConfig.DEFAULT.getVirtualRoot();
  }


  public JsonStreamingConfigBuilder withRepairingNamespaces(Boolean b) {
    setRepairingNamespaces(b);
    return this;
  }

  public boolean repairingNamespaces() {
    return BooleanUtils.toBooleanDefaultIfNull(getRepairingNamespaces(), JsonXMLConfig.DEFAULT.isRepairingNamespaces());
  }

  public JsonStreamingConfigBuilder withNamespaceMappings(KeyValuePairSet b) {
    setNamespaceMappings(b);
    return this;
  }

  public Map<String, String> namespaceMappings() {
    if (null == getNamespaceMappings()) {
      return JsonXMLConfig.DEFAULT.getNamespaceMappings();
    }
    return KeyValuePairBag.asMap(getNamespaceMappings());
  }

  public JsonStreamingConfigBuilder withMultipleProcessingInstruction(Boolean b) {
    setMultipleProcessingInstruction(b);
    return this;
  }

  public boolean multiplePI() {
    return BooleanUtils.toBooleanDefaultIfNull(getMultipleProcessingInstruction(), JsonXMLConfig.DEFAULT.isMultiplePI());
  }
}
