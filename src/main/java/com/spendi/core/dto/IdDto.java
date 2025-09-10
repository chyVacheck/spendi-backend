/**
 * @file IdDto.java
 * @module core/dto
 */

package com.spendi.core.dto;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdDto {
	/** 24-символьный hex как у Mongo ObjectId. */
	@Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id must be 24 hex characters")
	public String id;

	public IdDto() {
	}

	public IdDto(String id) {
		this.id = id;
	}

	public static IdDto of(String id) {
		return new IdDto(id);
	}
}
