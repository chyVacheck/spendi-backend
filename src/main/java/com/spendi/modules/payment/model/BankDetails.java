
/**
 * @file BankDetails.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Вложенный класс для деталей банковского счета.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class BankDetails implements PaymentMethodDetails {
	/**
	 * Название банка.
	 */
	private String bankName;
	/**
	 * Маскированный номер счета.
	 */
	private String accountMasked; // e.g. "UA** **** 1234"
}
