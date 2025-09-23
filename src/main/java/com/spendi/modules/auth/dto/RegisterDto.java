
/**
 * @file RegisterDto.java
 * @module modules/auth/dto
 * @description DTO for user login request.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth.dto;

import com.spendi.core.utils.StringUtils;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDto {
	@Email
	@NotBlank
	private String email;

	@Size(min = 3, max = 80)
	@NotBlank
	private String firstName;

	@Size(min = 3, max = 80)
	@NotBlank
	private String lastName;

	@NotBlank
	@Size(min = 8, max = 128)
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
