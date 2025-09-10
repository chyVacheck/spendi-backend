/**
 * @file BaseController.java
 * @module core/base
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

public abstract class BaseController extends BaseClass {

	public BaseController(String className) {
		super(EClassType.CONTROLLER, className);
	}
}
