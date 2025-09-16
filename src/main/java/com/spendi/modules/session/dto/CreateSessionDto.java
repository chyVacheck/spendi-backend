
/**
 * @file CreateSessionDto.java
 * @module modules/session
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.session.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateSessionDto {

	/** 24-символьный hex как у Mongo ObjectId. */
	@Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id must be 24 hex characters")
	private String userId;
	private String ip;
	@NotNull
	private String userAgent;

	// --- getters ---

	public String getUserId() {
		return userId;
	}

	public String getIp() {
		return ip;
	}

	public String getUserAgent() {
		return userAgent;
	}

	// --- setters ---

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}
