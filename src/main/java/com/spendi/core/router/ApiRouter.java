
/**
 * @file ApiRouter.java
 * @module core/router
 *
 * @description
 * Основной роутер для первой версии API с маршрутом api/v1.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.router;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRouter;

public abstract class ApiRouter extends BaseRouter {

	public ApiRouter(String className, String basePath, String apiPrefix) {
		super(className, normalizeBase(join(apiPrefix, basePath)));
	}
}
