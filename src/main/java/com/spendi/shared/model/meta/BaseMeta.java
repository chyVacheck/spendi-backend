
/**
 * @file BaseMeta.java
 * @module shared/model/meta
 * 
 * Базовая мета-информация: только факт создания.
 * Используется для сущностей, у которых нет обновлений/удалений.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.shared.model.meta;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ! lib imports
 */
import java.time.Instant;

/**
 * Базовая мета-информация: только факт создания. Используется для сущностей, у которых нет обновлений/удалений.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BaseMeta {
	/**
	 * Время создания сущности.
	 */
	@Builder.Default
	private Instant createdAt = Instant.now();

	/**
	 * Идентификатор создателя сущности.
	 */
	@Builder.Default
	private ObjectId createdBy = null;
}
