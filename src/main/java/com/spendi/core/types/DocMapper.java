
/**
 * @file DocMapper.java
 * @module core/types
 *
 * Generic contract for mapping between entities and BSON documents.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.types;

/**
 * ! lib imports
 */
import org.bson.Document;

/**
 * Generic contract for mapping between entities and BSON documents.
 *
 * @param <T> entity type
 */
public interface DocMapper<T> {
	Document toDocument(T value);

	T fromDocument(Document doc);
}