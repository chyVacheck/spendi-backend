/**
 * @file AppInitializer.java
 * @module core/init
 *
 * Централизованная инициализация модулей (composition root).
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.init;

/**
 * ! my imports
 */
import com.spendi.core.database.MongoProvider;
import com.spendi.core.files.FileStorage;
import com.spendi.modules.files.FileRepository;
import com.spendi.modules.files.FileService;
import com.spendi.modules.user.UserRepository;
import com.spendi.modules.user.UserService;
import com.spendi.modules.payment.PaymentMethodRepository;
import com.spendi.modules.payment.PaymentMethodService;
import com.spendi.modules.session.SessionRepository;
import com.spendi.modules.session.SessionService;
import com.mongodb.client.MongoDatabase;

public final class AppInitializer {
	private AppInitializer() {
	}

	/** Точка входа для инициализации всех модулей. */
	public static void initAll() {
		// Явно инициализируем подключение к MongoDB
		MongoProvider.init();
		// Получаем экземпляр базы для инициализации модулей
		MongoDatabase db = MongoProvider.getDatabase();

		initFilesModule(db);
		initSessionModule(db);
		initPaymentModule(db);
		initUserModule(db);
	}

	/**
	 * Инициализация модуля работы с файлами (storage + repo + service).
	 */
	public static void initFilesModule(MongoDatabase db) {
		var fileRepo = new FileRepository(db);

		// Инициализируем хранилище файлов (локальная ФС)
		FileStorage.init(new FileStorage());

		// Инициализируем сервис файлов с явным репозиторием
		FileService.init(fileRepo);
	}

	/**
	 * Инициализация модуля сессий (repo + service).
	 */
	public static void initSessionModule(MongoDatabase db) {
		var sessionRepo = new SessionRepository(db);

		// Инициализируем сервис сессий с явным репозиторием
		SessionService.init(sessionRepo);
	}

	/**
	 * Инициализация модуля пользователей (repo + service).
	 */
	public static void initUserModule(MongoDatabase db) {
		var userRepo = new UserRepository(db);
		UserService.init(userRepo);
	}

	/** Инициализация модуля способов оплаты. */
	public static void initPaymentModule(MongoDatabase db) {
		var pmRepo = new PaymentMethodRepository(db);
		PaymentMethodService.init(pmRepo);
	}

}
