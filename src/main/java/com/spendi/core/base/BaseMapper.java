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
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

public abstract class BaseMapper extends BaseClass {

	public BaseMapper(String className) {
		super(EClassType.MAPPER, className);
	}

	protected ObjectId readObjectId(Object raw) {
		if (raw == null)
			return null;

		// если raw является экземпляром ObjectId, возвращаем его, иначе создаем новый ObjectId из строки
		return (raw instanceof ObjectId oid) ? oid : new ObjectId(raw.toString());
	}
}
