package com.spendi.core.base;

import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.Middleware;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.types.EClassType;

public abstract class BaseMiddleware extends BaseClass implements Middleware {

	public BaseMiddleware(String className) {
		super(EClassType.MIDDLEWARE, className);
	}

	@Override
	abstract public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception;

}
