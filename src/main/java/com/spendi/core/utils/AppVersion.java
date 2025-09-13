/**
 * @file AppVersion.java
 * @module core/utils
 *
 * @description
 * Утилита для получения версии приложения.
 * Версия берётся из META-INF/MANIFEST.MF (при сборке) или из pom.xml (через ресурс).
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.core.utils;

/**
 * ! java imports
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppVersion {
	private static final String VERSION;

	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private AppVersion() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	static {
		String version = null;

		// 1) Попытка: взять из MANIFEST.MF -> Implementation-Version
		Package p = AppVersion.class.getPackage();
		if (p != null) {
			version = p.getImplementationVersion(); // будет null в dev-ранне без упаковки
		}

		// 2) Фоллбэк: app.properties (если включена фильтрация ресурсов — подставится
		// project.version)
		if (version == null) {
			try (InputStream in = AppVersion.class.getResourceAsStream("/app.properties")) {
				if (in != null) {
					Properties props = new Properties();
					props.load(in);
					version = props.getProperty("app.version");
				}
			} catch (IOException ignored) {
			}
		}

		VERSION = (version != null && !version.isBlank()) ? version : "unknown";
	}

	public static String get() {
		return VERSION;
	}
}