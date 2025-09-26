/**
 * @file IdParams.java
 * @module shared/dto
 */

package com.spendi.shared.dto;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdParams {
	/** 24-символьный hex как у Mongo ObjectId. */
	@Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id must be 24 hex characters")
	private String id;
}
