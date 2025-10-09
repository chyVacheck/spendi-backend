
/**
 * @file UserEntity.java
 * @module modules/user/model
 * @description Корневая сущность пользователя: ссылки на профиль, безопасность, финансы и системный
 *              блок.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.model;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.utils.CryptoUtils;
import com.spendi.shared.model.meta.LifecycleMeta;

/**
 * Корневая сущность пользователя, объединяющая различные аспекты пользовательских данных. Включает
 * ссылки на профиль, данные безопасности, финансовую информацию и системные метаданные.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEntity {

	// * === === === root === === ===

	/**
	 * Уникальный идентификатор пользователя (ObjectId).
	 */
	private ObjectId id;

	/**
	 * Блок профиля пользователя, содержащий публичную информацию.
	 */
	private UserProfile profile;

	/**
	 * Блок безопасности пользователя, содержащий конфиденциальные данные, такие как хэш пароля.
	 */
	private UserSecurity security;

	/**
	 * Финансовый блок пользователя, содержащий ссылки на финансовые сущности.
	 */
	private UserFinance finance;

	/**
	 * Системный блок, содержащий системную и метаинформацию о пользователе.
	 */
	private UserSystem system;

	// * === === === helpers === === ===

	/**
	 * Устанавливает уникальный идентификатор пользователя.
	 * 
	 * @param id Шестнадцатеричная строка идентификатора для установки (может быть null).
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

	/**
	 * Устанавливает уникальный идентификатор пользователя.
	 * 
	 * @param id Шестнадцатеричная строка идентификатора для установки (может быть null).
	 */
	public void setId(String id) {
		this.id = (id == null) ? null : new ObjectId(id);
	}

	/**
	 * Возвращает hex-строковое представление идентификатора пользователя.
	 * 
	 * @return Hex-строка идентификатора или null, если идентификатор не установлен.
	 */
	public String getHexId() {
		return id != null ? id.toHexString() : null;
	}

	/**
	 * Возвращает адрес электронной почты пользователя из профиля.
	 * 
	 * @return Адрес электронной почты или null, если профиль не установлен.
	 */
	public String getEmail() {
		return profile != null ? profile.getEmail() : null;
	}

	/**
	 * Проверяет, есть ли у пользователя аватар.
	 * 
	 * @return true, если у пользователя есть аватар, иначе false.
	 */
	public boolean hasAvatar() {
		return profile != null && profile.getAvatarFileId() != null;
	}

	/**
	 * Возвращает URL аватара для текущего пользователя.
	 * 
	 * @return URL аватара или null, если идентификатор пользователя не установлен.
	 */
	public String getAvatarUrl() {
		return (id != null) ? ("/users/" + id.toHexString() + "/avatar") : null;
	}

	/**
	 * Проверяет обычный пароль на соответствие сохраненному хэшу.
	 * 
	 * @param password Обычный (нехэшированный) пароль.
	 * @return true, если пароль совпадает, иначе false.
	 */
	public boolean comparePassword(String password) {
		if (security == null || security.getPasswordHash() == null)
			return false;
		return CryptoUtils.verifyPassword(password, security.getPasswordHash());
	}

	/**
	 * Возвращает Optional с идентификатором файла аватара.
	 * 
	 * @return Optional, содержащий ObjectId файла аватара, если он существует, иначе пустой
	 *         Optional.
	 */
	public Optional<ObjectId> getAvatarFileIdOptional() {
		return (profile != null && profile.getAvatarFileId() != null)
				? Optional.of(profile.getAvatarFileId())
				: Optional.empty();
	}

	/**
	 * Проверяет принадлежность метода оплаты по его шестнадцатеричному идентификатору.
	 * 
	 * @param pmIdHex Шестнадцатеричный идентификатор метода оплаты.
	 * @return true, если метод оплаты принадлежит пользователю, иначе false.
	 */
	public boolean hasPaymentMethod(String pmIdHex) {
		return finance != null && finance.hasPaymentMethod(new ObjectId(pmIdHex));
	}

	/**
	 * Проверяет принадлежность метода оплаты по его ObjectId.
	 * 
	 * @param pmId ObjectId метода оплаты.
	 * @return true, если метод оплаты принадлежит пользователю, иначе false.
	 */
	public boolean hasPaymentMethod(ObjectId pmId) {
		return finance != null && finance.hasPaymentMethod(pmId);
	}

	/**
	 * Возвращает публичные данные пользователя. Эти данные предназначены для отображения широкому
	 * кругу пользователей и не содержат конфиденциальной информации.
	 * 
	 * @return Объект, содержащий публичные данные пользователя.
	 */
	public Object getPublicData() {
		Map<String, Object> out = new HashMap<>(3);

		// id
		if (this.id != null) {
			out.put("id", this.id.toHexString());
		}

		// profile (публичный)
		if (this.profile != null) {
			Map<String, Object> prof = new HashMap<>(6);
			prof.put("email", this.profile.getEmail());
			prof.put("firstName", this.profile.getFirstName());
			prof.put("lastName", this.profile.getLastName());
			prof.put("avatarUrl", getAvatarUrl());
			out.put("profile", prof);
		}

		// system (meta + lastLoginAt)
		if (this.system != null) {
			Map<String, Object> sys = new HashMap<>(2);

			LifecycleMeta m = this.system.getMeta();
			if (m != null) {
				Map<String, Object> meta = new HashMap<>(3);
				meta.put("createdAt", m.getCreatedAt());
				meta.put("updatedAt", m.getUpdatedAt());
				meta.put("deletedAt", m.getDeletedAt());
				sys.put("meta", meta);
			}

			sys.put("lastLoginAt", this.system.getLastLoginAt());
			out.put("system", sys);
		}

		return out;
	}

	/**
	 * Возвращает приватные данные пользователя. Эти данные включают финансовую информацию, но
	 * исключают конфиденциальные данные безопасности (например, хэш пароля). Предназначены для
	 * использования текущим пользователем.
	 * 
	 * @return Объект, содержащий приватные данные пользователя.
	 */
	public Object getPrivateData() {
		Map<String, Object> out = new HashMap<>(4);

		// id
		if (this.id != null) {
			out.put("id", this.id.toHexString());
		}

		// profile (как в public)
		if (this.profile != null) {
			Map<String, Object> prof = new HashMap<>(4);
			prof.put("email", this.profile.getEmail());
			prof.put("firstName", this.profile.getFirstName());
			prof.put("lastName", this.profile.getLastName());
			prof.put("avatarUrl", getAvatarUrl());
			out.put("profile", prof);
		}

		// finance (полностью)
		if (this.finance != null) {
			Map<String, Object> fin = new HashMap<>(2);
			fin.put("defaultAccountId", this.finance.getDefaultAccountId());
			fin.put("paymentMethodIds", this.finance.getPaymentMethodIds());
			out.put("finance", fin);
		}

		// system (meta + lastLoginAt)
		if (this.system != null) {
			Map<String, Object> sys = new HashMap<>(2);

			LifecycleMeta m = this.system.getMeta();
			if (m != null) {
				Map<String, Object> meta = new HashMap<>(3);
				meta.put("createdAt", m.getCreatedAt());
				meta.put("createdBy", m.getCreatedBy());
				meta.put("updatedAt", m.getUpdatedAt());
				meta.put("updatedBy", m.getUpdatedBy());
				meta.put("deletedAt", m.getDeletedAt());
				meta.put("deletedBy", m.getDeletedBy());
				sys.put("meta", meta);
			}

			sys.put("lastLoginAt", this.system.getLastLoginAt());
			out.put("system", sys);
		}

		return out;
	}
}
