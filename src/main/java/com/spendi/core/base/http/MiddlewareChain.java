
/**
 * @file MiddlewareChain.java
 * @module core/base/http
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

/**
 * Цепочка middleware. Вызов {@link #next()} передаёт управление дальше по
 * цепочке.
 */
public interface MiddlewareChain {
	void next() throws Exception;
}