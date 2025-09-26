
/**
 * @file UserCreateCommand.java
 * @module modules/user/command
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.cmd;

/**
 * ! my imports
 */
import com.spendi.core.utils.StringUtils;

/**
 * DTO для создания пользователя. Валидируется BodyValidationMiddleware (Jakarta Bean Validation).
 */
public class UserCreateCmd {

	private ProfileBlock profile;

	private SecurityBlock security;

	// --- blocks ---

	public static class ProfileBlock {
		private String email;

		private String firstName;

		private String lastName;

		// --- getters ---

		public String getEmail() {
			return email;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		// --- setters ---

		public void setEmail(String email) {
			this.email = email.toLowerCase();
		}

		public void setFirstName(String firstName) {
			this.firstName = StringUtils.capitalize(firstName);
		}

		public void setLastName(String lastName) {
			this.lastName = StringUtils.capitalize(lastName);
		}
	}

	public static class SecurityBlock {
		/** Пароль в открытом виде — будет захеширован в сервисе. */
		private String password;

		// --- getters ---

		public String getPassword() {
			return password;
		}

		// --- setters ---

		public void setPassword(String password) {
			this.password = password;
		}
	}

	// --- getters ---

	public ProfileBlock getProfile() {
		return profile;
	}

	public SecurityBlock getSecurity() {
		return security;
	}

	// --- setters ---

	public void setProfile(ProfileBlock profile) {
		this.profile = profile;
	}

	public void setSecurity(SecurityBlock security) {
		this.security = security;
	}
}
