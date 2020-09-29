package com.adaptris.core.transform.json;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Default Transformation Driver that uses both {@link JSONObject#fromObject(Object)} and {@link JSONArray#fromObject(Object)} to
 * parse the input.
 * <p>
 * <strong>In some processing scenarios, this driver is inherently CPU bound because of how it processes XML elements<strong>. As a
 * result it is no longer the suggested default. In most use-cases you can use {@link SimpleJsonTransformationDriver} instead. If
 * you require the specific features of this driver, then use {@link JsonlibTransformationDriver} instead.
 * </p>
 * <p>
 * This uses the {@code net.sf.json} package, which in some situations, can be very sensitive to whitespace, and output may not be
 * as you expect. Accordingly, when you are generating the XML to be rendered as JSON make sure that you use
 * {@code xsl:strip-space elements="*"}.
 * </p>
 * 
 * @config default-transformation-driver
 * @deprecated since 3.10.0, the name was changed to {@code jsonlib-transformation-driver} since its performance is not predictable
 *             enough for it to be the default.
 * @author gdries
 */
@XStreamAlias("default-transformation-driver")
@ComponentProfile(summary = "JSON/XML Transformation driver, supports top level JSON arrays", tag = "json,xml,transformation")
@Deprecated
@Removal(version = "3.12.0",
    message = "since 3.10.0, the name was changed to jsonlib-transformation-driver since its performance is not predictable enough for it to be the default.")
@DisplayOrder(order= {"rootName", "arrayName", "elementName", "objectName"})
public class DefaultJsonTransformationDriver extends JsonlibTransformationDriver {

  private static transient boolean warningLogged = false;

  public DefaultJsonTransformationDriver() {
    LoggingHelper.logDeprecation(warningLogged, () -> {
      warningLogged = true;
    }, this.getClass().getCanonicalName(), JsonlibTransformationDriver.class.getCanonicalName());
  }
}
