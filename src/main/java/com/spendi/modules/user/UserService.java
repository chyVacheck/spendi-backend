
/**
 * @file UserService.java
 * @module modules/user
 *
 * @see BaseRepositoryService
 *
 * Сервис для работы с UserEntity.
 * Предоставляет CRUD-операции
 *
 * @author Dmytro Shakh
*/

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.exceptions.EntityAlreadyExistsException;
import com.spendi.core.exceptions.EntityNotFoundException;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.utils.CryptoUtils;

import com.spendi.modules.files.FileService;
import com.spendi.modules.files.FileEntity;
import com.spendi.modules.user.dto.UserCreateDto;

public class UserService extends BaseRepositoryService<UserRepository, UserEntity> {

	private static volatile UserService INSTANCE;
	private final FileService fileService = FileService.getInstance();

	public UserService(UserRepository repository) {
		super(UserService.class.getSimpleName(), repository);
	}

	/**
	 * Инициализация с явно переданным репозиторием (из AppInitializer).
	 */
	public static void init(UserRepository repository) {
		synchronized (UserRepository.class) {
			if (INSTANCE == null) {
				INSTANCE = new UserService(repository);
			}
		}
	}

	/**
	 * Доступ к инстансу после предварительного init(...).
	 */
	public static UserService getInstance() {
		UserService ref = INSTANCE;
		if (ref == null) {
			throw new IllegalStateException(
					"UserService not initialized. Call AppInitializer.initAll() in App.main");
		}
		return ref;
	}

	// ===============================
	// ===== Специфичные методы ======
	// ===============================

	/**
	 * Создать пользователя из DTO. Проверяет уникальность profile.email.
	 */
	public ServiceResponse<UserEntity> create(String requestId, UserCreateDto dto) {
		// Уникальность по email
		if (repository.existsByEmail(dto.profile.email)) {
			throw new EntityAlreadyExistsException(
					UserEntity.class.getSimpleName(),
					Map.of("profile.email", dto.profile.email));
		}

		var now = Instant.now();

		var entity = new UserEntity();
		entity.id = new ObjectId();

		// profile
		var profile = new UserEntity.Profile();
		profile.email = dto.profile.email.toLowerCase();
		profile.firstName = dto.profile.firstName;
		profile.lastName = dto.profile.lastName;
		profile.avatarFileId = null;
		entity.profile = profile;

		// security
		var sec = new UserEntity.Security();
		sec.passwordHash = CryptoUtils.hashPassword(dto.security.password);
		entity.security = sec;

		// finance
		var fin = new UserEntity.Finance();
		fin.defaultAccountId = null;
		fin.accountsCount = 0;
		fin.paymentMethodIds = List.of();
		entity.finance = fin;

		// system
		var sys = new UserEntity.System();
		var meta = new UserEntity.Meta();
		meta.createdAt = now;
		meta.updatedAt = now;
		meta.lastLoginAt = null;
		sys.meta = meta;
		entity.system = sys;

		var created = super.createOne(entity);
		this.info(
				"user created",
				requestId,
				detailsOf("id", entity.id.toHexString(), "email", profile.email),
				true);
		return created;
	}

	/** Найти по email. */
	public ServiceResponse<UserEntity> getByEmail(String email) {
		return this.getOne("profile.email", email.toLowerCase());
	}

	/**
	 * Обновить системный статус (Available/Suspended/...).
	 */
	// оставляем смену пароля и touch lastLogin

	/**
	 * Выставить/снять флаг администратора.
	 */
	// удалён admin/status функционал как нерелевантный

	/**
	 * Сменить пароль (принимает новый в открытом виде, хэширует).
	 */
	public ServiceResponse<UserEntity> changePassword(String requestId, String userId, String newPassword) {
		var updates = Map.<String, Object>of(
				"security.passwordHash", CryptoUtils.hashPassword(newPassword),
				"system.meta.updatedAt", Instant.now());
		var res = this.updateById(userId, updates);
		this.info("user password changed", requestId, detailsOf("userId", userId), true);
		return res;
	}

	/**
	 * Проставить lastLoginAt = now (можно вызывать после успешного логина).
	 */
	public ServiceResponse<UserEntity> touchLastLogin(String requestId, String userId) {
		Instant now = Instant.now();

		var res = this.updateById(userId, Map.<String, Object>of(
				"system.meta.lastLoginAt", now));
		this.info("user lastLoginAt updated", requestId,
				detailsOf("userId", userId, "at", now.toString()));
		return res;
	}

	/**
	 * Загрузить/заменить аватар сотруднику: сохраняет файл, обновляет
	 * account.avatarFileId у сотрудника, логирует создание/замену
	 * и удаляет старый файл (best-effort). Возвращает публичный URL
	 * аватарки ("/employees/{id}/avatar")
	 */
	public ServiceResponse<String> uploadAvatar(String requestId, String userId, UploadedFile uf) {
		// ensure exists and remember previous avatar id
		UserEntity user = this.getById(userId).getData();
		String oldAvatarId = user.getAvatarFileIdOptional().orElse(null);

		// store new file
		FileEntity stored = this.fileService.uploadOne(requestId, uf).getData();

		// build public url
		String url = user.getAvatarUrl();

		// update employee (persist only new file id)
		Instant now = Instant.now();
		var updates = Map.<String, Object>of(
				"profile.avatarFileId", stored.id.toHexString(),
				"system.meta.updatedAt", now);
		this.updateById(userId, updates);

		// Лог: создан или обновлён аватар
		if (oldAvatarId != null) {
			this.info("user avatar updated", requestId,
					detailsOf("userId", userId, "oldFileId", oldAvatarId,
							"newFileId", stored.id.toHexString()),
					true);
			// best-effort cleanup of previous avatar
			try {
				this.fileService.deleteById(requestId, oldAvatarId);
			} catch (RuntimeException ignore) {
			}

			return ServiceResponse.updated(url);
		} else {
			this.info("user avatar created", requestId,
					detailsOf("userId", userId, "fileId", stored.id.toHexString()),
					true);
			return ServiceResponse.created(url);
		}
	}

	/**
	 * Полное удаление аватарки: очищает ссылку у сотрудника и удаляет файл.
	 * Логирует операцию удаления (персистентно).
	 */
	public ServiceResponse<String> deleteAvatar(String requestId, String userId) {
		UserEntity user = this.getById(userId).getData();

		String avatarId = user.getAvatarFileIdOptional().orElseThrow(() -> new EntityNotFoundException(
				"UserAvatar", "userId", userId));

		var updates = new HashMap<String, Object>();
		updates.put("profile.avatarFileId", null);
		updates.put("system.meta.updatedAt", Instant.now());

		// Clear reference in employee
		this.updateById(userId, updates);

		// Удаление файла (метаданные + физический файл)
		this.fileService.deleteById(requestId, avatarId);

		// Лог удаления
		this.info("user avatar deleted", requestId,
				detailsOf("userId", userId, "fileId", avatarId),
				true);

		return ServiceResponse.deleted(user.getAvatarUrl());
	}
}
