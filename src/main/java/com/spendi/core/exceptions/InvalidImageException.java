/**
 * @file InvalidImageException.java
 * @module core/exceptions
 *
 * Ошибки, связанные с валидацией изображений (размеры, мегапиксели, заголовки).
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#VALIDATION_FAILED} (HTTP 422).
 */
public class InvalidImageException extends DomainException {

	public InvalidImageException(String message, Map<String, Object> details) {
		super(message, ErrorCode.VALIDATION_FAILED, details, Map.of());
	}

	public static InvalidImageException dimensionsOutOfRange(String fileName, int width, int height,
			Integer minW, Integer minH,
			Integer maxW, Integer maxH) {
		return new InvalidImageException(
				"Image dimensions are out of allowed range",
				Map.of(
						"file", fileName,
						"width", width,
						"height", height,
						"minWidth", minW,
						"minHeight", minH,
						"maxWidth", maxW,
						"maxHeight", maxH));
	}

	public static InvalidImageException megapixelsExceeded(String fileName, double mp, double maxMp) {
		return new InvalidImageException(
				"Image megapixels exceeded",
				Map.of("file", fileName, "megapixels", mp, "maxMegapixels", maxMp));
	}
}
