
/**
 * @file PaymentMethodCreateDto.java
 * @module modules/payment/dto/create
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto.create;

/**
 * ! lib imports
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! my imports
 */
import com.spendi.modules.payment.dto.create.constraint.ConsistentPaymentMethod;

/**
 * DTO для создания метода оплаты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConsistentPaymentMethod
public class PaymentMethodCreateDto {
	/**
	 * Информация о методе оплаты.
	 */
	@Valid
	@NotNull
	private PaymentMethodCreateInfoDto info;

	/**
	 * Детали метода оплаты.
	 */
	@Valid
	private PaymentMethodCreateDetailsDto details;

}
