
/**
 * @file PaymentMethodInfo.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * ! lib imports
 */

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! java imports
 */
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodInfo {
	/**
	 * Тип метода оплаты.
	 */
	private EPaymentMethodType type; // BANK, CARD, WALLET
	/**
	 * Название метода оплаты.
	 */
	private String name;
	/**
	 * Валюта метода оплаты (ISO 4217).
	 */
	private String currency;
	/**
	 * Порядок отображения в пользовательском интерфейсе.
	 */
	@Builder.Default
	private int order = 1;
	/**
	 * Список тегов, связанных с методом оплаты.
	 */
	private Set<String> tags;
}