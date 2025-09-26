
/**
 * @file ConsistentPaymentMethod.java
 * @module modules/payment/dto/create/constraint
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.dto.create.constraint;

/**
 * Аннотация класс-уровневой проверки соответствия:
 * info.type ↔ details.{card|bank|wallet}
 */
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Валидация соответствия типа метода оплаты и его деталей.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ConsistentPaymentMethodValidator.class)
public @interface ConsistentPaymentMethod {

	/**
	 * Сообщение по умолчанию (будут добавляться точечные сообщения на поля).
	 */
	String message() default "Payment method type/details are inconsistent";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}