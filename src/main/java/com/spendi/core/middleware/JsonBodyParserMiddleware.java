
/**
 * @file JsonBodyParserMiddleware.java
 * @module core/middleware
 * 
 * @see BaseMiddleware
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.middleware;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.exceptions.BadRequestException;
import com.spendi.core.exceptions.InvalidJsonException;
import com.spendi.core.exceptions.JsonMappingException;
import com.spendi.core.json.Jsons;
import com.spendi.core.utils.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Миддлвара для парсинга JSON-тела запроса.
 * - Проверяет Content-Type: application/json
 * - Читает body как byte[]
 * - Парсит в JsonNode
 * - Кладёт в контекст: RAW_BODY (byte[]), RAW_JSON (JsonNode)
 */
public class JsonBodyParserMiddleware extends BaseMiddleware {

	public JsonBodyParserMiddleware() {
		super(JsonBodyParserMiddleware.class.getSimpleName());

	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		final String ct = StringUtils.lowerOrNull(ctx.req().header("Content-Type"));
		if (ct == null || !ct.startsWith("application/json")) {
			// Неподдерживаемый/отсутствующий тип контента — это "плохой запрос"
			throw new BadRequestException("Content-Type", "Expected application/json");
		}

		// Body (Optional<byte[]> → byte[] | null)
		final byte[] body = ctx.req().bodyAsBytes().orElse(null);
		if (body == null || body.length == 0) {
			// Пустое тело — тоже "плохой запрос"
			throw new BadRequestException("body", "Empty payload");
		}

		final JsonNode node;
		try {
			node = Jsons.mapper().readTree(body);
		} catch (JsonParseException e) {
			// Синтаксис JSON поломан
			throw new InvalidJsonException("Invalid JSON syntax", java.util.Map.of(
					"message", e.getOriginalMessage(),
					"location", e.getLocation() != null ? e.getLocation().toString() : null));

		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			// Структура/типы не соответствуют JSON-дереву (редко при readTree, чаще при
			// readValue)
			String path = e.getPath() != null && !e.getPath().isEmpty()
					? e.getPathReference()
					: "$";
			throw new JsonMappingException("JSON mapping error", java.util.Map.of(
					"path", path,
					"message", e.getOriginalMessage()));

		} catch (Exception e) {
			// Непредвиденная ошибка парсинга
			throw new InvalidJsonException("Unable to parse JSON", java.util.Map.of(
					"message", e.getMessage()));
		}

		ctx.setAttr(RequestAttr.RAW_BODY, body);
		ctx.setAttr(RequestAttr.RAW_JSON, node);
		chain.next();
	}

}
