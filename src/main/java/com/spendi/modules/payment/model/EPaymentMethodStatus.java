
/**
 * @file EPaymentMethodStatus.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * Перечисление статусов метода оплаты.
 */
public enum EPaymentMethodStatus {
	/**
	 * Активный статус.
	 */
	ACTIVE,
	/**
	 * Неактивный статус.
	 */
	INACTIVE,
	/**
	 * Удаленный статус.
	 */
	DELETED
}