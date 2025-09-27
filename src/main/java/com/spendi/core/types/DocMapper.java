
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
	/**
	 * Преобразует BSON-документ в сущность доменной модели.
	 *
	 * @param doc BSON-документ из MongoDB (не null)
	 * @return сущность TEntity
	 */
	T toEntity(Document doc);

	/**
	 * Преобразует сущность доменной модели в BSON-документ для записи в MongoDB.
	 *
	 * @param value сущность TEntity (не null)
	 * @return BSON-документ Document
	 */
	Document toDocument(T value);

}