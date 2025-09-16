
/**
 * @file UserEntity.java
 * @module modules/user
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spendi.core.utils.CryptoUtils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEntity {

	public ObjectId id; // _id

	@NotNull
	public Profile profile;

	@NotNull
	public Security security;

	@NotNull
	public Finance finance;

	@NotNull
	public System system;

	// --- blocks ---

	public static class Profile {
		@Email
		@NotBlank
		public String email;

		@Size(max = 80)
		public String firstName;

		@Size(max = 80)
		public String lastName;

		public String avatarFileId; // nullable, id of stored file
	}

	public static class Security {
		@NotBlank
		public String passwordHash; // хранится хэш пароля
	}

	public static class Finance {
		// ссылки на связанные сущности по ObjectId (в hex-строке)
		public String defaultAccountId; // nullable
		public Integer accountsCount; // денорм для быстрого вывода
		public List<String> paymentMethodIds; // список id методов оплаты
	}

	public static class System {
		@NotNull
		public Meta meta;
	}

	public static class Meta {
		public Instant createdAt; // устанавливаем в коде при вставке
		public Instant updatedAt; // обновляем в коде при апдейтах
		public Instant lastLoginAt; // nullable
	}

	// --- helpers ---

	public String getEmail() {
		return this.profile.email;
	}

	public String getAvatarUrl() {
		return "/users/" + this.id.toHexString() + "/avatar";
	}

	public boolean comparePassword(String password) {
		return CryptoUtils.verifyPassword(password, this.security.passwordHash);
	}

	public Optional<String> getAvatarFileIdOptional() {
		if (this.profile == null || this.profile.avatarFileId == null || this.profile.avatarFileId.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(this.profile.avatarFileId);
	}

	public boolean hasAvatar() {
		return this.profile.avatarFileId != null && !this.profile.avatarFileId.isBlank();
	}

	public Object getPublicData() {
		Map<String, Object> out = new HashMap<>(8);

		out.put("id", this.id.toHexString());

		// profile
		Map<String, Object> prof = new HashMap<>(5);
		prof.put("email", this.profile.email);
		prof.put("firstName", this.profile.firstName);
		prof.put("lastName", this.profile.lastName);
		boolean hasAvatar = this.hasAvatar();
		prof.put("hasAvatar", hasAvatar);
		if (hasAvatar) {
			prof.put("avatarUrl", getAvatarUrl());
		}
		out.put("profile", prof);

		// system meta only
		Map<String, Object> meta = new HashMap<>(3);
		meta.put("createdAt", this.system.meta.createdAt);
		meta.put("updatedAt", this.system.meta.updatedAt);
		meta.put("lastLoginAt", this.system.meta.lastLoginAt);
		out.put("system", Map.of("meta", meta));

		return out;
	}

	// Полные данные для текущего пользователя (включая финансы), без security
	public Object getPrivateData() {
		Map<String, Object> out = new HashMap<>(10);

		out.put("id", this.id.toHexString());

		// profile
		Map<String, Object> prof = new HashMap<>(6);
		prof.put("email", this.profile.email);
		prof.put("firstName", this.profile.firstName);
		prof.put("lastName", this.profile.lastName);
		boolean hasAvatar = this.hasAvatar();
		prof.put("hasAvatar", hasAvatar);
		if (hasAvatar) {
			prof.put("avatarUrl", getAvatarUrl());
		}
		out.put("profile", prof);

		// finance full
		Map<String, Object> fin = new HashMap<>(3);
		fin.put("defaultAccountId", this.finance.defaultAccountId);
		fin.put("accountsCount", this.finance.accountsCount);
		fin.put("paymentMethodIds", this.finance.paymentMethodIds);
		out.put("finance", fin);

		// system meta only
		Map<String, Object> meta = new HashMap<>(3);
		meta.put("createdAt", this.system.meta.createdAt);
		meta.put("updatedAt", this.system.meta.updatedAt);
		meta.put("lastLoginAt", this.system.meta.lastLoginAt);
		out.put("system", Map.of("meta", meta));

		return out;
	}
}
