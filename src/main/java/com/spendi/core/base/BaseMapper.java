/**
 * @file BaseMapper.java
 * @module core/base
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

public abstract class BaseMapper extends BaseClass {

	public BaseMapper(String className) {
		super(EClassType.MAPPER, className);
	}

	/** Безопасно конвертит raw в ObjectId, бросает IAE при некорректном значении. */
	protected ObjectId readObjectId(Object raw) {
		if (raw == null)
			return null;
		if (raw instanceof ObjectId oid)
			return oid;
		try {
			return new ObjectId(raw.toString());
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Invalid ObjectId value: " + raw, ex);
		}
	}

	/** Записывает ключ только если значение не null. Возвращает тот же Document для чейнинга. */
	protected <T> Document putIfNotNull(Document d, String key, T value) {
		if (value != null)
			d.put(key, value);
		return d;
	}
}
