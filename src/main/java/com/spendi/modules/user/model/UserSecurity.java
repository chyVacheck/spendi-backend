/**
 * @file UserSecurity.java
 * @module modules/user/model
 * @description Секьюрные данные пользователя (хэш пароля и т.п.).
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
 * Класс, представляющий секьюрные данные пользователя. Содержит информацию, такую как хэш пароля, которая не должна
 * быть доступна публично.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSecurity {

	/**
	 * Хэш пароля пользователя. Используется для безопасного хранения пароля. Это поле не должно быть отдано наружу
	 * (например, через API).
	 */
	private String passwordHash;
}