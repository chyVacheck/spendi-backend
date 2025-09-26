
/**
 * @file UserSystemMeta.java
 * @module modules/user/model
 * @description Метаданные сущности пользователя (createdAt/updatedAt/deletedAt).
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.model;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ! java imports
 */
import java.time.Instant;

/**
 * Класс, представляющий метаданные пользователя. Содержит информацию о времени создания, последнего обновления и
 * удаления сущности пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSystemMeta {

	/**
	 * Время создания сущности пользователя.
	 */
	private Instant createdAt;

	/**
	 * Время последнего обновления сущности пользователя.
	 */
	private Instant updatedAt;

	/**
	 * Время удаления сущности пользователя (если применимо).
	 */
	private Instant deletedAt;

}