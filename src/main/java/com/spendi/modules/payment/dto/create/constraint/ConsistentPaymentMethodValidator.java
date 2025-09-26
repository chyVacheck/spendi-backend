
/**
 * @file ConsistentPaymentMethodValidator.java
 * @module modules/payment/dto/create/constraint
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto.create.constraint;

/**
 * Валидатор согласованности деталей с типом.
 *
 * Правила:
 * - Ровно один из блоков details.{card,bank,wallet} должен быть заполнен (≠ null).
 * - Этот единственный блок должен соответствовать info.type.
 * - Остальные блоки должны быть null.
 *
 * Примечание:
 * - @Valid на полях details.* уже обеспечит проверку «листьев», когда выбранный блок не null.
 * - Здесь мы фокусируемся на согласованности и количестве заполненных блоков.
 */
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDetailsDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDto; // предполагаемый корневой DTO
import com.spendi.modules.payment.dto.create.PaymentMethodCreateInfoDto;
import com.spendi.modules.payment.model.EPaymentMethodType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConsistentPaymentMethodValidator
		implements ConstraintValidator<ConsistentPaymentMethod, PaymentMethodCreateDto> {

	@Override
	public boolean isValid(PaymentMethodCreateDto root, ConstraintValidatorContext ctx) {
		// Если сам объект null — доверяем другим аннотациям ловить это.
		if (root == null)
			return true;

		final PaymentMethodCreateInfoDto info = root.getInfo();
		final PaymentMethodCreateDetailsDto details = root.getDetails();

		// Если info/details null — пусть сработают @NotNull/@Valid на уровне полей, здесь молчим.
		if (info == null || details == null)
			return true;

		final EPaymentMethodType type = info.getType();
		// Если type отсутствует — отдельные аннотации сообщат об этом.
		if (type == null)
			return true;

		final boolean hasCard = details.getCard() != null;
		final boolean hasBank = details.getBank() != null;
		final boolean hasWallet = details.getWallet() != null;

		final int filledCount = (hasCard ? 1 : 0) + (hasBank ? 1 : 0) + (hasWallet ? 1 : 0);

		boolean ok = true;
		ctx.disableDefaultConstraintViolation();

		// Должен быть заполнен ровно один блок
		if (filledCount == 0) {
			ctx.buildConstraintViolationWithTemplate("Exactly one details block must be provided")
					.addPropertyNode("details").addConstraintViolation();
			ok = false;
		} else if (filledCount > 1) {
			ctx.buildConstraintViolationWithTemplate("Only one details block must be provided")
					.addPropertyNode("details").addConstraintViolation();
			ok = false;
		}

		// Соответствие блока выбранному типу
		switch (type) {
		case CARD -> {
			if (!hasCard) {
				ctx.buildConstraintViolationWithTemplate("details.card is required for type=CARD")
						.addPropertyNode("details").addPropertyNode("card").addConstraintViolation();
				ok = false;
			}
			// Запрещаем лишние блоки — на случай если filledCount==1 не сработал
			if (hasBank || hasWallet) {
				ctx.buildConstraintViolationWithTemplate("Only details.card must be provided for type=CARD")
						.addPropertyNode("details").addConstraintViolation();
				ok = false;
			}
		}
		case BANK -> {
			if (!hasBank) {
				ctx.buildConstraintViolationWithTemplate("details.bank is required for type=BANK")
						.addPropertyNode("details").addPropertyNode("bank").addConstraintViolation();
				ok = false;
			}
			if (hasCard || hasWallet) {
				ctx.buildConstraintViolationWithTemplate("Only details.bank must be provided for type=BANK")
						.addPropertyNode("details").addConstraintViolation();
				ok = false;
			}
		}
		case WALLET -> {
			if (!hasWallet) {
				ctx.buildConstraintViolationWithTemplate("details.wallet is required for type=WALLET")
						.addPropertyNode("details").addPropertyNode("wallet").addConstraintViolation();
				ok = false;
			}
			if (hasCard || hasBank) {
				ctx.buildConstraintViolationWithTemplate("Only details.wallet must be provided for type=WALLET")
						.addPropertyNode("details").addConstraintViolation();
				ok = false;
			}
		}
		// на случай расширения enum в будущем — по умолчанию считаем ок и отдаем проверку другим слоям
		default -> {
			/* no-op */ }
		}

		return ok;
	}
}