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
import com.spendi.core.exceptions.ValidationException;
import com.spendi.core.utils.CryptoUtils;
import com.spendi.modules.auth.dto.LoginDto;
import com.spendi.modules.user.UserEntity;
import com.spendi.modules.user.UserService;
import com.spendi.modules.user.dto.UserCreateDto;
import com.spendi.modules.session.SessionEntity;
import com.spendi.modules.session.SessionService;
import com.spendi.core.utils.CookieUtils;

public class AuthController extends BaseController {

	protected static AuthController INSTANCE = new AuthController();
	protected final UserService userService = UserService.getInstance();
	protected final SessionService sessionService = SessionService.getInstance();
	protected final AuthConfig authCfg = new AuthConfig();

	protected AuthController() {
		super(AuthController.class.getSimpleName());
	}

    public static AuthController getInstance() {
        return INSTANCE;
    }

    public void register(HttpContext ctx) {
        LoginDto dto = ctx.getValidBody(LoginDto.class);

        // Лог: запрос регистрации (несохраненный)
        this.info("register requested", ctx.getRequestId(), detailsOf("email", dto.email));

        // Сборка минимального UserCreateDto из email/password
        var createDto = new UserCreateDto();
        var profile = new UserCreateDto.ProfileBlock();
        profile.email = dto.email;
        createDto.profile = profile;
        var sec = new UserCreateDto.SecurityBlock();
        sec.password = dto.password;
        createDto.security = sec;

        var created = userService.create(ctx.getRequestId(), createDto).getData();

        ctx.res().success(ApiSuccessResponse.created(
                ctx.getRequestId(),
                "User registered",
                created.getPublicData()));
    }

	public void login(HttpContext ctx) {
		LoginDto dto = ctx.getValidBody(LoginDto.class);

		// Лог: запрос логина (несохраненный)
		this.info("login requested", ctx.getRequestId(), detailsOf("email", dto.email));

        // Поиск пользователя по почте (выбрасывает исключение если не найден)
        UserEntity user = userService.getByEmail(dto.email).getData();

		// Verify password
        boolean ok = CryptoUtils.verifyPassword(dto.password, user.security.passwordHash);
		if (!ok) {
			// Лог: неверные учётные данные (сохраняем)
			this.warn("login failed: invalid credentials", ctx.getRequestId(),
					detailsOf("email", dto.email), true);
			throw new ValidationException("Invalid credentials", Map.of("password", "invalid password"), Map.of());
		}

		// Single active session policy: отзываем предыдущие активные сессии
		// сотрудника
		sessionService.revokeActiveByUser(ctx.getRequestId(), user.id.toHexString());

		// Создание новой сессии
		String ip = ctx.req().remoteAddress().orElse(null);
		String ua = ctx.req().header("User-Agent").orElse("");
		var s = sessionService
				.create(ctx.getRequestId(), user.id.toHexString(), ip, ua)
				.getData();

		// Set cookie
		String cookie = CookieUtils.buildCookie(authCfg.getCookieName(), s.id.toHexString(), authCfg.getSessionTtlSec(),
				authCfg);
		ctx.res().header("Set-Cookie", cookie);

		// Лог: успешный логин
        this.info("login success", ctx.getRequestId(),
                detailsOf("userId", user.id.toHexString(), "sessionId", s.id.toHexString()), true);

        ctx.res().success(ApiSuccessResponse.ok(
                ctx.getRequestId(),
                "User " + user.getEmail() + " logged",
                user.getPublicData()));
    }

	public void logout(HttpContext ctx) {
		SessionEntity session = ctx.getAuthSession();

		// Лог: запрос на logout (несохраненный)
		this.info("logout requested", ctx.getRequestId(), detailsOf(
				"sessionId", session.id.toHexString()));

		try {
			sessionService.revokeById(ctx.getRequestId(), session.id.toHexString());
			// Лог: успешный logout
			this.info("logout success", ctx.getRequestId(), detailsOf("sessionId", session.id.toHexString()), true);
		} catch (RuntimeException ignore) {
		}

		// очистка cookie
		String cleared = CookieUtils.buildCookie(authCfg.getCookieName(), "", 0, authCfg);
		ctx.res().header("Set-Cookie", cleared);
		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "logout ok", Map.of()));
	}
}
