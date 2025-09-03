
/**
 * @file ParamsValidationMiddleware.java
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

/**
 * Валидирует path-параметры, биндя их в DTO и прогоняя через Jakarta
 * Validation.
 */
public final class ParamsValidationMiddleware<T> extends BaseMiddleware {
	private final Class<T> dtoClass;
	private final Validator validator = Validators.get();

	public ParamsValidationMiddleware(Class<T> dtoClass) {
		super(ParamsValidationMiddleware.class.getSimpleName() + "<" + dtoClass.getSimpleName() + ">");
		this.dtoClass = dtoClass;
	}

	public static <T> ParamsValidationMiddleware<T> of(Class<T> dtoClass) {
		return new ParamsValidationMiddleware<>(dtoClass);
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		// 1) Берём все path-параметры как Map<String,String>
		Map<String, String> source = ctx.req().pathParams();

		// 2) Конвертим в DTO (Jackson умеет map->bean по совпадающим именам)
		final T dto = Jsons.mapper().convertValue(source, dtoClass);

		// 3) Валидируем
		Set<ConstraintViolation<T>> violations = validator.validate(dto);
		if (!violations.isEmpty()) {
			Map<String, String> fieldErrors = new HashMap<>(violations.size());
			violations.forEach(v -> {
				String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
				fieldErrors.put("params." + path, v.getMessage()); // префиксируем для ясности
			});
			throw new ValidationException("Params validation failed", fieldErrors, Map.of());
		}

		// 4) Кладём валидный DTO в контекст
		ctx.setAttr(RequestAttr.VALID_PARAMS, dto);
		chain.next();
	}
}
