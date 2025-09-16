/**
 * @file AuthController.java
 * @module modules/auth
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.config.AuthConfig;
import com.spendi.core.base.BaseController;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.response.ApiSuccessResponse;
import com.spendi.modules.auth.dto.LoginDto;
import com.spendi.modules.user.UserEntity;
import com.spendi.modules.user.UserService;
import com.spendi.modules.user.dto.UserCreateDto;
import com.spendi.modules.session.SessionEntity;
import com.spendi.modules.session.SessionService;
import com.spendi.modules.session.dto.CreateSessionDto;
import com.spendi.core.utils.CookieUtils;

public class AuthController extends BaseController {

	protected static AuthController INSTANCE = new AuthController();
	private final UserService userService = UserService.getInstance();
	private final SessionService sessionService = SessionService.getInstance();
	private final AuthService authService = AuthService.getInstance();
	protected final AuthConfig authCfg = AuthConfig.getConfig();

	protected AuthController() {
		super(AuthController.class.getSimpleName());
	}

	public static AuthController getInstance() {
		return INSTANCE;
	}

	/**
	 * Регистрация пользователя
	 * 
	 * @param ctx HttpContext контекст запроса
	 */
	public void register(HttpContext ctx) {
		UserCreateDto dto = ctx.getValidBody(UserCreateDto.class);

		// Лог: запрос регистрации (несохраненный)
		this.info("register requested", ctx.getRequestId(), detailsOf("email", dto.getProfile().getEmail()));

		var res = this.authService.register(ctx.getRequestId(), dto);

		ctx.res().success(
				ApiSuccessResponse.created(ctx.getRequestId(), "User registered", res.getData().getPublicData()));
	}

	/**
	 * Авторизация пользователя
	 * 
	 * @param ctx HttpContext контекст запроса
	 */
	public void login(HttpContext ctx) {
		LoginDto dto = ctx.getValidBody(LoginDto.class);

		String maskedPassword = "*".repeat(dto.getPassword().length());

		// Лог: запрос логина (несохраненный)
		this.info("login requested", ctx.getRequestId(),
				detailsOf("email", dto.getEmail(), "password", maskedPassword));

		// Поиск пользователя по почте и проверка пароля (выбрасывает исключение если не найден)
		UserEntity user = this.authService.getUserAndCheckPassword(ctx.getRequestId(), dto).getData();

		// Single active session policy: отзываем предыдущие активные сессии пользователя
		this.sessionService.revokeActiveByUser(ctx.getRequestId(), user.id.toHexString());

		// Создание новой сессии
		String ip = ctx.req().remoteAddress().orElse(null);
		String ua = ctx.req().header("User-Agent").orElse("");

		CreateSessionDto createSessionDto = new CreateSessionDto();
		createSessionDto.setUserId(user.id.toHexString());
		createSessionDto.setIp(ip);
		createSessionDto.setUserAgent(ua);

		var s = this.sessionService.create(ctx.getRequestId(), createSessionDto).getData();

		// Set cookie
		String cookie = CookieUtils.buildCookie(authCfg.getCookieName(), s.id.toHexString(), authCfg.getSessionTtlSec(),
				authCfg);
		ctx.res().header("Set-Cookie", cookie);

		// Лог: успешный логин
		this.info("login success", ctx.getRequestId(),
				detailsOf("userId", user.id.toHexString(), "sessionId", s.id.toHexString()), true);

		this.userService.touchLastLogin(ctx.getRequestId(), user.id.toHexString());

		ctx.res().success(
				ApiSuccessResponse.ok(ctx.getRequestId(), "User " + user.getEmail() + " logged", user.getPublicData()));
	}

	/**
	 * Выход пользователя из системы
	 * 
	 * @param ctx HttpContext контекст запроса
	 */
	public void logout(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		// Лог: запрос на logout (несохраненный)
		this.info("logout requested", ctx.getRequestId(), detailsOf("sessionId", s.id.toHexString()));

		this.authService.logout(ctx.getRequestId(), s.id.toHexString());

		// очистка cookie
		String cleared = CookieUtils.buildCookie(authCfg.getCookieName(), "", 0, authCfg);
		ctx.res().header("Set-Cookie", cleared);
		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "logout ok", Map.of()));
	}
}
