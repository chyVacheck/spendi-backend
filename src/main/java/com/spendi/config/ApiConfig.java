
/**
 * @file ApiConfig.java
 * @module config
 *
 * @description
 * Конфигурация API endpoints для приложения Spendi Backend.
 * Управляет префиксами маршрутов и версионированием API.
 * 
 * Основная задача - централизованное управление префиксом API,
 * что позволяет легко изменять версию API или структуру маршрутов
 * без модификации каждого роутера отдельно.
 * 
 * Использует паттерн Singleton для обеспечения единственного
 * экземпляра конфигурации во всем приложении.
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

/**
 * Конфигурация API маршрутов и версионирования.
 * 
 * <p>Этот класс отвечает за управление структурой API endpoints:
 * <ul>
 *   <li>Определение базового префикса для всех API маршрутов</li>
 *   <li>Версионирование API (например, /api/v1, /api/v2)</li>
 *   <li>Централизованное изменение структуры маршрутов</li>
 * </ul>
 * 
 * <p>Конфигурация загружается из переменных окружения с fallback значениями:
 * <ul>
 *   <li>{@code SPENDI_API_PREFIX} - префикс для API маршрутов (по умолчанию: "/api/v1")</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * ApiConfig config = ApiConfig.getConfig();
 * String prefix = config.getApiPrefix(); // "/api/v1"
 * 
 * // В роутере:
 * server.registerRouter(new UserRouter(config.getApiPrefix()));
 * // Результат: маршруты будут доступны по /api/v1/users/*
 * }</pre>
 * 
 * @see BaseConfig
 */
public class ApiConfig extends BaseConfig {
	/** Единственный экземпляр конфигурации (Singleton pattern) */
	private static final ApiConfig INSTANCE = new ApiConfig();
	
	/** Префикс для всех API маршрутов (например, "/api/v1") */
	private final String apiPrefix;

	/**
	 * Приватный конструктор для реализации паттерна Singleton.
	 * 
	 * <p>Загружает конфигурацию из переменных окружения:
	 * <ul>
	 *   <li>SPENDI_API_PREFIX - базовый префикс API (по умолчанию "/api/v1")</li>
	 * </ul>
	 * 
	 * <p>Порядок приоритета источников конфигурации:
	 * <ol>
	 *   <li>Переменные окружения системы</li>
	 *   <li>.env файл</li>
	 *   <li>Значения по умолчанию</li>
	 * </ol>
	 */
	private ApiConfig() {
		// Загружаем префикс API из переменных окружения или используем значение по умолчанию
		// Это позволяет легко изменять версию API без перекомпиляции
		this.apiPrefix = this.getenv(this.dotenv, "SPENDI_API_PREFIX", "/api/v1");
	}

	/**
	 * Получить единственный экземпляр конфигурации API.
	 * 
	 * @return экземпляр ApiConfig
	 */
	public static ApiConfig getConfig() {
		return INSTANCE;
	}

	/**
	 * Получить префикс для API маршрутов.
	 * 
	 * <p>Этот префикс используется всеми роутерами для формирования
	 * полных путей к API endpoints. Например, если префикс "/api/v1",
	 * то маршрут пользователей будет доступен по "/api/v1/users".
	 * 
	 * @return префикс API маршрутов (например, "/api/v1")
	 */
	public String getApiPrefix() {
		return this.apiPrefix;
	}

	/**
	 * Строковое представление конфигурации для логирования и отладки.
	 * 
	 * @return строка с основными параметрами конфигурации
	 */
	@Override
	public String toString() {
		return "ApiConfig{prefix='%s'}".formatted(apiPrefix);
	}
}
