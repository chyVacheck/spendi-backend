/**
 * @file PaymentMethodIdParams.java
 * @module modules/payment/dto
 */

package com.spendi.modules.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodIdParams {
	/** 24-символьный hex как у Mongo ObjectId. */
	@Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "pmId must be 24 hex characters")
	public String pmId;
}
