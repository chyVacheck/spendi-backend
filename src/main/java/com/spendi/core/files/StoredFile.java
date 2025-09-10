/**
 * @file StoredFile.java
 * @module core/files
 *
 * Модель сохранённого (постоянного) файла.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.files;

public final class StoredFile {
	// Конечное имя файла (на диске)
	private final String filename;
	// Относительный путь относительно корня стораджа
	private final String relative;

	public StoredFile(String filename, String relative) {
		this.filename = filename;
		this.relative = relative;
	}

	public String getFilename() {
		return filename;
	}

	public String getRelative() {
		return relative;
	}
}
