
/**
 * @file FriendlyEnumDeserializer.java
 * @module core/json/deser
 * @description Universal, user-friendly Jackson deserializer for enums.
 *
 * Features:
 * - Trims input, supports case-insensitive matching
 * - Converts spaces/dashes to underscores (e.g. "in-come" -> IN_COME)
 * - Optional aliases map (e.g. "in" -> INCOME)
 * - Throws InvalidFormatException with clear "Allowed: [..]" list
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.json.deser;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * ! java imports
 */
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FriendlyEnumDeserializer<E extends Enum<E>> extends StdScalarDeserializer<E> {

	private final Class<E> enumType;
	private final boolean ignoreCase;
	private final Function<String, String> normalizer;
	private final Map<String, E> aliases; // normalizedKey -> enum

	/**
	 * Default behavior: - ignoreCase = true - normalizer: trim + replace('-', '_') + replace(' ', '_') + UPPERCASE - no
	 * aliases
	 */
	public FriendlyEnumDeserializer(Class<E> enumType) {
		this(enumType, true, FriendlyEnumDeserializer::defaultNormalize, Map.of());
	}

	public FriendlyEnumDeserializer(Class<E> enumType, boolean ignoreCase, Function<String, String> normalizer,
			Map<String, E> aliases) {
		super(enumType);
		this.enumType = Objects.requireNonNull(enumType, "enumType");
		this.ignoreCase = ignoreCase;
		this.normalizer = Objects.requireNonNullElse(normalizer, FriendlyEnumDeserializer::defaultNormalize);
		this.aliases = normalizeAliases(aliases);
	}

	private static String defaultNormalize(String raw) {
		if (raw == null)
			return null;
		String s = raw.trim();
		if (s.isEmpty())
			return s;
		s = s.replace('-', '_').replace(' ', '_');
		return s.toUpperCase(Locale.ROOT);
	}

	private Map<String, E> normalizeAliases(Map<String, E> in) {
		if (in == null || in.isEmpty())
			return Map.of();
		Map<String, E> out = new HashMap<>(in.size());
		in.forEach((k, v) -> out.put(normalizer.apply(k), v));
		return Collections.unmodifiableMap(out);
	}

	@Override
	public E deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonToken token = p.currentToken();

		if (token == JsonToken.VALUE_STRING) {
			String raw = p.getValueAsString();
			String norm = normalizer.apply(raw);

			if (norm == null || norm.isBlank()) {
				return handleInvalid(raw, ctxt);
			}

			// Try alias first
			E byAlias = aliases.get(norm);
			if (byAlias != null)
				return byAlias;

			// Try direct valueOf or case-insensitive lookup
			try {
				if (ignoreCase) {
					for (E c : enumType.getEnumConstants()) {
						if (c.name().equalsIgnoreCase(norm)) {
							return c;
						}
					}
				}
				return Enum.valueOf(enumType, norm);
			} catch (IllegalArgumentException ex) {
				return handleInvalid(raw, ctxt);
			}
		}

		// If token is not a string → produce a clear error
		return handleInvalid(p.getText(), ctxt);
	}

	private E handleInvalid(String raw, DeserializationContext ctxt) throws IOException {
		String allowed = Arrays.stream(enumType.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
		String msg = "Invalid value '" + raw + "' for " + enumType.getSimpleName() + ". Allowed: [" + allowed + "].";
		throw JsonMappingException.from(ctxt, msg);
	}
}