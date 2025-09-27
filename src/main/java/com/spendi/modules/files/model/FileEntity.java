
/**
 * @file FileEntity.java
 * @module modules/files/model
 *
 * Сущность для хранения метаданных файла.
 * Используется для управления загруженными файлами и их атрибутами.
 * 
 * Пример: аватар пользователя, PDF-документ, изображение.
 * 
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files.model;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileEntity {
	/**
	 * Уникальный идентификатор файла (ObjectId). Используется как ключ в базе данных и основа для имени файла.
	 */
	private ObjectId id;

	/**
	 * Исходное имя файла, как оно было загружено пользователем.
	 * <p>
	 * Пример: {@code avatar.png}
	 * </p>
	 */
	private String originalName;

	/**
	 * MIME-тип содержимого файла.
	 * <p>
	 * Пример: {@code image/png}, {@code application/pdf}
	 * </p>
	 */
	private String contentType;

	/**
	 * Размер файла в байтах.
	 */
	private long size;

	/**
	 * Итоговое имя файла после сохранения. Обычно формируется на основе {@link #id}.
	 * <p>
	 * Пример: {@code 65fabc12e4567.png}
	 * </p>
	 */
	private String filename;

	/**
	 * Относительный путь, по которому хранится файл в файловой системе. Используется для организации каталогов по дате
	 * загрузки.
	 * <p>
	 * Пример: {@code 2025/09/27/65fabc12e4567.png}
	 * </p>
	 */
	private String relativePath;

	private FileSystem system;

	/**
	 * @return строковое представление {@link #id} в формате hex.
	 */
	public String getHexId() {
		return id != null ? id.toHexString() : null;
	}
}
