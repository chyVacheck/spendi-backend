
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
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private Jsons() {
	}

	public static ObjectMapper mapper() {
		return MAPPER;
	}
}
