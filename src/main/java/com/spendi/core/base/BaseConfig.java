/**
 * @file BaseConfig.java
 * @module config
 *
 * @description
 * класс базовой конфигурация. Загружает переменные из .env (через dotenv-java)
 * или из системных переменных (System.getenv).
 *
 * Приоритет: .env > System.getenv > значения по умолчанию.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! lib imports
 */
import io.github.cdimascio.dotenv.Dotenv;

/**
* ! java imports
*/
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseConfig {
	protected Dotenv dotenv = Dotenv.configure()
			.ignoreIfMissing() // чтобы не падало, если .env нет
			.load();

	public BaseConfig() {
	}

	abstract public String toString();

	protected String getenv(Dotenv dotenv, String key, String defaultValue) {
		String val = dotenv.get(key);
		if (val == null || val.isBlank()) {
			val = System.getenv(key);
		}
		return (val != null && !val.isBlank()) ? val : defaultValue;
	}

	// ? =================
	// ? ==== HELPERS ====
	// ? =================

	/**
	 * Разобрать CSV-строку в set (trim + toLowerCase, без пустых).
	 *
	 * @param csv строка для парсинга
	 * @return разобрать csv-строку в set (trim + toLowerCase, без пустых)
	 */
	protected static Set<String> parseCsvSet(String csv) {
		if (csv == null || csv.isBlank())
			return Set.of();
		LinkedHashSet<String> acc = new LinkedHashSet<>(Arrays.asList(csv.split(",")));
		LinkedHashSet<String> out = new LinkedHashSet<>();
		for (String s : acc) {
			if (s == null)
				continue;
			String x = s.trim();
			if (!x.isBlank())
				out.add(x.toLowerCase());
		}
		return out;
	}

	/**
	 * Парсинг long с дефолтом.
	 *
	 * @param s   строка для парсинга
	 * @param def значение по умолчанию
	 * @return парсинг long с дефолтом
	 */
	protected static long parseLong(String s, long def) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Парсинг int с дефолтом.
	 *
	 * @param s   строка для парсинга
	 * @param def значение по умолчанию
	 * @return парсинг int с дефолтом
	 */
	protected static int parseInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Парсинг boolean из строки (true/1/yes).
	 *
	 * @param s   строка для парсинга
	 * @param def значение по умолчанию
	 * @return парсинг boolean из строки (true/1/yes)
	 */
	protected static boolean parseBool(String s, boolean def) {
		if (s == null)
			return def;
		String v = s.trim().toLowerCase();
		if (v.equals("true") || v.equals("1") || v.equals("yes"))
			return true;
		if (v.equals("false") || v.equals("0") || v.equals("no"))
			return false;
		return def;
	}

	/**
	 * Nullable Integer из строки.
	 *
	 * @param s строка для парсинга
	 * @return nullable integer из строки
	 */
	protected static Integer parseNullableInt(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Nullable Double из строки.
	 *
	 * @param s строка для парсинга
	 * @return nullable double из строки
	 */
	protected static Double parseNullableDouble(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return Double.parseDouble(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Парсинг enum с дефолтом, case-insensitive.
	 *
	 * @param s   строка для парсинга
	 * @param def значение по умолчанию
	 * @return парсинг enum с дефолтом, case-insensitive
	 */
	protected static <E extends Enum<E>> E parseEnum(String s, E def) {
		if (s == null || s.isBlank())
			return def;
		try {
			return Enum.valueOf(def.getDeclaringClass(), s.trim().toUpperCase());
		} catch (Exception e) {
			return def;
		}
	}

}