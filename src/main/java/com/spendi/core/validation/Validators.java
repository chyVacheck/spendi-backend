
/**
 * @file Validators.java
 * @module core/validators
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.validation;

/**
 * ! lib imports
 */
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public final class Validators {
	private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
	private static final Validator VALIDATOR = FACTORY.getValidator();

	private Validators() {
	}

	public static Validator get() {
		return VALIDATOR;
	}
}
