/**
 * @file LoginDto.java
 * @module modules/auth/dto
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginDto {
	@Email
	@NotBlank
	private String email;

	@NotBlank
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
