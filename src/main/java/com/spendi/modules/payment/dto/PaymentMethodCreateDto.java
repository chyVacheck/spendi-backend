/**
 * @file PaymentMethodCreateDto.java
 * @module modules/payment/dto
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto;

/**
 * ! lib imports
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.modules.payment.types.EPaymentMethodType;

public class PaymentMethodCreateDto {

	@Valid
	@NotNull
	public Info info;
	@Valid
	public Details details;

	public static class Info {
		@NotNull
		public EPaymentMethodType type;
		@NotBlank
		@Size(max = 80)
		public String name;
		@NotBlank
		@Size(min = 3, max = 3)
		public String currency; // ISO 4217
		public Integer order;
		public List<String> tags;
	}

	public static class Details {
		public Card card;
		public Bank bank;
		public Wallet wallet;
	}

	public static class Card {
		public String brand;
		public String last4;
		public Integer expMonth;
		public Integer expYear;
	}

	public static class Bank {
		public String bankName;
		public String accountMasked;
	}

	public static class Wallet {
		public String provider;
		public String handle;
	}
}
