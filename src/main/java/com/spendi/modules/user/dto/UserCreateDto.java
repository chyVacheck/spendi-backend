
/**
 * @file EmployeeCreateDto.java
 * @module modules/employee/dto
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.dto;

import com.spendi.core.utils.StringUtils;

/**
 * ! lib imports
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания сотрудника. Валидируется BodyValidationMiddleware (Jakarta Bean Validation).
 */
public class UserCreateDto {

	@Valid
	@NotNull
	private ProfileBlock profile;

	@Valid
	@NotNull
	private SecurityBlock security;

	// --- blocks ---

	public static class ProfileBlock {
		@Email
		@NotBlank
		private String email;

		@Size(max = 80)
		@NotBlank
		private String firstName;

		@Size(max = 80)
		@NotBlank
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
		@NotBlank
		@Size(min = 8, max = 128)
		private String password;

		// --- getters ---

		public String getPassword() {
			return password;
		}

		// --- setters ---

		public void setPassword(String password) {
			this.password = password.toLowerCase();
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
