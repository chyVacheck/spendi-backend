/**
 * @file DownloadedFile.java
 * @module core/files
 *
 * DTO файла для отдачи наружу: содержимое, имя и MIME-тип.
 */

package com.spendi.core.files;

public final class DownloadedFile {
	private final byte[] content;
	private final String filename; // предпочтительно originalName
	private final String contentType; // MIME

	public DownloadedFile(byte[] content, String filename, String contentType) {
		this.content = content;
		this.filename = filename;
		this.contentType = contentType == null ? "application/octet-stream" : contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public String getFilename() {
		return filename;
	}

	public String getContentType() {
		return contentType;
	}
}
