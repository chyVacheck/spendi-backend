
/**
 * @file LifecycleMeta.java
 * @module shared/model/meta
 * 
 * Расширение AuditMeta: добавляет информацию о мягком удалении.
 * Используется для сущностей с полным жизненным циклом (создание → обновление → удаление).
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
public class LifecycleMeta extends AuditMeta {
	/**
	 * Время удаления сущности (soft-delete).
	 */
	@Builder.Default
	private Instant deletedAt = null;

	/**
	 * Идентификатор пользователя, который удалил сущность.
	 */
	@Builder.Default
	private ObjectId deletedBy = null;
}