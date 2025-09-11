/**
 * @file App.java
 * @module com.spendi
 *
 * @description
 * Точка входа в приложение Spendi Backend.
 * Отвечает за инициализацию конфигурации, настройку сервера и регистрацию роутеров.
 *
 * Запуск приложения:
 *  - Загружаются конфиги (ServerConfig, ApiConfig).
 *  - Создаётся и настраивается адаптер веб-сервера (JavalinServerAdapter).
 *  - Подключается глобальный ExceptionMapper для унифицированной обработки ошибок.
 *  - Подключаются глобальные middleware (например, RequestLifecycleMiddleware).
 *  - Регистрируются роутеры (PingRouter, NotFoundRouter и др.).
 *  - Запускается HTTP-сервер на указанном порту.
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
import com.spendi.config.ApiConfig;
import com.spendi.config.AuthConfig;
import com.spendi.config.ServerConfig;
import com.spendi.core.base.server.JavalinServerAdapter;
import com.spendi.core.base.server.MyExceptionMapper;
import com.spendi.core.init.AppInitializer;
import com.spendi.core.middleware.RequestLifecycleMiddleware;
import com.spendi.core.router.NotFoundRouter;
import com.spendi.core.router.PingRouter;
import com.spendi.modules.auth.AuthRouter;
import com.spendi.modules.files.FileRouter;
import com.spendi.modules.user.UserRouter;

public class App {
	public static void main(String[] args) {
		// ? --- Локаль процесса -------------------------------------------------
		// Устанавливаем локаль по умолчанию (для форматирования ошибок, дат и
		// сообщений).
		// Это гарантирует, что вся система будет использовать ENGLISH,
		// даже если у ОС/контейнера иной язык. Полезно для детерминированных логов.
		Locale.setDefault(Locale.ENGLISH);

		// ? --- Конфигурация ----------------------------------------------------
		// Конфиг сервера (порт, хост и пр.). Источник значений инкапсулирован
		// внутри ServerConfig (например, .env/переменные окружения/файлы свойств).
		ServerConfig serverConfig = ServerConfig.getConfig();

		// Конфиг API (например, общий префикс /api/v1). Единая точка изменения
		// префикса помогает избежать «магических строк» в роутерах.
		ApiConfig apiConfig = ApiConfig.getConfig();

		// Конфиг авторизации/сессий. Источник значений точка изменения
		// времени жизни сессии, названия ключа для cookie и т.д.
		AuthConfig authConfig = AuthConfig.getConfig();

		// Отладочная печать загруженных параметров
		// (полезно при старте в разных средах).
		System.out.println("✅ Loaded config for server: " + serverConfig);
		System.out.println("✅ Loaded config api: " + apiConfig);
		System.out.println("✅ Loaded config auth: " + authConfig);

		// ? --- Инициализация модулей ------------------------------------------
		// Централизованная инициализация зависимостей приложения: сервисов,
		// репозиториев,
		// клиентов внешних API, файловых/облачных хранилищ и т.д.
		// Важно: порядок может иметь значение, если модули зависят друг от друга.
		AppInitializer.initAll();

		// ? --- Сервер/фреймворк -----------------------------------------------
		// Создаём адаптер над Javalin. Наличие собственного адаптера позволяет:
		// - абстрагироваться от конкретного фреймворка;
		// - удобно тестировать (можно подменять адаптер/mock);
		// - упростить миграцию на другой стек при необходимости.
		var server = new JavalinServerAdapter();

		// Централизованный маппер исключений: доменные/технические исключения
		// приводятся к единообразному JSON-ответу (ApiErrorResponse) с корректным
		// HTTP-статусом. Это повышает предсказуемость для клиентов API.
		server.setExceptionMapper(new MyExceptionMapper());

		// ? --- Глобальные middleware -------------------------------------------
		// RequestLifecycleMiddleware (after-фаза):
		// - считает время обработки и выставляет X-Response-Time;
		// - присваивает/прокидывает X-Request-Id для корреляции логов;
		// - логирует результат (успех/ошибка) с итоговым статусом.
		server.useAfter(new RequestLifecycleMiddleware());

		// ? --- Регистрация роутеров -------------------------------------------
		// Рекомендуется группировать регистрацию логически и передавать общий
		// API-префикс из ApiConfig, чтобы не дублировать строки и не ошибаться.

		// Служебные/диагностические маршруты: health-check, версия и пр.
		// Примеры:
		// GET {prefix}/ping
		// GET {prefix}/ping/version
		server.registerRouter(new PingRouter(apiConfig.getApiPrefix()));

		// ! admin router
		// Файлы/загрузки/скачивание артефактов
		server.registerRouter(new FileRouter(apiConfig.getApiPrefix()));

		// Аутентификация/авторизация
		server.registerRouter(new AuthRouter(apiConfig.getApiPrefix()));

		// Пользователи
		server.registerRouter(new UserRouter(apiConfig.getApiPrefix()));

		// Маршрут «не найдено» перехватывает несуществующие пути внутри {prefix}/* и
		// выбрасывает RouterNotFoundException, чтобы клиент получил
		// структурированный JSON, а не дефолтный ответ фреймворка.
		server.registerRouter(new NotFoundRouter(apiConfig.getApiPrefix()));

		// ? --- Старт сервера ---------------------------------------------------
		// Фактический запуск HTTP-сервера на указанном порту.
		server.start(serverConfig.getPort());
	}
}
