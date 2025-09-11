/**
 * @file FileService.java
 * @module modules/files
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.time.Instant;

/**
 * ! my imports
 */
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.exceptions.EntityNotFoundException;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.files.StoredFile;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.files.FileStorage;
import com.spendi.core.files.DownloadedFile;

public class FileService extends BaseRepositoryService<FileRepository, FileEntity> {

	private static volatile FileService INSTANCE;
	private final FileStorage fileStorage = FileStorage.getInstance();

	protected FileService(FileRepository repository) {
		super(FileService.class.getSimpleName(), repository);
	}

	/**
	 * Инициализация с явно переданным репозиторием (из AppInitializer).
	 */
	public static void init(FileRepository repository) {
		synchronized (FileService.class) {
			if (INSTANCE == null) {
				INSTANCE = new FileService(repository);
			}
		}
	}

	/**
	 * Доступ к инстансу после предварительного init(...).
	 */
	public static FileService getInstance() {
		FileService ref = INSTANCE;
		if (ref == null) {
			throw new IllegalStateException("FileService not initialized. Call AppInitializer.initAll() in App.main");
		}
		return ref;
	}

	/**
	 * Создать запись в БД на основе сохранённого файла (локально).
	 */
	protected ServiceResponse<FileEntity> createOne(String requestId, UploadedFile uf) {
		FileEntity e = new FileEntity();

		ObjectId id = new ObjectId();
		StoredFile stored = this.fileStorage.save(requestId, id.toHexString(), uf);

		e.id = id;
		e.originalName = uf.getOriginalName();
		e.contentType = uf.getContentType();
		e.size = uf.getSize();
		e.filename = stored.getFilename();
		e.relativePath = stored.getRelative();
		e.createdAt = Instant.now();

		ServiceResponse<FileEntity> res = this.createOne(e);

		this.info("File metadata created", requestId, detailsOf("id", e.id.toHexString(), "rel", e.relativePath),
				true);
		return res;
	}

	/** Публичная обёртка для загрузки одного файла. */
	public ServiceResponse<FileEntity> uploadOne(String requestId, UploadedFile uf) {
		return createOne(requestId, uf);
	}

	/**
	 * Загрузить содержимое файла по id с диска, вернуть как DTO для отдачи.
	 */
	public ServiceResponse<DownloadedFile> downloadOne(String requestId, String id) {
		// 1) Метаданные из БД
		FileEntity e = this.getById(id).getData();
		// 2) Чтение из ФС
		if (e == null || e.relativePath == null) {
			throw new EntityNotFoundException("File", "id", id);
		}

		if (!this.fileStorage.exists(e.relativePath)) {
			this.info("file content not found", requestId, detailsOf("id", id), false);

			throw new EntityNotFoundException("FileContent", "relativePath", e.relativePath);
		}

		byte[] content = this.fileStorage.read(requestId, e.relativePath);
		String filename = (e.originalName != null && !e.originalName.isBlank()) ? e.originalName : e.filename;

		DownloadedFile dto = new DownloadedFile(content, filename, e.contentType);

		this.info("file read for download", requestId, detailsOf("id", id, "bytes", content.length), false);

		return ServiceResponse.founded(dto);
	}

	public ServiceResponse<String> deleteById(String requestId, String id) {
		// ensure exists and get metadata
		FileEntity e = this.getById(id).getData();
		// try delete physical file (best-effort)
		try {
			if (e != null && e.relativePath != null) {
				this.fileStorage.delete(requestId, e.relativePath);
				this.info("file deleted", requestId, detailsOf("relative", e.relativePath), true);
			}
		} catch (RuntimeException ignore) {
		}
		var res = super.deleteById(id); // ServiceResponse<String> with deleted id or throws
		this.info("file metadata deleted", requestId, detailsOf("id", id), false);
		return res;
	}

}
