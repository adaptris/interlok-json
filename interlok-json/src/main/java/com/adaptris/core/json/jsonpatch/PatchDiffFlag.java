package com.adaptris.core.json.jsonpatch;

import com.flipkart.zjsonpatch.DiffFlags;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Mirrors {@code com.flipkart.zjsonpatch.DiffFlags} for configuration purposes.
 * <p>
 * Since {@code com.flipkart.zjsonpatch.DiffFlags} doesn't have a XStreamAlias, it renders poorly
 * for configuration; this enum is simply here to make things look nice.
 * </p>
 * 
 * @config json-patch-diff-flag
 */
@XStreamAlias("json-patch-diff-flag")
public enum PatchDiffFlag {
  OMIT_VALUE_ON_REMOVE(DiffFlags.OMIT_VALUE_ON_REMOVE), 
  OMIT_MOVE_OPERATION(DiffFlags.OMIT_MOVE_OPERATION),
  OMIT_COPY_OPERATION(DiffFlags.OMIT_COPY_OPERATION),
  ADD_ORIGINAL_VALUE_ON_REPLACE(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE),
  EMIT_TEST_OPERATIONS(DiffFlags.EMIT_TEST_OPERATIONS);
  
  private DiffFlags actual;

  private PatchDiffFlag(DiffFlags df) {
    this.actual = df;
  }
  
  /**
   * Get the real value.
   * 
   * @return the real {@code com.flipkart.zjsonpatch.DiffFlags} value.
   */
  public DiffFlags actualValue() {
    return actual;
  }
}
