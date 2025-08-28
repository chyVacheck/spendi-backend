
/**
 * @file Middleware.java
 * @module core/base/http
 *
 * @see HttpMethod
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

/**
 * Функциональный интерфейс middleware: (ctx, chain) -> chain.next()
 */
@FunctionalInterface
public interface Middleware {
	void handle(HttpContext ctx, MiddlewareChain chain) throws Exception;
}