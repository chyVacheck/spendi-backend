/**
 * @file FileValidationMiddleware.java
 * @module core/middleware
 *
 * Middleware валидации загружаемых файлов на основе FileValidationRules.
 */

package com.spendi.core.middleware;

/**
 * ! java imports
 */
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.exceptions.BadRequestException;
import com.spendi.core.exceptions.FileExtensionNotAllowedException;
import com.spendi.core.exceptions.FileValidationException;
import com.spendi.core.exceptions.PayloadTooLargeException;
import com.spendi.core.exceptions.UnsupportedMediaTypeException;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.files.validation.FileValidationRules;
import com.spendi.core.utils.FileUtils;
import com.spendi.core.utils.StringUtils;

public class FileValidationMiddleware extends BaseMiddleware {

	private final FileValidationRules rules;

	public FileValidationMiddleware(FileValidationRules rules) {
		super(FileValidationMiddleware.class.getSimpleName());
		this.rules = Objects.requireNonNull(rules);
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		@SuppressWarnings("unchecked")
		List<UploadedFile> files = (List<UploadedFile>) ctx.getAttr(RequestAttr.FILES, List.class);

		if (files == null || files.isEmpty()) {
			throw new BadRequestException("No files to validate", Map.of("expected", ">= " + rules.getMinFiles()));
		}

		// Количество
		int count = files.size();
		if (count < rules.getMinFiles()) {
			throw new FileValidationException("Too few files",
					Map.of("count", count, "min", rules.getMinFiles()));
		}
		if (rules.getMaxFiles() > 0 && count > rules.getMaxFiles()) {
			throw new PayloadTooLargeException(count, rules.getMaxFiles());
		}

		long totalBytes = 0L;

		for (int i = 0; i < files.size(); i++) {
			UploadedFile f = files.get(i);
			String name = f.getOriginalName();
			String mime = StringUtils.lowerOrNull(f.getContentType());
			long size = f.getSize();

			// Размер файла
			if (rules.getMaxPerFileBytes() > 0 && size > rules.getMaxPerFileBytes()) {
				throw new PayloadTooLargeException(name, size, rules.getMaxPerFileBytes());
			}
			if (size <= 0) {
				throw new FileValidationException("Empty file",
						Map.of("file", name, "index", i));
			}

			totalBytes += size;

			// Имя файла: длина
			if (name != null && name.length() > rules.getMaxFilenameLength()) {
				throw new FileValidationException("Filename too long",
						Map.of("file", name, "maxLength", rules.getMaxFilenameLength()));
			}

			// Расширение
			String ext = FileUtils.getFileExtension(name);
			if (!rules.getAllowedExtensions().isEmpty()) {
				if (ext == null || !rules.getAllowedExtensions().contains(ext)) {
					throw new FileExtensionNotAllowedException(name, ext, rules.getAllowedExtensions());
				}
			}

			// MIME deny-list
			if (!rules.getDeniedMimes().isEmpty() && mime != null && rules.getDeniedMimes().contains(mime)) {
				throw new UnsupportedMediaTypeException(mime, "not in denied list");
			}
			if (!rules.getDeniedMimePrefixes().isEmpty() && mime != null) {
				for (String p : rules.getDeniedMimePrefixes()) {
					if (mime.startsWith(p)) {
						throw new UnsupportedMediaTypeException(mime, "not starting with '" + p + "'");
					}
				}
			}

			// MIME allow-list (простая проверка по header; строгий sniff добавим позже)
			if (!rules.getAllowedMimes().isEmpty()) {
				if (mime == null || !rules.getAllowedMimes().contains(mime)) {
					throw new UnsupportedMediaTypeException(mime, String.join(",", rules.getAllowedMimes()));
				}
			}
			if (!rules.getAllowedMimePrefixes().isEmpty()) {
				boolean any = false;
				if (mime != null) {
					for (String p : rules.getAllowedMimePrefixes()) {
						if (mime.startsWith(p)) {
							any = true;
							break;
						}
					}
				}
				if (!any) {
					throw new UnsupportedMediaTypeException(mime, String.join(",", rules.getAllowedMimePrefixes()));
				}
			}
		}

		if (rules.getMaxTotalBytes() > 0 && totalBytes > rules.getMaxTotalBytes()) {
			throw new PayloadTooLargeException(totalBytes, rules.getMaxTotalBytes());
		}

		chain.next();
	}

}
