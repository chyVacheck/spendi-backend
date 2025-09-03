
/**
 * @file QueryValidationMiddleware.java
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
import java.util.List;
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

/**
 * Валидирует query-параметры, биндя их в DTO и прогоняя через Jakarta
 * Validation.
 *
 * Пример:
 * this.get("/search", handler,
 * QueryValidationMiddleware.of(SearchQueryDto.class));
 */
public final class QueryValidationMiddleware<T> extends BaseMiddleware {

	private final Class<T> dtoClass;
	private final Validator validator = Validators.get();

	public QueryValidationMiddleware(Class<T> dtoClass) {
		super(QueryValidationMiddleware.class.getSimpleName() + "<" + dtoClass.getSimpleName() + ">");
		this.dtoClass = dtoClass;
	}

	public static <T> QueryValidationMiddleware<T> of(Class<T> dtoClass) {
		return new QueryValidationMiddleware<>(dtoClass);
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		// 1) Берём все query-параметры: Map<String, List<String>>
		Map<String, List<String>> raw = ctx.req().queryParams();

		// 2) Приводим к Map<String, Object>:
		// - если значений 1 → String
		// - если значений >1 → List<String>
		Map<String, Object> source = new HashMap<>(raw.size());
		for (Map.Entry<String, List<String>> e : raw.entrySet()) {
			List<String> values = e.getValue();
			if (values == null || values.isEmpty())
				continue;
			source.put(e.getKey(), values.size() == 1 ? values.get(0) : values);
		}

		// 3) Конвертим в DTO
		final T dto = Jsons.mapper().convertValue(source, dtoClass);

		// 4) Валидируем
		Set<ConstraintViolation<T>> violations = validator.validate(dto);
		if (!violations.isEmpty()) {
			Map<String, String> fieldErrors = new HashMap<>(violations.size());
			violations.forEach(v -> {
				String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
				fieldErrors.put("query." + path, v.getMessage());
			});
			throw new ValidationException("Query validation failed", fieldErrors, Map.of());
		}

		// 5) Кладём валидный DTO в контекст
		ctx.setAttr(RequestAttr.VALID_QUERY, dto);
		chain.next();
	}
}