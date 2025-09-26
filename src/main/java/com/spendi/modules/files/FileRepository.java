
/**
 * @file FileRepository.java
 * @module modules/files
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! lib imports
 */
import org.bson.Document;
import com.mongodb.client.MongoDatabase;

/**
 * ! java imports
 */
import java.time.Instant;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.utils.InstantUtils;

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
		FileEntity e = new FileEntity();
		e.id = doc.getObjectId("_id");
		e.originalName = doc.getString("originalName");
		e.contentType = doc.getString("contentType");
		Object size = doc.get("size");
		e.size = (size instanceof Number) ? ((Number) size).longValue() : 0L;
		e.filename = doc.getString("filename");
		e.relativePath = doc.getString("relativePath");
		Object ca = doc.get("createdAt");
		e.createdAt = InstantUtils.getInstantOrNull(ca);
		return e;
	}

	@Override
	protected Document toDocument(FileEntity e) {
		Document d = new Document();
		if (e.id != null)
			d.put("_id", e.id);
		d.put("originalName", e.originalName);
		d.put("contentType", e.contentType);
		d.put("size", e.size);
		d.put("filename", e.filename);
		d.put("relativePath", e.relativePath);
		d.put("createdAt", e.createdAt != null ? Instant.from(e.createdAt) : null);
		return d;
	}
}
