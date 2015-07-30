package com.adaptris.core.transform.json;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default Transformation Driver.
 * 
 * @config default-transformation-driver
 * @author gdries
 * @deprecated since 3.0.4 use {@link JsonObjectTransformationDriver} or
 *             {@link JsonArrayTransformationDriver} instead.
 */
@XStreamAlias("default-transformation-driver")
@Deprecated
public class DefaultJsonTransformationDriver extends JsonObjectTransformationDriver {

}
