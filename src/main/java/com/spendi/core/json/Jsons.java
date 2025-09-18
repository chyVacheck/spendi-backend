
/**
 * @file Jsons.java
 * @module core/json
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.json;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Jsons {
	private static final ObjectMapper MAPPER = new ObjectMapper() // делаем mapper на основе ObjectMapper
			.registerModule(new JavaTimeModule()) // подключаем модуль для работы с датами
			.registerModule(new EnumFriendlyModule()) // подключаем модуль для работы с enum
			.setSerializationInclusion(JsonInclude.Include.NON_NULL) // исключаем из сериализации null значения
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private Jsons() {}

	public static ObjectMapper mapper() {
		return MAPPER;
	}
}
