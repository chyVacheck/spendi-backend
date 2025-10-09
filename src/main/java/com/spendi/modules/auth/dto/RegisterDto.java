
/**
 * @file RegisterDto.java
 * @module modules/auth/dto
 * @description DTO for user login request.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ! my imports
 */
import com.spendi.core.utils.StringUtils;

public class RegisterDto {
	@Email(message = "Email is not valid")
	@NotBlank(message = "Email is required")
	private String email;

	@Size(min = 3, max = 80, message = "First name must be between 3 and 80 characters")
	@NotBlank(message = "First name is required")
	private String firstName;

	@Size(min = 3, max = 80, message = "Last name must be between 3 and 80 characters")
	@NotBlank(message = "Last name is required")
	private String lastName;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	private String password;

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

	public String getPassword() {
		return password;
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

	public void setPassword(String password) {
		this.password = password;
	}
}
