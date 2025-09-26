
/**
 * @file PaymentMethodCreateDetailsDto.java
 * @module modules/payment/dto/create
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto.create;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.Valid;

/**
 * DTO для деталей метода оплаты при создании.
 * 
 * В зависимости от info.type должен быть заполнен ровно один из блоков.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodCreateDetailsDto {
	/**
	 * Детали карты.
	 */
	@Valid
	private PaymentMethodCreateCardDto card;

	/**
	 * Детали банковского счета.
	 */
	@Valid
	private PaymentMethodCreateBankDto bank;

	/**
	 * Детали электронного кошелька.
	 */
	@Valid
	private PaymentMethodCreateWalletDto wallet;
}
