
/**
 * @file AuditMeta.java
 * @module shared/model/meta
 * 
 * Расширение BaseMeta: добавляет информацию об обновлениях.
 * Используется для сущностей с возможностью обновления.
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ! java imports
 */
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class AuditMeta extends BaseMeta {
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
}