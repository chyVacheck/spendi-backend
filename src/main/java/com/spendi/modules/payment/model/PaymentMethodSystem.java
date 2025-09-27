
/**
 * @file PaymentMethodSystem.java
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
 * ! my imports
 */
import com.spendi.shared.model.meta.LifecycleMeta;

/**
 * Системная информация о методе оплаты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodSystem {

	/**
	 * Статус метода оплаты.
	 */
	private EPaymentMethodStatus status; // ACTIVE, INACTIVE, DELETED
	/**
	 * Метаданные метода оплаты.
	 */
	private LifecycleMeta meta;
}