/**
 * @file AuthRouter.java
 * @module modules/auth
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth;

/**
 * ! my imports
 */
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.middleware.AuthMiddleware;
import com.spendi.core.middleware.BodyValidationMiddleware;
import com.spendi.core.middleware.JsonBodyParserMiddleware;
import com.spendi.core.router.ApiRouter;
import com.spendi.modules.auth.dto.LoginDto;
import com.spendi.modules.auth.dto.RegisterDto;

public class AuthRouter extends ApiRouter {
	public AuthController controller = AuthController.getInstance();

	public AuthRouter(String apiPrefix) {
		super(AuthRouter.class.getSimpleName(), "/auth", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		this.post("/register", controller::register, new JsonBodyParserMiddleware(),
				BodyValidationMiddleware.of(RegisterDto.class));

		this.post("/login", controller::login, new JsonBodyParserMiddleware(),
				BodyValidationMiddleware.of(LoginDto.class));

		this.post("/logout", controller::logout, AuthMiddleware.getInstance());
	}
}
