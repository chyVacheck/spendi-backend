
/**
 * @file MongoUpdateBuilder.java
 * @module core/base/database
 * @description
 * Билдер для создания GenericUpdate.
 * 
 * @param <TEntity> тип сущности (POJO), с которой работает репозиторий
 * @author Dmytro Shakh
 */

package com.spendi.core.base.database;

public class MongoUpdateBuilder {
	private final GenericUpdate genericUpdate = new GenericUpdate();

	/**
	 * Добавляет значение в массив, если оно еще не присутствует.
	 *
	 * @param field поле, в которое нужно добавить значение
	 * @param value значение для добавления
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder addToSet(String field, Object value) {
		genericUpdate.addToSet(field, value);
		return this;
	}

	/**
	 * Добавляет значение в конец массива.
	 *
	 * @param field поле, в которое нужно добавить значение
	 * @param value значение для добавления
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder push(String field, Object value) {
		genericUpdate.push(field, value);
		return this;
	}

	/**
	 * Удаляет значение из массива.
	 *
	 * @param field поле, из которого нужно удалить значение
	 * @param value значение для удаления
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder pull(String field, Object value) {
		genericUpdate.pull(field, value);
		return this;
	}

	/**
	 * Устанавливает значение поля.
	 *
	 * @param field поле, значение которого нужно установить
	 * @param value новое значение
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder set(String field, Object value) {
		genericUpdate.set(field, value);
		return this;
	}

	/**
	 * Увеличивает значение поля на заданное число.
	 *
	 * @param field поле, значение которого нужно увеличить
	 * @param value число, на которое нужно увеличить значение
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder inc(String field, Number value) {
		genericUpdate.inc(field, value);
		return this;
	}

	/**
	 * Увеличивает значение поля на 1.
	 *
	 * @param field поле, значение которого нужно увеличить
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder incOne(String field) {
		return inc(field, 1);
	}

	/**
	 * Уменьшает значение поля на 1.
	 *
	 * @param field поле, значение которого нужно уменьшить
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder decOne(String field) {
		return inc(field, -1);
	}

	/**
	 * Удаляет поле из документа.
	 *
	 * @param field поле, которое нужно удалить
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder unset(String field) {
		genericUpdate.unset(field);
		return this;
	}

	/**
	 * Умножает значение поля на заданное число.
	 *
	 * @param field поле, значение которого нужно умножить
	 * @param value число, на которое нужно умножить значение
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder mul(String field, Number value) {
		genericUpdate.mul(field, value);
		return this;
	}

	/**
	 * Переименовывает поле.
	 *
	 * @param field   старое имя поля
	 * @param newName новое имя поля
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder rename(String field, String newName) {
		genericUpdate.rename(field, newName);
		return this;
	}

	/**
	 * Метод для установки минимального значения для поля.
	 *
	 * @param field поле, для которого устанавливается минимальное значение
	 * @param value минимальное значение
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder min(String field, Object value) {
		genericUpdate.min(field, value);
		return this;
	}

	/**
	 * Метод для установки максимального значения для поля.
	 *
	 * @param field поле, для которого устанавливается максимальное значение
	 * @param value максимальное значение
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder max(String field, Object value) {
		genericUpdate.max(field, value);
		return this;
	}

	/**
	 * Метод для установки текущей даты для поля.
	 *
	 * @param field поле, для которого устанавливается текущая дата
	 * @return текущий экземпляр MongoUpdateBuilder
	 */
	public MongoUpdateBuilder currentDate(String field) {
		genericUpdate.currentDate(field);
		return this;
	}

	/**
	 * Строит и возвращает объект GenericUpdate.
	 *
	 * @return построенный объект GenericUpdate
	 */
	public GenericUpdate build() {
		return genericUpdate;
	}
}