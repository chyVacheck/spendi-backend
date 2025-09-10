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
	public String email;

	@NotBlank
	public String password;
}
