
/**
 * @file EmployeeCreateDto.java
 * @module modules/employee/dto
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.dto;

/**
 * ! lib imports
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания сотрудника.
 * Валидируется BodyValidationMiddleware (Jakarta Bean Validation).
 */
public class UserCreateDto {

	@Valid
	@NotNull
	public ProfileBlock profile;

	@Valid
	@NotNull
	public SecurityBlock security;

	// --- blocks ---

	public static class ProfileBlock {
		@Email
		@NotBlank
		public String email;

		@Size(max = 80)
		@NotBlank
		public String firstName;

		@Size(max = 80)
		@NotBlank
		public String lastName;
	}

	public static class SecurityBlock {
		/** Пароль в открытом виде — будет захеширован в сервисе. */
		@NotBlank
		@Size(min = 8, max = 128)
		public String password;
	}
}
