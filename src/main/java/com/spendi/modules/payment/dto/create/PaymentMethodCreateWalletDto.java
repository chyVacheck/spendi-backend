
/**
 * @file PaymentMethodCreateWalletDto.java
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
import jakarta.validation.constraints.*;

/**
 * DTO: детали ЭЛЕКТРОННОГО КОШЕЛЬКА при создании метода оплаты. Валидируется только когда выбран type=WALLET.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodCreateWalletDto {

	/**
	 * Провайдер кошелька (PayPal/Revolut/...).
	 */
	@NotBlank(message = "Wallet provider is required")
	@Size(max = 40, message = "Provider must be <= 40 chars")
	private String provider;

	/**
	 * Идентификатор/ник кошелька.
	 */
	@NotBlank(message = "Wallet handle is required")
	@Size(max = 80, message = "Handle must be <= 80 chars")
	private String handle;
}