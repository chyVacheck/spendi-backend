
/**
 * @file JavalinMultipartParserMiddleware.java
 * @module core/base/server/javalin/middleware
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server.javalin.middleware;

/**
 * ! java imports
 */
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.base.server.javalin.JavalinHttpContext;
import com.spendi.core.exceptions.BadRequestException;
import com.spendi.core.exceptions.UnsupportedMediaTypeException;
import com.spendi.core.files.UploadedFile;

public class JavalinMultipartParserMiddleware extends BaseMiddleware {

	public JavalinMultipartParserMiddleware() {
		super(JavalinMultipartParserMiddleware.class.getSimpleName());
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {

		if (!(ctx instanceof JavalinHttpContext jctx)) {
			// Этот middleware заточен под Javalin-обёртку
			chain.next();
			return;
		}

		var raw = jctx.raw();

		String actual = raw.contentType(); // может быть null
		String base = stripParams(actual); // может вернуть null
		if (!"multipart/form-data".equalsIgnoreCase(base)) {
			throw new UnsupportedMediaTypeException(
					actual == null ? "null" : actual,
					"multipart/form-data");
		}

		// Собираем загруженные файлы -> во временные файлы
		List<UploadedFile> ours = new ArrayList<>();
		List<Path> temps = new ArrayList<>();

		this.debug("start upload files");

		for (io.javalin.http.UploadedFile f : raw.uploadedFiles()) {
			String originalName = f.filename();
			String contentType = f.contentType() == null ? "application/octet-stream" : f.contentType();
			long size = f.size(); // Javalin знает размер

			String suffix = guessSuffix(originalName);

			Path tmp = Files.createTempFile("upload_", suffix);
			try (InputStream in = f.content()) {
				Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception copyErr) {
				this.warn("Error during replace temp file", ctx.getRequestId(), Map.of("temporary", tmp), true);

				try {
					Files.deleteIfExists(tmp);
				} catch (Exception ignore) {
				}
				throw copyErr;
			}

			ours.add(new UploadedFile(originalName, contentType, size, tmp));
			temps.add(tmp);
		}

		if (ours.isEmpty()) {
			throw new BadRequestException("No files in request", Map.of("expected", "at least one file"));
		}

		// Кладём в контекст
		ctx.setAttr(RequestAttr.FILES, ours);
		ctx.setAttr(RequestAttr.TEMP_FILES, temps);

		chain.next();
	}

	private static String stripParams(String contentType) {
		if (contentType == null)
			return null;
		int i = contentType.indexOf(';');
		return (i >= 0) ? contentType.substring(0, i).trim() : contentType.trim();
	}

	private static String guessSuffix(String filename) {
		if (filename == null)
			return ".bin";
		int dot = filename.lastIndexOf('.');
		return (dot >= 0) ? filename.substring(dot) : ".bin";
	}
}