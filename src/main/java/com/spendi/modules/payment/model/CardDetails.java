
/**
 * @file CardDetails.java
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
 * Вложенный класс для деталей карты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CardDetails implements PaymentMethodDetails {
	/**
	 * Бренд карты.
	 */
	private String brand;
	/**
	 * Последние 4 цифры карты.
	 */
	private String last4;
	/**
	 * Месяц истечения срока действия карты.
	 */
	private Integer expMonth; // 1-12
	/**
	 * Год истечения срока действия карты.
	 */
	private Integer expYear; // YYYY

}