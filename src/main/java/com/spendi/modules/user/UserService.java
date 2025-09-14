
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

import org.bson.Document;
/**
 * ! lib imports
 */
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.time.Instant;
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
import com.spendi.modules.payment.PaymentMethodEntity;
import com.spendi.modules.payment.PaymentMethodService;
import com.spendi.modules.user.dto.UserCreateDto;

public class UserService extends BaseRepositoryService<UserRepository, UserEntity> {

	private static volatile UserService INSTANCE;
	private final FileService fileService = FileService.getInstance();
	private final PaymentMethodService paymentMethodService = PaymentMethodService.getInstance();

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

	/**
	 * ? ==================
	 * ? ===== Create =====
	 * ? ==================
	 */

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

	/**
	 * ? ================
	 * ? ===== Read =====
	 * ? ================
	 */

	/** Найти по email. */
	public ServiceResponse<UserEntity> getByEmail(String email) {
		return this.getOne("profile.email", email.toLowerCase());
	}

	/**
	 * Получить все способы оплаты для пользователя.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return все способы оплаты пользователя
	 */
	public ServiceResponse<List<PaymentMethodEntity>> getPaymentMethods(String requestId, String userId) {
		this.info("get payment methods by user id", requestId, detailsOf("userId", userId));

		return this.paymentMethodService.getMany(userId, new ObjectId(userId), 1, 250);
	}

	/**
	 * ? ==================
	 * ? ===== Update =====
	 * ? ==================
	 */

	/**
	 * Сменить пароль (принимает новый в открытом виде, хэширует).
	 */
	public ServiceResponse<UserEntity> changePassword(String requestId, String userId, String newPassword) {

		Document updateDoc = new Document("$set", new Document(
				Map.<String, Object>of(
						"security.passwordHash", CryptoUtils.hashPassword(newPassword),
						"system.meta.updatedAt", Instant.now())));

		var res = this.updateById(userId, updateDoc);
		this.info("user password changed", requestId, detailsOf("userId", userId), true);
		return res;
	}

	/**
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 */
	public ServiceResponse<UserEntity> touchLastLogin(String requestId, String userId) {
		Instant now = Instant.now();

		Document updateDoc = new Document("$set", new Document(
				Map.<String, Object>of(
						"system.meta.lastLoginAt", now)));

		var res = this.updateById(userId, updateDoc);

		this.info("user lastLoginAt updated", requestId,
				detailsOf("userId", userId, "at", now.toString()));
		return res;
	}

	/**
	 * @param requestId     request-id для корреляции логов
	 * @param userId        строковый ObjectId пользователя
	 * @param paymentMethod способ оплаты
	 * @return обновлённый пользователь с добавленным способом оплаты
	 */
	public ServiceResponse<UserEntity> addPaymentMethod(String requestId, String userId,
			PaymentMethodEntity paymentMethod) {

		this.info("add payment method to user", requestId,
				detailsOf("userId", userId, "paymentMethodId", paymentMethod.id.toHexString()));

		// append to user's finance.paymentMethodIds using $addToSet
		Document updates = new Document();
		updates.append("$addToSet", new Document("finance.paymentMethodIds", paymentMethod.id.toHexString()));
		updates.append("$set", new Document("system.meta.updatedAt", Instant.now()));
		updates.append("$inc", new Document("finance.accountsCount", 1));
		return this.updateById(userId, updates);
	}

	/**
	 * Загрузить/заменить аватар пользователю: сохраняет файл, обновляет
	 * account.avatarFileId у пользователя, логирует создание/замену
	 * и удаляет старый файл (best-effort).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param uf        файл для загрузки
	 * @return публичный URL аватарки
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
		Document updates = new Document("$set", new Document()
				.append("profile.avatarFileId", stored.id.toHexString())
				.append("system.meta.updatedAt", now));

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
	 * ? ==================
	 * ? ===== Delete =====
	 * ? ==================
	 */

	/**
	 * Полное удаление аватарки: очищает ссылку у пользователя и удаляет файл.
	 * Логирует операцию удаления (персистентно).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return публичный URL аватарки
	 */
	public ServiceResponse<String> deleteAvatar(String requestId, String userId) {
		UserEntity user = this.getById(userId).getData();

		String avatarId = user.getAvatarFileIdOptional().orElseThrow(() -> new EntityNotFoundException(
				"UserAvatar", "userId", userId));

		// Clear reference in user
		Document updates = new Document("$set", new Document()
				.append("profile.avatarFileId", null)
				.append("system.meta.updatedAt", Instant.now()));

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

	/**
	 * Удаление метода оплаты: удаляет ссылку у пользователя на метод оплаты.
	 * Логирует операцию удаления (персистентно).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param methodId  строковый ObjectId метода оплаты
	 * @return обновлённый пользователь с удалённым способом оплаты
	 */
	public ServiceResponse<UserEntity> deletePaymentMethod(String requestId, String userId, String methodId) {

		UserEntity user = this.getById(userId).getData();

		// проверка на наличие метода оплаты у пользователя
		if (!user.finance.paymentMethodIds.contains(methodId)) {
			throw new EntityNotFoundException("PaymentMethod", "PaymentMethods", user.finance.paymentMethodIds);
		}

		// удаляем метод оплаты
		this.paymentMethodService.deleteById(methodId);

		// Clear reference in user's finance.paymentMethodIds and update timestamp
		Document updates = new Document();
		updates.append("$pull", new Document("finance.paymentMethodIds", methodId));
		updates.append("$set", new Document("system.meta.updatedAt", Instant.now()));
		updates.append("$inc", new Document("finance.accountsCount", -1));

		// Clear reference in employee
		var res = this.updateById(userId, updates);

		// Лог удаления
		this.info("user payment method deleted", requestId,
				detailsOf("userId", userId, "methodId", methodId),
				true);

		return ServiceResponse.deleted(res.getData());
	}
}
