
/**
 * @file EntityMeta.java
 * @module shared/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.shared.model;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! java imports
 */
import java.time.Instant;

/**
 * Вложенный класс для метаданных.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityMeta {
	/**
	 * Время создания сущности.
	 */
	@Builder.Default
	private Instant createdAt = Instant.now();

	/**
	 * Идентификатор создателя сущности.
	 */
	private ObjectId createdBy;

	/**
	 * Время последнего обновления сущности.
	 */
	@Builder.Default
	private Instant updatedAt = null;

	/**
	 * Идентификатор пользователя, который последний раз обновил сущность.
	 */
	@Builder.Default
	private ObjectId updatedBy = null;

	/**
	 * Время удаления сущности.
	 */
	@Builder.Default
	private Instant deletedAt = null;
}