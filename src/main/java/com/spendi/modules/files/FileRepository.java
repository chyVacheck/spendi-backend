
/**
 * @file FileRepository.java
 * @module modules/files
 * 
 * Репозиторий метаданных файлов. Строгий маппинг:
 * - все ключевые поля обязательны;
 * - при отсутствии/невалидности — осмысленная ошибка через helpers из BaseRepository.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! lib imports
 */
import com.mongodb.client.MongoDatabase;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.files.model.FileEntity;

public class FileRepository extends BaseRepository<FileEntity> {

	public static final String COLLECTION = "files";

	public FileRepository(MongoDatabase db) {
		super(FileRepository.class.getSimpleName(), FileEntity.class, db, COLLECTION, FileMapper.getInstance());
	}

}
