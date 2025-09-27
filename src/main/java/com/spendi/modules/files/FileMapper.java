
/**
 * @file FileMapper.java
 * @module modules/file
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.core.types.DocMapper;
import com.spendi.modules.files.model.FileEntity;
import com.spendi.modules.files.model.FileSystem;
import com.spendi.shared.mapper.meta.BaseMetaMapper;

public class FileMapper extends BaseMapper<FileEntity> implements DocMapper<FileEntity> {

	private final static FileMapper mapper = new FileMapper();
	private static final BaseMetaMapper META = BaseMetaMapper.getInstance();

	public FileMapper() {
		super(FileMapper.class.getSimpleName(), FileEntity.class, "files");
	}

	public static FileMapper getInstance() {
		return mapper;
	}

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	public FileEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		// читаем _id первым (для логов ниже)
		ObjectId id = reqObjectId(doc, "_id", null);

		return FileEntity.builder().id(id).originalName(reqString(doc, "originalName", id))
				// поддержка Number/Decimal128/String в BaseRepository
				.contentType(reqString(doc, "contentType", id)).size(reqLong(doc, "size", id))
				.filename(reqString(doc, "filename", id)).relativePath(reqString(doc, "relativePath", id))
				.system(FileSystem.builder().meta(META.toEntity(doc)).build()).build();
	}

	@Override
	public Document toDocument(FileEntity e) {
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
		d.put("system", e.getSystem());

		return d;
	}
}
