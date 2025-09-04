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
import com.spendi.config.ServerConfig;
import com.spendi.core.base.server.JavalinServerAdapter;
import com.spendi.core.base.server.MyExceptionMapper;
import com.spendi.core.middleware.RequestLifecycleMiddleware;
import com.spendi.core.router.NotFoundRouter;
import com.spendi.core.router.PingRouter;

public class App {
	public static void main(String[] args) {
		// Устанавливаем локаль по умолчанию (для ошибок, дат и сообщений).
		// Это гарантирует, что вся система будет использовать ENGLISH,
		// даже если у ОС другой язык.
		Locale.setDefault(Locale.ENGLISH);

		// Загружаем конфигурацию сервера (host, port и т.д.).
		ServerConfig serverConfig = new ServerConfig();

		// Загружаем конфигурацию API (например, префикс /api/v1).
		ApiConfig apiConfig = new ApiConfig();

		// Выводим информацию о том, какие параметры сервера загрузились.
		System.out.println("✅ Loaded config: " + serverConfig);

		// Создаём адаптер под Javalin (наш слой абстракции над фреймворком).
		var server = new JavalinServerAdapter();

		// Устанавливаем маппер ошибок.
		// Теперь любые исключения будут автоматически преобразованы
		// в ApiErrorResponse через MyExceptionMapper.
		server.setExceptionMapper(new MyExceptionMapper());

		// Глобальные middleware:
		// RequestLifecycleMiddleware – финальный шаг запроса.
		// Считает время обработки, выставляет X-Response-Time, X-Request-Id,
		// логирует результат (успех или ошибка).
		server.useAfter(new RequestLifecycleMiddleware());

		// Регистрируем PingRouter.
		// Содержит базовые эндпоинты для health-check и проверки версии.
		// Например:
		// GET /api/v1/ping
		// GET /api/v1/ping/version
		server.registerRouter(new PingRouter(apiConfig.getApiPrefix()));

		// Регистрируем NotFoundRouter (ОБЯЗАТЕЛЬНО последним).
		// Он обрабатывает все несуществующие пути внутри /api/v1/*
		// и выбрасывает RouterNotFoundException.
		// Благодаря этому клиент получает структурированный JSON с ошибкой,
		// а не дефолтную страницу Javalin.
		server.registerRouter(new NotFoundRouter(apiConfig.getApiPrefix()));

		// Запускаем HTTP-сервер на указанном порту.
		// После этого сервер начинает слушать входящие запросы.

		server.start(serverConfig.getPort());
	}
}
