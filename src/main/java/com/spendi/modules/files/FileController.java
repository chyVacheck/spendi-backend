
/**
 * @file FileController.java
 * @module modules/files
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! java imports
 */
import java.util.List;

import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseController;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.files.DownloadedFile;
import com.spendi.core.response.ApiSuccessResponse;
import com.spendi.core.dto.IdDto;
import com.spendi.modules.files.dto.FileDownloadQuery;

public class FileController extends BaseController {

	protected static FileController INSTANCE = new FileController();
	private final FileService fileService = FileService.getInstance();

	protected FileController() {
		super(FileController.class.getSimpleName());
	}

	public static FileController getInstance() {
		return INSTANCE;
	}

	public void createOne(HttpContext ctx) {
		List<UploadedFile> files = ctx.getFiles();

		// Лог запроса на загрузку файла (несохраненный)
		this.info("file upload requested", ctx.getRequestId(), detailsOf("count", files.size()));

		var resp = this.fileService.createOne(ctx.getRequestId(), files.get(0));
		FileEntity file = resp.getData();

		ctx.res().success(
				ApiSuccessResponse.created(ctx.getRequestId(), "Uploaded one file", IdDto.of(file.id.toHexString())));
	}

	/**
	 * Скачать/просмотреть файл по id. Query-параметр: download=true|1 — принудительно attachment.
	 */
	public void downloadOne(HttpContext ctx) {
		IdDto params = ctx.getValidParams(IdDto.class);

		FileDownloadQuery q = ctx.getValidQuery(FileDownloadQuery.class);
		boolean attachment = q != null && q.asAttachment();

		// Лог запроса скачивания файла (несохраненный)
		this.info("file download requested", ctx.getRequestId(), detailsOf("id", params.id));

		var resp = this.fileService.downloadOne(ctx.getRequestId(), new ObjectId(params.id));
		DownloadedFile file = resp.getData();

		String disposition = (attachment ? "attachment" : "inline") + "; filename=\""
				+ (file.getFilename() == null ? (params.id + "") : file.getFilename()) + "\"";

		ctx.res().header("Content-Type", file.getContentType()).header("Content-Disposition", disposition)
				.sendBytes(file.getContent());
	}

	/**
	 * Удалить файл по id.
	 */
	public void deleteOne(HttpContext ctx) {
		IdDto params = ctx.getValidParams(IdDto.class);

		// Лог запроса удаления файла (несохраненный)
		this.info("file delete requested", ctx.getRequestId(), detailsOf("id", params.id));

		var resp = this.fileService.deleteById(ctx.getRequestId(), new ObjectId(params.id));

		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "deleted", detailsOf("id", resp.getData())));
	}
}
