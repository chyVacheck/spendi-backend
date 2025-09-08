/**
 * @file FileExtensionNotAllowedException.java
 * @module core/exceptions
 *
 * Исключение, когда расширение файла не разрешено политикой загрузки.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Collection;
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#UNSUPPORTED_MEDIA_TYPE} (HTTP 415).
 */
public class FileExtensionNotAllowedException extends DomainException {

	public FileExtensionNotAllowedException(String fileName, String ext, Collection<String> allowed) {
		super("File extension is not allowed: " + ext,
				ErrorCode.UNSUPPORTED_MEDIA_TYPE,
				Map.of(
						"file", fileName,
						"extension", ext,
						"allowed", allowed == null ? null : allowed.toString()),
				Map.of());
	}
}
