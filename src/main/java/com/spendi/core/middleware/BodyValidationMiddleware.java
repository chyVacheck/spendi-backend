
/**
 * @file BodyValidationMiddleware.java
 * @module core/middleware
 * 
 * @see BaseMiddleware
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.middleware;

/**
 * ! lib imports
 */
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.exceptions.ValidationException;
import com.spendi.core.json.Jsons;
import com.spendi.core.validation.Validators;
import com.spendi.core.exceptions.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Миддлвара, которая:
 * - берёт из контекста RAW_JSON (JsonBodyParserMiddleware должен идти раньше),
 * - конвертирует JSON в DTO (T),
 * - валидирует через Jakarta Bean Validation,
 * - кладёт валидный DTO в контекст под ключом VALID_BODY
 * - при ошибках кидает ValidationException с map вида "field.path" ->
 * "message".
 */
public final class BodyValidationMiddleware<T> extends BaseMiddleware {

	private final Class<T> dtoClass;
	private final Validator validator;

	private BodyValidationMiddleware(
			Class<T> dtoClass) {
		super(BodyValidationMiddleware.class.getSimpleName() + "<" + dtoClass.getSimpleName() + ">");
		this.dtoClass = dtoClass;
		this.validator = Validators.get();
	}

	/** Фабрика по умолчанию. */
	public static <T> BodyValidationMiddleware<T> of(
			Class<T> dtoClass) {
		return new BodyValidationMiddleware<>(dtoClass);
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		// Забираем RAW_JSON, который положил JsonBodyParserMiddleware
		JsonNode json = ctx.getAttr(RequestAttr.RAW_JSON, JsonNode.class);
		if (json == null) {
			// Если кто-то забыл поставить парсер раньше — считаем это ошибкой программиста
			throw new ValidationException(
					"JSON not parsed. Place JsonBodyParserMiddleware before BodyValidationMiddleware.",
					Map.of(), Map.of());
		}

		try {
			// JSON → DTO
			final T dto = Jsons.mapper().treeToValue(json, dtoClass);

			// Jakarta Bean Validation
			final Set<ConstraintViolation<T>> violations = validator.validate(dto);
			if (!violations.isEmpty()) {
				Map<String, String> fieldErrors = new HashMap<>(violations.size());
				for (ConstraintViolation<T> v : violations) {
					String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
					fieldErrors.put("body." + path, v.getMessage());
				}
				throw new ValidationException("Body validation failed", fieldErrors, Map.of());
			}

			// Кладём валидный DTO в контекст под стандартным ключом
			ctx.setAttr(RequestAttr.VALID_BODY, dto);

		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			// на случай несовпадения типов при treeToValue
			String path = (e.getPath() != null && !e.getPath().isEmpty())
					? e.getPathReference()
					: "$";
			throw new JsonMappingException("JSON mapping error", Map.of(
					"path", path,
					"message", e.getOriginalMessage()));
		} catch (ValidationException ve) {
			throw ve; // уже собрали fieldErrors
		} catch (Exception e) {
			throw new ValidationException("Unable to validate body", Map.of(), Map.of("message", e.getMessage()));
		}

		chain.next();
	}
}
