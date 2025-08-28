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
import com.spendi.core.middleware.RequestLifecycleMiddleware;
import com.spendi.core.router.PingRouter;

public class App {
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();
		System.out.println("✅ Loaded config: " + config);

		var server = new JavalinServerAdapter();

		// глобальные middleware
		server.use(new RequestLifecycleMiddleware()); // или без параметра

		// вызываешь get/post/... и всё
		server.registerRouter(new PingRouter());

		server.start(config.getPort());
		System.out.println("✅ Server started on http://localhost:" + config.getPort());
	}
}
