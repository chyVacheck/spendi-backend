/**
 * @file UserSystem.java
 * @module modules/user/model
 * @description Системные данные пользователя: мета и lastLoginAt.
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
 * Класс, представляющий системные данные пользователя. Содержит метаинформацию и данные о последнем входе в систему.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSystem {

	/**
	 * Метаданные пользователя, включающие информацию о создании, обновлении и удалении.
	 */
	private UserSystemMeta meta;

	/**
	 * Время последнего входа пользователя в систему (может быть null).
	 */
	@Builder.Default
	private Instant lastLoginAt = null;
}