
/**
 * @file LogOptions.java
 * @module core/logger/types
 * @description
 * Опции для настройки логирования.
 * Позволяет управлять отображением дополнительной информации в логах,
 * такой как идентификатор запроса и детали выполнения.
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.logger.types;

/**
 * Класс, представляющий настройки логирования.
 * Позволяет гибко настраивать отображение различной отладочной информации в
 * логах.
 */
public class LogOptions {
	/**
	 * Флаг, указывающий, нужно ли сохранять логи.
	 */
	private final boolean save;

	/**
	 * Создает новый экземпляр LogOptions с указанными параметрами.
	 *
	 * @param save флаг сохранения логов
	 */
	public LogOptions(boolean save) {
		this.save = save;
	}

	/**
	 * Проверяет, нужно ли сохранять логи.
	 *
	 * @return true если логи нужно сохранять, иначе false
	 */
	public boolean shouldSave() {
		return save;
	}

	@Override
	public String toString() {
		return "LogOptions{" + "save=" + save + '}';
	}
}
