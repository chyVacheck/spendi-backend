
/**
 * @file RequestAttr.java
 * @module core/base/http
 *
 * Перечисление списка системных атрибутов
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

public final class RequestAttr {
	private RequestAttr() {
	}

	public static final String REQUEST_ID = "requestId";
	public static final String START_NANOS = "startNanos";
	public static final String SUCCESS = "success";

	public static final String RAW_BODY = "rawBody"; // byte[]
	public static final String RAW_JSON = "rawJson"; // com.fasterxml.jackson.databind.JsonNode

	public static final String VALID_BODY = "validBody"; // T (валидация/маппинг)
	public static final String VALID_PARAMS = "validParams"; // T (валидация/маппинг)
	public static final String VALID_QUERY = "validQuery"; // T (валидация/маппинг)

	/**
	 * Список загруженных файлов (List<UploadedFile>)
	 */
	public static final String FILES = "uploadedFiles";

	/**
	 * Служебное: список временных путей для последующей очистки
	 * (List<java.nio.file.Path>)
	 */
	public static final String TEMP_FILES = "tempFiles";
}
