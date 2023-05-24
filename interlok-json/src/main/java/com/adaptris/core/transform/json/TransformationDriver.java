package com.adaptris.core.transform.json;

import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Transformation driver.
 */
public interface TransformationDriver {
	/**
   * Perform transformation.
   *
   * @param input The data to transform.
   * @param direction The direction of the transformation.
   * @return The transformed data.
   * @throws ServiceException Thrown if there is a problem with the transformation.
   * @deprecated since 3.11.0 use the {@link #transform(AdaptrisMessage, TransformationDirection)}
   *             instead.
   * @implNote The default implementation throws an {@link UnsupportedOperationException}.
   */
  @Deprecated
  @Removal(version = "4.0.0")
  default String transform(String input, TransformationDirection direction)
      throws ServiceException {
    throw new UnsupportedOperationException("Use transform(AdaptrisMessage, direction) instead");
  }

  /**
   * Perform the transformation.
   *
   * @param msg The data to transform.
   * @param direction The direction of the transformation.
   * @throws ServiceException Thrown if there is a problem with the transformation.
   * @implNote The default implementation just delegates to
   *           {@link #transform(String, TransformationDirection)}.
   *
   */
  default void transform(AdaptrisMessage msg, TransformationDirection direction)
      throws ServiceException {
    msg.setContent(transform(msg.getContent(), direction), msg.getContentEncoding());
  }
}
