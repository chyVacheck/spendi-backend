/**
 * @file BaseMapper.java
 * @module core/base
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

public abstract class BaseMapper extends BaseClass {

	public BaseMapper(String className) {
		super(EClassType.MAPPER, className);
	}
}
