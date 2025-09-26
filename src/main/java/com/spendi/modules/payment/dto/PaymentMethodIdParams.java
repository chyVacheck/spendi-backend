/**
 * @file PaymentMethodIdParams.java
 * @module modules/payment/dto
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto;

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
public class PaymentMethodIdParams {
	/** 24-символьный hex как у Mongo ObjectId. */
	@Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "pmId must be 24 hex characters")
	private String pmId;
}
