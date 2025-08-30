// src/main/java/com/spendi/core/base/server/MyExceptionMapper.java
package com.spendi.core.base.server;

import java.util.Map;

import com.spendi.core.exceptions.DomainException;
import com.spendi.core.exceptions.ErrorCode;

public class MyExceptionMapper implements ExceptionMapper {

	@Override
	public DomainException toDomainException(Throwable e) {
		// 1) Уже доменное — отдадим как есть
		if (e instanceof DomainException de) {
			return de;
		}

		// // 2) Частые случаи: неверные аргументы = 400
		// if (e instanceof IllegalArgumentException) {
		// return new DomainException(
		// e.getMessage() != null ? e.getMessage() : "Bad request",
		// ErrorCode.BAD_REQUEST,
		// Map.of("exception", e.getClass().getSimpleName()),
		// Map.of()) {
		// };
		// }

		// 3) Иначе — внутренняя ошибка
		return new DomainException(
				"Internal server error",
				ErrorCode.INTERNAL_ERROR,
				Map.of("exception", e.getClass().getName()),
				Map.of()) {
		};
	}
}