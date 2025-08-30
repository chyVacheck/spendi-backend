/**
 * @file App.java
 * @module com.spendi
 *
 * @author Dmytro Shakh
 */

package com.spendi;

/**
 * ! my imports
 */
import com.spendi.config.ServerConfig;
import com.spendi.core.base.server.JavalinServerAdapter;
import com.spendi.core.base.server.MyExceptionMapper;
import com.spendi.core.middleware.RequestLifecycleMiddleware;
import com.spendi.core.router.PingRouter;

public class App {
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();
		System.out.println("✅ Loaded config: " + config);

		var server = new JavalinServerAdapter();

		server.setExceptionMapper(new MyExceptionMapper());

		// глобальные middleware
		server.useAfter(new RequestLifecycleMiddleware());

		// вызываешь get/post/... и всё
		server.registerRouter(new PingRouter());

		server.start(config.getPort());
	}
}
