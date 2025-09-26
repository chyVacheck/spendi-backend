/**
 * @file PresentFieldValueExtractor.java
 * @module core/validation
 * @description ValueExtractor для PresentField.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.validation;

/**
 * ! lib imports
 */
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * ! my imports
 */
import com.spendi.core.types.PresentField;

public class PresentFieldValueExtractor implements ValueExtractor<PresentField<@ExtractedValue ?>> {
	@Override
	public void extractValues(PresentField<?> original, ValueReceiver receiver) {
		if (original != null && original.isValue()) {
			receiver.value(null, original.get()); // валидируем именно VALUE, null не валидируем
		}
	}
}