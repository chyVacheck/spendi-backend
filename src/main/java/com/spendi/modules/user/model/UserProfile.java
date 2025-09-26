/**
 * @file UserProfile.java
 * @module modules/user/model
 * @description Публичные данные профиля пользователя (email, имя, аватар).
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
import org.bson.types.ObjectId;

/**
 * Класс, представляющий публичные данные профиля пользователя. Включает информацию об электронной почте, имени, фамилии
 * и идентификаторе файла аватара.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

	/**
	 * Адрес электронной почты пользователя.
	 */
	private String email;

	/**
	 * Имя пользователя.
	 */
	private String firstName;

	/**
	 * Фамилия пользователя.
	 */
	private String lastName;

	/**
	 * Идентификатор файла аватара пользователя в системе хранения.
	 */
	@Builder.Default
	private ObjectId avatarFileId = null;

	/**
	 * Устанавливает идентификатор файла аватара пользователя из строкового представления. Если строка `avatarFileId`
	 * равна null, то идентификатор аватара также устанавливается в null.
	 * 
	 * @param avatarFileId ObjectId идентификатор файла аватара.
	 */
	public void setAvatarFileId(ObjectId avatarFileId) {
		this.avatarFileId = avatarFileId;
	}

	/**
	 * Устанавливает идентификатор файла аватара пользователя из строкового представления. Если строка `avatarFileId`
	 * равна null, то идентификатор аватара также устанавливается в null.
	 * 
	 * @param avatarFileId Строковое представление идентификатора файла аватара.
	 */
	public void setAvatarFileId(String avatarFileId) {
		this.avatarFileId = (avatarFileId == null) ? null : new ObjectId(avatarFileId);
	}

	/**
	 * Очищает идентификатор файла аватара, устанавливая его в null.
	 */
	public void clearAvatarFileId() {
		this.avatarFileId = null;
	}
}