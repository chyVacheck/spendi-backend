/**
 * @file PaymentMethodOrderDto.java
 * @module modules/payment/dto
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.NotNull;

public class PaymentMethodOrderDto {
	@NotNull
	public int order;
}
