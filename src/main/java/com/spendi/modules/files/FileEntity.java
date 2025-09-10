/**
 * @file FileEntity.java
 * @module modules/files
 *
 * Сущность метаданных сохранённого файла.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ! java imports
 */
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileEntity {
	public ObjectId id; // _id (используем в имени файла)
	public String originalName; // исходное имя (avatar.png)
	public String contentType; // MIME
	public long size; // байты
	public String filename; // конечное имя (ObjectId.png)
	public String relativePath; // относительный путь (yyyy/MM/dd/ObjectId.png)
	public Instant createdAt; // для датированных каталогов
}
