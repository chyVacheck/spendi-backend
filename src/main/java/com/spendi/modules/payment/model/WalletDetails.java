
/**
 * @file WalletDetails.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * ! lib imports
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Вложенный класс для деталей электронного кошелька.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class WalletDetails implements PaymentMethodDetails {
	/**
	 * Провайдер электронного кошелька.
	 */
	private String provider; // PayPal/Revolut/...
	/**
	 * Идентификатор электронного кошелька.
	 */
	private String handle; // identifier/nickname
}