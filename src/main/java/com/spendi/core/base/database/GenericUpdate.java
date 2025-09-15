
/**
 * @file GenericUpdate.java
 * @module core/base/database
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.base.database;

/**
 * ! lib imports
 */
import org.bson.Document;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericUpdate {
	private Map<String, Object> setOperations;
	private Map<String, Number> incOperations;
	private Map<String, Object> unsetOperations;
	private Map<String, Object> pushOperations;
	private Map<String, Object> pullOperations;
	private Map<String, Object> addToSetOperations;
	private Map<String, Number> mulOperations;
	private Map<String, String> renameOperations;
	private Map<String, Object> minOperations;
	private Map<String, Object> maxOperations;
	private Map<String, Object> currentDateOperations;

	/**
	 * Метод для установки значения поля.
	 *
	 * @param field поле, значение которого нужно установить
	 * @param value новое значение
	 */
	public void set(String field, Object value) {
		if (setOperations == null) {
			setOperations = new HashMap<>();
		}
		setOperations.put(field, value);
	}

	/**
	 * Метод для увеличения значения поля на заданное число.
	 *
	 * @param field поле, значение которого нужно увеличить
	 * @param value число, на которое нужно увеличить значение
	 */
	public void inc(String field, Number value) {
		if (incOperations == null) {
			incOperations = new HashMap<>();
		}
		incOperations.put(field, value);
	}

	/**
	 * Метод для удаления поля из документа.
	 *
	 * @param field поле, которое нужно удалить
	 */
	public void unset(String field) {
		if (unsetOperations == null) {
			unsetOperations = new HashMap<>();
		}
		unsetOperations.put(field, "");
	}

	/**
	 * Метод для добавления значения в конец массива.
	 *
	 * @param field поле, в которое нужно добавить значение
	 * @param value значение для добавления
	 */
	public void push(String field, Object value) {
		if (pushOperations == null) {
			pushOperations = new HashMap<>();
		}
		pushOperations.put(field, value);
	}

	/**
	 * Метод для удаления значения из массива.
	 *
	 * @param field поле, из которого нужно удалить значение
	 * @param value значение для удаления
	 */
	public void pull(String field, Object value) {
		if (pullOperations == null) {
			pullOperations = new HashMap<>();
		}
		pullOperations.put(field, value);
	}

	/**
	 * Метод для удаления нескольких значений из массива.
	 *
	 * @param field поле, из которого нужно удалить значение
	 * @param value значение для удаления
	 */
	public void pull(String field, List<Object> values) {
		if (pullOperations == null) {
			pullOperations = new HashMap<>();
		}
		pullOperations.put(field, new Document("$in", values));
	}

	/**
	 * Метод для добавления значения в массив, если оно еще не присутствует.
	 *
	 * @param field поле, в которое нужно добавить значение
	 * @param value значение для добавления
	 */
	public void addToSet(String field, Object value) {
		if (addToSetOperations == null) {
			addToSetOperations = new HashMap<>();
		}
		addToSetOperations.put(field, value);
	}

	/**
	 * Метод для добавления нескольких значений в массив, если они еще не присутствуют.
	 *
	 * @param field  поле, в которое нужно добавить значение
	 * @param values список значений для добавления
	 */
	public void addToSet(String field, List<Object> values) {
		if (addToSetOperations == null) {
			addToSetOperations = new HashMap<>();
		}
		addToSetOperations.put(field, new Document("$each", values));
	}

	/**
	 * Метод для умножения значения поля на заданное число.
	 *
	 * @param field поле, значение которого нужно умножить
	 * @param value число, на которое нужно умножить значение
	 */
	public void mul(String field, Number value) {
		if (mulOperations == null) {
			mulOperations = new HashMap<>();
		}
		mulOperations.put(field, value);
	}

	/**
	 * Метод для переименования поля.
	 *
	 * @param field   старое имя поля
	 * @param newName новое имя поля
	 */
	public void rename(String field, String newName) {
		if (renameOperations == null) {
			renameOperations = new HashMap<>();
		}
		renameOperations.put(field, newName);
	}

	/**
	 * Метод для установки минимального значения для поля.
	 *
	 * @param field поле, для которого устанавливается минимальное значение
	 * @param value минимальное значение
	 */
	public void min(String field, Object value) {
		if (minOperations == null) {
			minOperations = new HashMap<>();
		}
		minOperations.put(field, value);
	}

	/**
	 * Метод для установки максимального значения для поля.
	 *
	 * @param field поле, для которого устанавливается максимальное значение
	 * @param value максимальное значение
	 */
	public void max(String field, Object value) {
		if (maxOperations == null) {
			maxOperations = new HashMap<>();
		}
		maxOperations.put(field, value);
	}

	/**
	 * Метод для установки текущей даты для поля.
	 *
	 * @param field поле, для которого устанавливается текущая дата
	 */
	public void currentDate(String field) {
		if (currentDateOperations == null) {
			currentDateOperations = new HashMap<>();
		}
		currentDateOperations.put(field, new Document("$type", "date"));
	}

	/**
	 * Метод для преобразования GenericUpdate в org.bson.Document для MongoDB.
	 *
	 * @return Document, представляющий операции обновления MongoDB
	 */
	public Document toMongoDocument() {
		Document document = new Document();

		if (setOperations != null && !setOperations.isEmpty()) {
			document.append("$set", new Document(setOperations));
		}
		if (incOperations != null && !incOperations.isEmpty()) {
			document.append("$inc", new Document(incOperations));
		}
		if (unsetOperations != null && !unsetOperations.isEmpty()) {
			document.append("$unset", new Document(unsetOperations));
		}
		if (pushOperations != null && !pushOperations.isEmpty()) {
			document.append("$push", new Document(pushOperations));
		}
		if (pullOperations != null && !pullOperations.isEmpty()) {
			document.append("$pull", new Document(pullOperations));
		}
		if (addToSetOperations != null && !addToSetOperations.isEmpty()) {
			document.append("$addToSet", new Document(addToSetOperations));
		}
		if (mulOperations != null && !mulOperations.isEmpty()) {
			document.append("$mul", new Document(mulOperations));
		}
		if (renameOperations != null && !renameOperations.isEmpty()) {
			document.append("$rename", new Document(renameOperations));
		}
		if (minOperations != null && !minOperations.isEmpty()) {
			document.append("$min", new Document(minOperations));
		}
		if (maxOperations != null && !maxOperations.isEmpty()) {
			document.append("$max", new Document(maxOperations));
		}
		if (currentDateOperations != null && !currentDateOperations.isEmpty()) {
			document.append("$currentDate", new Document(currentDateOperations));
		}

		return document;
	}

	/**
	 * Проверяет, содержит ли GenericUpdate какие-либо операции обновления.
	 *
	 * @return true, если нет операций обновления, иначе false
	 */
	public boolean isEmpty() {
		return (setOperations == null || setOperations.isEmpty()) && (incOperations == null || incOperations.isEmpty())
				&& (unsetOperations == null || unsetOperations.isEmpty())
				&& (pushOperations == null || pushOperations.isEmpty())
				&& (pullOperations == null || pullOperations.isEmpty())
				&& (addToSetOperations == null || addToSetOperations.isEmpty())
				&& (mulOperations == null || mulOperations.isEmpty())
				&& (renameOperations == null || renameOperations.isEmpty())
				&& (minOperations == null || minOperations.isEmpty())
				&& (maxOperations == null || maxOperations.isEmpty())
				&& (currentDateOperations == null || currentDateOperations.isEmpty());
	}
}