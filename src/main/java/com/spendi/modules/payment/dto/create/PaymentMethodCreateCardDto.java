
/**
 * @file PaymentMethodCreateCardDto.java
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
 * DTO: детали КАРТЫ при создании метода оплаты. Валидируется только когда выбран type=CARD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodCreateCardDto {

	/**
	 * Бренд карты (например, Visa/Mastercard).
	 */
	@NotBlank(message = "Card brand is required")
	@Size(max = 40, message = "Card brand must be <= 40 chars")
	private String brand;

	/**
	 * Последние 4 цифры карты.
	 */
	@NotBlank(message = "Last4 is required")
	@Pattern(regexp = "^[0-9]{4}$", message = "Last4 must be exactly 4 digits")
	private String last4;

	/**
	 * Месяц истечения (1-12).
	 */
	@NotNull(message = "Expiration month is required")
	@Min(value = 1, message = "Expiration month must be between 1 and 12")
	@Max(value = 12, message = "Expiration month must be between 1 and 12")
	private Integer expMonth;

	/**
	 * Год истечения (YYYY). При желании в валидаторе можно дополнительно проверять, что не в прошлом.
	 */
	@NotNull(message = "Expiration year is required")
	@Min(value = 2000, message = "Expiration year must be >= 2000")
	private Integer expYear;
}