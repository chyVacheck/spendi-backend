/**
 * @file PaymentMethodCreateCmd.java
 * @module modules/payment/command
 * @description Команда создания метода оплаты (формируется из валидного DTO).
 *
 * Содержит плоские поля info и один полиморфный details (Bank/Card/Wallet).
 * Предполагается, что соответствие type ↔ details уже проверено на уровне DTO-валидатора.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.cmd;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * ! my imports
 */
import com.spendi.modules.payment.model.PaymentMethodDetails;
import com.spendi.modules.payment.model.PaymentMethodInfo;

/**
 * Команда для создания нового метода оплаты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodCreateCmd {

	/**
	 * Основная информация о методе оплаты.
	 */
	private PaymentMethodInfo info;

	/**
	 * Полиморфные детали (CardDetails/BankDetails/WalletDetails).
	 */
	private PaymentMethodDetails details;
}
