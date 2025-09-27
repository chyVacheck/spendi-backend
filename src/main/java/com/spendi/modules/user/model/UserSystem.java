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
import com.spendi.shared.model.meta.LifecycleMeta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ! java imports
 */
import java.time.Instant;

/**
 * Системные данные пользователя: - {@link LifecycleMeta} — аудит и soft-delete поля; - {@code lastLoginAt} — время
 * последнего входа пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSystem {

	/**
	 * Метаданные жизненного цикла сущности (кто/когда создал, обновил, удалил).
	 */
	private LifecycleMeta meta;

	/**
	 * Время последнего входа пользователя в систему (может быть null).
	 */
	@Builder.Default
	private Instant lastLoginAt = null;
}