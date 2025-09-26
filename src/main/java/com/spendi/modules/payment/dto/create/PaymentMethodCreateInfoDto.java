
/**
 * @file PaymentMethodCreateInfoDto.java
 * @module modules/payment/dto/create
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto.create;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ! java imports
 */
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.modules.payment.model.EPaymentMethodType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodCreateInfoDto {
	/**
	 * Тип метода оплаты.
	 */
	@NotNull
	private EPaymentMethodType type;

	/**
	 * Название метода оплаты.
	 */
	@NotBlank
	@Size(max = 80)
	private String name;

	/**
	 * Валюта (ISO 4217).
	 */
	@NotBlank
	@Size(min = 3, max = 3)
	private String currency; // ISO 4217

	/**
	 * Порядок отображения.
	 */
	@Builder.Default
	private int order = 1;

	/**
	 * Список тегов.
	 */
	@Builder.Default
	private List<String> tags = List.of();

	/**
	 * Возвращает тип метода оплаты.
	 * 
	 * @return Тип метода оплаты.
	 */
	public EPaymentMethodType getType() {
		return type;
	}

}
