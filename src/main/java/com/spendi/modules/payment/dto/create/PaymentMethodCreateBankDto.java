
/**
 * @file PaymentMethodCreateBankDto.java
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
 * DTO: детали БАНКОВСКОГО СЧЁТА при создании метода оплаты. Валидируется только когда выбран type=BANK.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodCreateBankDto {

	/**
	 * Название банка.
	 */
	@NotBlank(message = "Bank name is required")
	@Size(max = 80, message = "Bank name must be <= 80 chars")
	private String bankName;

	/**
	 * Маскированный номер счёта (формат свободный, но обязателен).
	 */
	@NotBlank(message = "Account masked is required")
	@Size(max = 64, message = "Account masked must be <= 64 chars")
	private String accountMasked;
}