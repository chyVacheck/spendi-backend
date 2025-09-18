/**
 * @file EnumFriendlyModule.java
 * @module core/json
 * @description Jackson module registering user-friendly enum deserializers.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.json;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.spendi.core.json.deser.FriendlyEnumDeserializer;

/**
 * ! my imports
 */
import com.spendi.modules.payment.types.EPaymentMethodType;

public class EnumFriendlyModule extends SimpleModule {

	public EnumFriendlyModule() {
		// Регистрация enums для платежных методов
		addDeserializer(EPaymentMethodType.class, new FriendlyEnumDeserializer<>(EPaymentMethodType.class));
	}
}