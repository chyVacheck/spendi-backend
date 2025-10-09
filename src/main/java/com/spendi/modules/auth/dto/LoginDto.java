/**
 * @file LoginDto.java
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

public class LoginDto {
	@Email(message = "Email is not valid")
	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	private String password;

	// --- getters ---

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	// --- setters ---

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
