/**
 * @file AuthService.java
 * @module modules/auth
 * @description
 * Сервис аутентификации пользователей.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth;

/**
 * ! java imports
 */
import java.util.Map;

import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.config.AuthConfig;
import com.spendi.core.base.service.BaseService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.exceptions.ValidationException;
import com.spendi.modules.auth.dto.LoginDto;
import com.spendi.modules.auth.dto.RegisterDto;
import com.spendi.modules.user.UserService;
import com.spendi.modules.user.cmd.UserCreateCmd;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.session.SessionService;

/**
 * Сервис аутентификации пользователей.
 */
public class AuthService extends BaseService {

	protected static AuthService INSTANCE = new AuthService();
	private final UserService userService = UserService.getInstance();
	private final SessionService sessionService = SessionService.getInstance();
	protected final AuthConfig authCfg = AuthConfig.getConfig();
	protected final AuthMapper authMapper = AuthMapper.getInstance();

	protected AuthService() {
		super(AuthService.class.getSimpleName());
	}

	public static AuthService getInstance() {
		return INSTANCE;
	}

	/**
	 * Регистрация пользователя
	 * 
	 * @param requestId идентификатор запроса
	 * @param dto       данные для регистрации
	 */
	public ServiceResponse<UserEntity> register(String requestId, RegisterDto dto) {
		// маппим dto в command
		UserCreateCmd command = authMapper.toCmd(dto);

		// создаем пользователя
		var created = this.userService.create(requestId, command);

		this.info("register success", requestId,
				detailsOf("email", created.getData().getEmail(), "userId", created.getData().getId().toHexString()));

		return created;
	}

	/**
	 * Авторизация пользователя
	 * 
	 * @param requestId идентификатор запроса
	 * @param dto       данные для авторизации
	 */
	public ServiceResponse<UserEntity> getUserAndCheckPassword(String requestId, LoginDto dto) {
		// Поиск пользователя по почте (выбрасывает исключение если не найден)
		UserEntity user = this.userService.getByEmail(requestId, dto.getEmail()).getData();

		// Проверка пароля
		boolean ok = user.comparePassword(dto.getPassword());

		if (!ok) {
			// Лог: неверные учётные данные (сохраняем)
			this.warn("login failed: invalid credentials", requestId, detailsOf("email", dto.getEmail()), true);
			throw new ValidationException("Invalid credentials", Map.of("password", "invalid password"), Map.of());
		}

		this.info("user found and password verified", requestId, detailsOf("email", dto.getEmail()), true);

		return ServiceResponse.founded(user);
	}

	/**
	 * Выход пользователя из системы
	 * 
	 * @param requestId идентификатор запроса
	 * @param sessionId идентификатор сессии
	 */
	public void logout(String requestId, ObjectId sessionId) {
		try {
			this.sessionService.revokeById(requestId, sessionId);
			// Лог: успешный logout
			this.info("logout success", requestId, detailsOf("sessionId", sessionId), true);
		} catch (RuntimeException ignore) {
		}
	}
}
