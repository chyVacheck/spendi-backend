
/**
 * @file EPaymentMethodType.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * Перечисление типов метода оплаты.
 */
public enum EPaymentMethodType {
	/**
	 * Тип карты.
	 */
	CARD(CardDetails.class),
	/**
	 * Тип банковского счета.
	 */
	BANK(BankDetails.class),
	/**
	 * Тип электронного кошелька.
	 */
	WALLET(WalletDetails.class);

	private final Class<? extends PaymentMethodDetails> detailsClass;

	EPaymentMethodType(Class<? extends PaymentMethodDetails> detailsClass) {
		this.detailsClass = detailsClass;
	}

	public Class<? extends PaymentMethodDetails> getDetailsClass() {
		return detailsClass;
	}
}