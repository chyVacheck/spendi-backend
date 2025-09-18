
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
import jakarta.validation.Path;
import jakarta.validation.Validator;
import com.spendi.core.validation.Validators;
import jakarta.validation.ElementKind;
import com.fasterxml.jackson.databind.JsonNode;

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
import com.spendi.core.json.Jsons;
import com.spendi.core.exceptions.ValidationException;
import com.spendi.core.exceptions.JsonMappingException;

/**
 * Миддлвара, которая: - берёт из контекста RAW_JSON (JsonBodyParserMiddleware должен идти раньше), - конвертирует JSON
 * в DTO (T), - валидирует через Jakarta Bean Validation, - кладёт валидный DTO в контекст под ключом VALID_BODY - при
 * ошибках кидает ValidationException с map вида "field.path" -> "message".
 */
public final class BodyValidationMiddleware<T> extends BaseMiddleware {

	private static final String ROOT_PREFIX = "body"; // единый префикс для всех ошибок тела
	private final Class<T> dtoClass;
	private final Validator validator;

	private BodyValidationMiddleware(Class<T> dtoClass) {
		super(BodyValidationMiddleware.class.getSimpleName() + "<" + dtoClass.getSimpleName() + ">");
		this.dtoClass = dtoClass;
		this.validator = Validators.get();
	}

	/** Фабрика по умолчанию. */
	public static <T> BodyValidationMiddleware<T> of(Class<T> dtoClass) {
		return new BodyValidationMiddleware<>(dtoClass);
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		// Забираем RAW_JSON, который положил JsonBodyParserMiddleware
		JsonNode json = ctx.getAttr(RequestAttr.RAW_JSON, JsonNode.class);
		if (json == null) {
			// Если кто-то забыл поставить парсер раньше — считаем это ошибкой программиста
			throw new ValidationException(
					"JSON not parsed. Place JsonBodyParserMiddleware before BodyValidationMiddleware.", Map.of(),
					Map.of());
		}

		final T dto;
		try {
			// Jackson: JsonNode -> DTO
			dto = Jsons.mapper().treeToValue(json, dtoClass);
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			// Невалидный синтаксис JSON
			throw new JsonMappingException("JSON parse error", Map.of("path", "$", "message", e.getOriginalMessage()));
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			// Несоответствие типов/структур при маппинге
			final String path = (e.getPath() != null && !e.getPath().isEmpty()) ? e.getPathReference() : "$";
			throw new JsonMappingException("JSON mapping error",
					Map.of("path", path, "message", e.getOriginalMessage()));
		} catch (Exception e) {
			// Непредвиденные ошибки маппинга
			throw new JsonMappingException("JSON mapping error", Map.of("path", "$", "message", e.getMessage()));
		}

		// Jakarta Bean Validation
		final Set<ConstraintViolation<T>> violations = validator.validate(dto);

		if (!violations.isEmpty()) {
			Map<String, String> fieldErrors = new HashMap<>(violations.size());

			for (ConstraintViolation<T> v : violations) {
				final String dotPath = toDotPath(v.getPropertyPath());
				final String key = dotPath.isBlank() ? ROOT_PREFIX : (ROOT_PREFIX + "." + dotPath);
				fieldErrors.put(key, safeMessage(v.getMessage()));
			}
			throw new ValidationException("Body validation failed", fieldErrors, Map.of());
		}

		// Кладём валидный DTO в контекст под стандартным ключом
		ctx.setAttr(RequestAttr.VALID_BODY, dto);

		chain.next();
	}

	/**
	 * Преобразует javax/jakarta.validation.Path в читаемый путь с точками и индексами, например: data.aliases[2],
	 * items[0].price, mapValues["en"].
	 *
	 * Логика: - PROPERTY узлы -> добавляем имя ("data", "aliases", "price"). - CONTAINER_ELEMENT + isInIterable ->
	 * добавляем [index] или ["key"]. - Для класс-уровневых ограничений path обычно пустой -> вернём "" (будет выведено
	 * как "body").
	 */
	private static String toDotPath(Path path) {
		if (path == null)
			return "";
		final StringBuilder out = new StringBuilder();
		// String lastSegment = null; // последний добавленный property/segment

		for (Path.Node node : path) {
			final ElementKind kind = node.getKind();

			switch (kind) {
			case PROPERTY: {
				final String name = node.getName();
				if (name != null && !name.isBlank()) {
					if (out.length() > 0)
						out.append('.');
					out.append(name);
					// lastSegment = name;
				}
				break;
			}
			case CONTAINER_ELEMENT: {
				// Узел элемента коллекции/массива/карты. Имя type parameter нам не нужно.
				// Нас интересует только индекс/ключ, если узел внутри iterable.
				if (node.isInIterable()) {
					if (node.getIndex() != null) {
						// Список/массив: aliases[2]
						out.append('[').append(node.getIndex()).append(']');
					} else if (node.getKey() != null) {
						// Map: i18n["en"]
						out.append('[').append('"').append(String.valueOf(node.getKey())).append('"').append(']');
					} else {
						// Iterable без индекса/ключа — редко, но на всякий случай.
						out.append("[]");
					}
				}
				break;
			}
			case BEAN:
			case METHOD:
			case CONSTRUCTOR:
			case PARAMETER:
			case CROSS_PARAMETER:
			case RETURN_VALUE:
			default:
				// Для BEAN/CLASS-level ничего не добавляем — путь останется пустым,
				// и ошибка будет повешена на "body".
				break;
			}
		}

		return out.toString();
	}

	/** Безопасно нормализуем сообщение (на случай null). */
	private static String safeMessage(String msg) {
		return (msg == null || msg.isBlank()) ? "Invalid value" : msg;
	}

}
