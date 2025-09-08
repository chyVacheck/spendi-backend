/**
 * @file ContentTypeMismatchException.java
 * @module core/exceptions
 *
 * Несоответствие заявленного Content-Type и фактически определённого по сигнатуре.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#UNSUPPORTED_MEDIA_TYPE} (HTTP 415).
 */
public class ContentTypeMismatchException extends DomainException {

	public ContentTypeMismatchException(String fileName, String headerType, String sniffedType) {
		super("Content-Type does not match file content",
				ErrorCode.UNSUPPORTED_MEDIA_TYPE,
				Map.of(
						"file", fileName,
						"header", headerType,
						"sniffed", sniffedType),
				Map.of());
	}
}
