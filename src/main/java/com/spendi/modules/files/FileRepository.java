
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
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoDatabase;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.files.model.FileEntity;

public class FileRepository extends BaseRepository<FileEntity> {

	public static final String COLLECTION = "files";

	public FileRepository(MongoDatabase db) {
		super(FileRepository.class.getSimpleName(), FileEntity.class, db, COLLECTION);
	}

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	protected FileEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		// читаем _id первым (для логов ниже)
		ObjectId id = reqObjectId(doc, "_id", null);

		return FileEntity.builder().id(id).originalName(reqString(doc, "originalName", id))
				// поддержка Number/Decimal128/String в BaseRepository
				.contentType(reqString(doc, "contentType", id)).size(reqLong(doc, "size", id))
				.filename(reqString(doc, "filename", id)).relativePath(reqString(doc, "relativePath", id))
				.createdAt(reqInstant(doc, "createdAt", id)).build();
	}

	@Override
	protected Document toDocument(FileEntity e) {
		Document d = new Document();

		// _id обязателен при сохранении (но на всякий случай проверим)
		if (e.getId() != null) {
			d.put("_id", e.getId());
		}

		d.put("originalName", e.getOriginalName());
		d.put("contentType", e.getContentType());
		d.put("size", e.getSize());
		d.put("filename", e.getFilename());
		d.put("relativePath", e.getRelativePath());
		d.put("createdAt", e.getCreatedAt());

		return d;
	}
}
