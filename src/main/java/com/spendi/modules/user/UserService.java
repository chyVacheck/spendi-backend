
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
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.database.MongoUpdateBuilder;
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.exceptions.EntityAlreadyExistsException;
import com.spendi.core.exceptions.EntityNotFoundException;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.utils.CryptoUtils;
import com.spendi.modules.files.FileService;
import com.spendi.modules.files.model.FileEntity;
import com.spendi.modules.payment.model.PaymentMethodEntity;
import com.spendi.modules.user.cmd.UserCreateCmd;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.user.model.UserFinance;
import com.spendi.modules.user.model.UserSystemMeta;
import com.spendi.modules.user.model.UserProfile;
import com.spendi.modules.user.model.UserSecurity;
import com.spendi.modules.user.model.UserSystem;
import com.spendi.modules.payment.PaymentMethodService;
import com.spendi.shared.dto.PaginationQueryDto;

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
			throw new IllegalStateException("UserService not initialized. Call AppInitializer.initAll() in App.main");
		}
		return ref;
	}

	/**
	 * ? === === === Create === === ===
	 */

	/**
	 * Создать пользователя из UserCreateCommand. Проверяет уникальность profile.email.
	 */
	public ServiceResponse<UserEntity> create(String requestId, UserCreateCmd cmd) {
		// Уникальность по email
		if (repository.existsByEmail(cmd.getProfile().getEmail())) {
			throw new EntityAlreadyExistsException(UserEntity.class.getSimpleName(),
					Map.of("profile.email", cmd.getProfile().getEmail()));
		}

		var now = Instant.now();

		var entity = new UserEntity();
		entity.setId(new ObjectId());

		// profile
		var profile = new UserProfile();
		profile.setEmail(cmd.getProfile().getEmail().toLowerCase());
		profile.setFirstName(cmd.getProfile().getFirstName());
		profile.setLastName(cmd.getProfile().getLastName());
		entity.setProfile(profile);

		// security
		var sec = new UserSecurity();
		sec.setPasswordHash(CryptoUtils.hashPassword(cmd.getSecurity().getPassword()));
		entity.setSecurity(sec);

		// finance
		var fin = new UserFinance();
		fin.setDefaultAccountId(null);
		fin.setPaymentMethodIds(Set.of());
		entity.setFinance(fin);

		// system
		var sys = new UserSystem();
		var meta = new UserSystemMeta();
		meta.setCreatedAt(now);
		meta.setUpdatedAt(now);
		sys.setMeta(meta);
		entity.setSystem(sys);

		var created = super.createOne(entity);
		this.info("user created", requestId, detailsOf("id", entity.getId().toHexString(), "email", profile.getEmail()),
				true);
		return created;
	}

	/**
	 * ? === === === Read === === ===
	 */

	/**
	 * Получить пользователя по email.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param email     email искомого пользователя
	 * 
	 * @return найденный пользователь по email
	 */
	public ServiceResponse<UserEntity> getByEmail(String requestId, String email) {
		this.info("get user by email", requestId, detailsOf("email", email));
		return this.getOne("profile.email", email.toLowerCase());
	}

	/**
	 * Получить все способы оплаты для пользователя.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return все способы оплаты пользователя
	 */
	public ServiceResponse<List<Map<String, Object>>> getPaymentMethods(String requestId, String userId,
			PaginationQueryDto paginationDto) {
		this.info("get payment methods by user id", requestId,
				detailsOf("userId", userId, "page", paginationDto.getPage(), "limit", paginationDto.getLimit()));

		ServiceResponse<List<PaymentMethodEntity>> paymentMethodRes = this.paymentMethodService.getMany("userId",
				new ObjectId(userId), paginationDto.getPage(), paginationDto.getLimit());

		List<Map<String, Object>> publicPaymentMethods = paymentMethodRes.getData().stream()
				.map(PaymentMethodEntity::getPublicData).collect(Collectors.toList());

		return ServiceResponse.founded(publicPaymentMethods, paymentMethodRes.getPaginationOrThrow());
	}

	/**
	 * ? === === === Update === === ===
	 */

	/**
	 * Сменить пароль (принимает новый в открытом виде, хэширует).
	 */
	public ServiceResponse<UserEntity> changePassword(String requestId, String userId, String newPassword) {
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("security.passwordHash", CryptoUtils.hashPassword(newPassword));
		updateBuilder.currentDate("system.meta.updatedAt");

		var res = this.updateById(userId, updateBuilder.build());
		this.info("user password changed", requestId, detailsOf("userId", userId), true);
		return res;
	}

	/**
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 */
	public ServiceResponse<UserEntity> touchLastLogin(String requestId, String userId) {
		Instant now = Instant.now();

		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("system.meta.lastLoginAt", now);

		var res = this.updateById(userId, updateBuilder.build());
		this.info("user lastLoginAt updated", requestId, detailsOf("userId", userId, "at", now.toString()));
		return res;
	}

	/**
	 * @param requestId     request-id для корреляции логов
	 * @param userId        ObjectId пользователя
	 * @param paymentMethod способ оплаты
	 * 
	 * @return обновлённый пользователь с добавленным способом оплаты
	 */
	public ServiceResponse<UserEntity> addPaymentMethod(String requestId, ObjectId userId,
			PaymentMethodEntity paymentMethod) {

		this.info("add payment method to user", requestId,
				detailsOf("userId", userId, "paymentMethodId", paymentMethod.getHexId()));

		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.currentDate("system.meta.updatedAt");
		updateBuilder.addToSet("finance.paymentMethodIds", paymentMethod.getHexId());
		updateBuilder.incOne("finance.accountsCount");

		return this.updateById(userId, updateBuilder.build());
	}

	/**
	 * @param requestId     request-id для корреляции логов
	 * @param userId        строковый ObjectId пользователя
	 * @param paymentMethod способ оплаты
	 * 
	 * @return обновлённый пользователь с добавленным способом оплаты
	 */
	public ServiceResponse<UserEntity> addPaymentMethod(String requestId, String userId,
			PaymentMethodEntity paymentMethod) {
		return this.addPaymentMethod(requestId, new ObjectId(userId), paymentMethod);
	}

	/**
	 * Обновить порядок способа оплаты у пользователя.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param methodId  строковый ObjectId способа оплаты
	 * @param order     новый порядок способа оплаты
	 * 
	 * @throws EntityNotFoundException если способ оплаты не найден
	 * 
	 * @return обновлённый пользователь с обновлённым порядком способа оплаты
	 */
	public ServiceResponse<PaymentMethodEntity> updatePaymentMethodOrder(String requestId, String userId,
			String methodId, int order) {
		PaymentMethodEntity pm = this.paymentMethodService.getById(methodId).getData();

		if (pm.getUserId().toHexString().equals(userId)) {
			// если не совпадает, просто отдаём not found
			throw new EntityNotFoundException(PaymentMethodEntity.class.getSimpleName(),
					Map.of("id", methodId, "userId", userId));
		}

		this.info("update payment method order", requestId,
				detailsOf("userId", userId, "paymentMethodId", methodId, "order", order));

		return this.paymentMethodService.updateOrder(requestId, userId, methodId, order);
	}

	/**
	 * Загрузить/заменить аватар пользователю: сохраняет файл, обновляет account.avatarFileId у пользователя, логирует
	 * создание/замену и удаляет старый файл (best-effort).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param uf        файл для загрузки
	 * @return публичный URL аватарки
	 */
	public ServiceResponse<String> uploadAvatar(String requestId, String userId, UploadedFile uf) {
		// ensure exists and remember previous avatar id
		UserEntity user = this.getById(userId).getData();
		ObjectId oldAvatarId = user.getProfile().getAvatarFileId();

		// store new file
		FileEntity stored = this.fileService.uploadOne(requestId, uf).getData();

		// build public url
		String url = user.getAvatarUrl();

		// update employee (persist only new file id)
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("profile.avatarFileId", stored.getHexId());
		updateBuilder.currentDate("system.meta.updatedAt");

		this.updateById(userId, updateBuilder.build());

		// Лог: создан или обновлён аватар
		if (oldAvatarId != null) {
			this.info("user avatar updated", requestId,
					detailsOf("userId", userId, "oldFileId", oldAvatarId.toHexString(), "newFileId", stored.getHexId()),
					true);
			// best-effort cleanup of previous avatar
			try {
				this.fileService.deleteById(requestId, oldAvatarId);
			} catch (RuntimeException ignore) {
			}

			return ServiceResponse.updated(url);
		} else {
			this.info("user avatar created", requestId, detailsOf("userId", userId, "fileId", stored.getHexId()), true);
			return ServiceResponse.created(url);
		}
	}

	/**
	 * ? === === === Delete === === ===
	 */

	/**
	 * Полное удаление аватарки: очищает ссылку у пользователя и удаляет файл. Логирует операцию удаления
	 * (персистентно).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return публичный URL аватарки
	 */
	public ServiceResponse<String> deleteAvatar(String requestId, String userId) {
		UserEntity user = this.getById(userId).getData();

		ObjectId avatarId = user.getAvatarFileIdOptional()
				.orElseThrow(() -> new EntityNotFoundException("UserAvatar", "userId", userId));

		// Clear reference in user
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.currentDate("system.meta.updatedAt");
		updateBuilder.set("profile.avatarFileId", null);

		// Clear reference in user
		this.updateById(userId, updateBuilder.build());

		// Удаление файла (метаданные + физический файл)
		this.fileService.deleteById(requestId, avatarId);

		// Лог удаления
		this.info("user avatar deleted", requestId, detailsOf("userId", userId, "fileId", avatarId), true);

		return ServiceResponse.deleted(user.getAvatarUrl());
	}

	/**
	 * Удаление метода оплаты: удаляет ссылку у пользователя на метод оплаты. Логирует операцию удаления (персистентно).
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param methodId  строковый ObjectId метода оплаты
	 * @return обновлённый пользователь с удалённым способом оплаты
	 */
	public ServiceResponse<UserEntity> deletePaymentMethod(String requestId, String userId, String methodId) {
		UserEntity user = this.getById(userId).getData();

		// проверка на наличие метода оплаты у пользователя
		if (!user.hasPaymentMethod(methodId)) {
			throw new EntityNotFoundException("PaymentMethod", "PaymentMethods",
					user.getFinance().getPaymentMethodIds());
		}

		// удаляем метод оплаты
		this.paymentMethodService.deleteById(methodId);

		// Clear reference in user's finance.paymentMethodIds and update timestamp
		Document updates = new Document();
		updates.append("$pull", new Document("finance.paymentMethodIds", methodId));

		// Clear reference in user
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.pull("finance.paymentMethodIds", methodId);
		updateBuilder.currentDate("system.meta.updatedAt");
		updateBuilder.decOne("finance.accountsCount");

		// Clear reference in user
		var res = this.updateById(userId, updateBuilder.build());

		// Лог удаления
		this.info("user payment method deleted", requestId, detailsOf("userId", userId, "methodId", methodId), true);

		return ServiceResponse.deleted(res.getData());
	}
}
