/**
 * @file App.java
 * @module com.spendi
 *
 * @author Dmytro Shakh
 */

package com.spendi;

/**
 * ! java imports
 */
import java.util.Locale;

/**
 * ! my imports
 */
import com.spendi.config.ServerConfig;
import com.spendi.core.base.server.JavalinServerAdapter;
import com.spendi.core.base.server.MyExceptionMapper;
import com.spendi.core.middleware.RequestLifecycleMiddleware;
import com.spendi.core.router.NotFoundRouter;
import com.spendi.core.router.PingRouter;

public class App {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		ServerConfig config = new ServerConfig();
		System.out.println("✅ Loaded config: " + config);

		var server = new JavalinServerAdapter();

		server.setExceptionMapper(new MyExceptionMapper());

		// глобальные middleware
		server.useAfter(new RequestLifecycleMiddleware());

		// вызываешь get/post/... и всё
		server.registerRouter(new PingRouter());

		// обязательно ПОСЛЕДНИМ
		server.registerRouter(new NotFoundRouter());

		server.start(config.getPort());
	}
}
