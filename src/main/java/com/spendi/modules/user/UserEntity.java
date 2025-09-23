
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

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEntity {

	// * === === === root === === ===

	/**
	 * Unique user identifier (_id).
	 */
	private ObjectId id;

	/**
	 * Profile block (public user info).
	 */
	private Profile profile;

	/**
	 * Security block (sensitive data).
	 */
	private Security security;

	/**
	 * Finance block (links to financial entities).
	 */
	private Finance finance;

	/**
	 * System block (system/meta info).
	 */
	private System system;

	// * === === === blocks === === ===

	/**
	 * Profile data.
	 */
	public static class Profile {

		/**
		 * Email address (normalized to lower-case on input at DTO level).
		 */
		private String email;

		/**
		 * First name (capitalized at DTO level).
		 */
		private String firstName;

		/**
		 * Last name (capitalized at DTO level).
		 */
		private String lastName;

		/**
		 * Optional avatar file id (nullable).
		 */
		private ObjectId avatarFileId;

		// ? === ==== === getters === === ===

		/**
		 * @return email
		 */
		public String getEmail() {
			return email;
		}

		/**
		 * @return firstName
		 */
		public String getFirstName() {
			return firstName;
		}

		/**
		 * @return lastName
		 */
		public String getLastName() {
			return lastName;
		}

		/**
		 * @return avatarFileId (nullable)
		 */
		public ObjectId getAvatarFileId() {
			return avatarFileId;
		}

		// ? === ==== === setters === === ===

		/**
		 * @param email email to set
		 */
		public void setEmail(String email) {
			this.email = email;
		}

		/**
		 * @param firstName firstName to set
		 */
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		/**
		 * @param lastName lastName to set
		 */
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		/**
		 * @param avatarFileId avatar file id to set (nullable)
		 */
		public void setAvatarFileId(ObjectId avatarFileId) {
			this.avatarFileId = avatarFileId;
		}

		/**
		 * Set avatar file id to null.
		 */
		public void setAvatarFileId() {
			this.avatarFileId = null;
		}

		/**
		 * @param avatarFileId avatar file id to set (nullable)
		 */
		public void setAvatarFileId(String avatarFileId) {
			this.avatarFileId = new ObjectId(avatarFileId);
		}
	}

	/**
	 * Security data (sensitive).
	 */
	public static class Security {

		/**
		 * Password hash (never expose).
		 */
		private String passwordHash;

		// ? === === === getters === === ===

		/**
		 * @return passwordHash
		 */
		public String getPasswordHash() {
			return passwordHash;
		}

		// ? === === === setters === === ===

		/**
		 * @param passwordHash hash to set
		 */
		public void setPasswordHash(String passwordHash) {
			this.passwordHash = passwordHash;
		}
	}

	/**
	 * Finance data.
	 */
	public static class Finance {

		/**
		 * Default account id (nullable).
		 */
		private ObjectId defaultAccountId;

		/**
		 * Set of user's payment method ids (never null by convention).
		 */
		private Set<ObjectId> paymentMethodIds;

		// ? === === === getters === === ===

		/**
		 * @return defaultAccountId (nullable)
		 */
		public ObjectId getDefaultAccountId() {
			return defaultAccountId;
		}

		/**
		 * @return unmodifiable view of paymentMethodIds (never null, may be empty)
		 */
		public Set<ObjectId> getPaymentMethodIds() {
			return paymentMethodIds == null ? Set.of() : Set.copyOf(paymentMethodIds);
		}

		// ? === === === setters === === ===

		/**
		 * @param defaultAccountId default account id to set (nullable)
		 */
		public void setDefaultAccountId(ObjectId defaultAccountId) {
			this.defaultAccountId = defaultAccountId;
		}

		/**
		 * @param paymentMethodIds set of payment method ids (if null → stored as empty set)
		 */
		public void setPaymentMethodIds(Set<ObjectId> paymentMethodIds) {
			this.paymentMethodIds = (paymentMethodIds == null) ? Set.of() : Set.copyOf(paymentMethodIds);
		}
	}

	/**
	 * System block (meta).
	 */
	public static class System {

		/**
		 * Meta info (timestamps).
		 */
		private Meta meta;

		// ? === === === getters === === ===

		/**
		 * @return meta
		 */
		public Meta getMeta() {
			return meta;
		}

		// ? === === === setters === === ===

		/**
		 * @param meta meta to set
		 */
		public void setMeta(Meta meta) {
			this.meta = meta;
		}
	}

	/**
	 * Meta timestamps.
	 */
	public static class Meta {

		/**
		 * Creation timestamp (set at insert).
		 */
		private Instant createdAt;

		/**
		 * Update timestamp (updated on each update).
		 */
		private Instant updatedAt;

		/**
		 * Last login timestamp (nullable).
		 */
		private Instant lastLoginAt;

		// ? === === === getters === === ===

		/**
		 * @return createdAt
		 */
		public Instant getCreatedAt() {
			return createdAt;
		}

		/**
		 * @return updatedAt
		 */
		public Instant getUpdatedAt() {
			return updatedAt;
		}

		/**
		 * @return lastLoginAt (nullable)
		 */
		public Instant getLastLoginAt() {
			return lastLoginAt;
		}

		// ? === === === setters === === ===

		/**
		 * @param createdAt to set
		 */
		public void setCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
		}

		/**
		 * @param updatedAt to set
		 */
		public void setUpdatedAt(Instant updatedAt) {
			this.updatedAt = updatedAt;
		}

		/**
		 * @param lastLoginAt to set (nullable)
		 */
		public void setLastLoginAt(Instant lastLoginAt) {
			this.lastLoginAt = lastLoginAt;
		}
	}

	// ? === === === getters === === ===

	/**
	 * @return user id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @return profile block
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * @return security block
	 */
	public Security getSecurity() {
		return security;
	}

	/**
	 * @return finance block
	 */
	public Finance getFinance() {
		return finance;
	}

	/**
	 * @return system block
	 */
	public System getSystem() {
		return system;
	}

	// ? === === === setters === === ===

	/**
	 * @param id user id to set
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

	/**
	 * @param id hex string id to set (nullable → null)
	 */
	public void setId(String id) {
		this.id = (id == null) ? null : new ObjectId(id);
	}

	/**
	 * @param profile to set
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	/**
	 * @param security to set
	 */
	public void setSecurity(Security security) {
		this.security = security;
	}

	/**
	 * @param finance to set
	 */
	public void setFinance(Finance finance) {
		this.finance = finance;
	}

	/**
	 * @param system to set
	 */
	public void setSystem(System system) {
		this.system = system;
	}

	// * === === === helpers === === ===

	public String getEmail() {
		return this.profile.getEmail();
	}

	/**
	 * @return avatar URL for current user or null if id is not set
	 */
	public String getAvatarUrl() {
		return "/users/" + this.id.toHexString() + "/avatar";
	}

	/**
	 * Verifies plain password against stored hash.
	 *
	 * @param password plain password
	 * @return true if matches, else false
	 */
	public boolean comparePassword(String password) {
		return CryptoUtils.verifyPassword(password, this.security.passwordHash);
	}

	/**
	 * @return true if avatar is set
	 */
	public boolean hasAvatar() {
		return this.profile.avatarFileId != null;
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
		Map<String, Object> fin = new HashMap<>(2);
		fin.put("defaultAccountId", this.finance.defaultAccountId);
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

	/**
	 * @return optional avatar file id
	 */
	public Optional<ObjectId> getAvatarFileIdOptional() {
		ObjectId aid = this.profile.getAvatarFileId();
		if (this.hasAvatar()) {
			return Optional.of(aid);
		}
		return Optional.empty();
	}

	/**
	 * Checks ownership of payment method by its hex id.
	 *
	 * @param pmIdHex payment method id hex
	 * @return true if user has it
	 */
	public boolean hasPaymentMethod(String pmIdHex) {
		return this.finance.paymentMethodIds.contains(new ObjectId(pmIdHex));
	}

	/**
	 * Checks ownership of payment method by its hex id.
	 *
	 * @param pmId payment method ObjectId
	 * @return true if user has it
	 */
	public boolean hasPaymentMethod(ObjectId pmId) {
		return this.finance.paymentMethodIds.contains(pmId);
	}
}
