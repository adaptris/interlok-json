package com.adaptris.core.transform.json;

import com.adaptris.core.ServiceException;

/**
 * Transformation driver.
 */
public interface TransformationDriver {
	/**
	 * Perform transformation.
	 *
	 * @param input
	 *          The data to transform.
	 * @param direction
	 *          The direction of the transformation.
	 *
	 * @return The transformed data.
	 *
	 * @throws ServiceException
	 *           Thrown if there is a problem with the transformation.
	 */
	public String transform(String input, TransformationDirection direction) throws ServiceException;
}