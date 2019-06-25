package com.adaptris.core.json.jsonpatch;

import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Mirrors {@code com.flipkart.zjsonpatch.CompatibilityFlags} for configuration purposes.
 * <p>
 * Since {@code com.flipkart.zjsonpatch.CompatibilityFlags} doesn't have a XStreamAlias, it renders
 * poorly for configuration; this enum is simply here to make things look nice.
 * </p>
 * 
 * @config json-patch-apply-flag
 */
@XStreamAlias("json-patch-apply-flag")
public enum PatchApplyFlag {
  MISSING_VALUES_AS_NULLS(CompatibilityFlags.MISSING_VALUES_AS_NULLS), 
  REMOVE_NONE_EXISTING_ARRAY_ELEMENT(
      CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT);
  
  private CompatibilityFlags actual;

  private PatchApplyFlag(CompatibilityFlags df) {
    this.actual = df;
  }
  
  /**
   * Get the real value.
   * 
   * @return the real {@code com.flipkart.zjsonpatch.CompatibilityFlags} value.
   */
  public CompatibilityFlags actualValue() {
    return actual;
  }
}
