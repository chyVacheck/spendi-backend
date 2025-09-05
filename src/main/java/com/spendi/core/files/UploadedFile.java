
/** 
 * @file UploadedFile.java
 * @module core/files
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.files;

/**
 * ! java imports
 */
import java.nio.file.Path;
import java.util.Objects;

/**
 * Универсальный объект загруженного файла, лежащего во временном файле.
 */
public final class UploadedFile {
	private final String originalName;
	private final String contentType;
	private final long size;
	private final Path tempPath;

	public UploadedFile(String originalName, String contentType, long size, Path tempPath) {
		this.originalName = (originalName == null) ? "" : originalName;
		this.contentType = (contentType == null) ? "application/octet-stream" : contentType;
		this.size = Math.max(0, size);
		this.tempPath = Objects.requireNonNull(tempPath, "tempPath must not be null");
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSize() {
		return size;
	}

	public Path getTempPath() {
		return tempPath;
	}

	@Override
	public String toString() {
		return "UploadedFile{originalName='%s', contentType='%s', size=%d, tempPath=%s}"
				.formatted(originalName, contentType, size, tempPath);
	}
}