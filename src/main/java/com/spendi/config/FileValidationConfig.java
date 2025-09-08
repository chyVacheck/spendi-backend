/**
 * @file FileValidationConfig.java
 * @module config
 *
 * Конфигурация правил валидации файлов.
 * Источники: .env → System.getenv → значения по умолчанию.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! java imports
 */
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;
import com.spendi.core.files.validation.FileValidationRules;

public final class FileValidationConfig extends BaseConfig {

	// --- MIME / Extensions ---
	private final Set<String> allowedMimes;
	private final Set<String> allowedMimePrefixes;
	private final Set<String> deniedMimes;
	private final Set<String> deniedMimePrefixes;
	private final Set<String> allowedExtensions;

	// --- Limits ---
	private final int minFiles;
	private final int maxFiles;
	private final long maxPerFileBytes;
	private final long maxTotalBytes;
	private final int maxFilenameLength;

	// --- Behavior ---
	private final boolean sanitizeFilenames;
	private final FileValidationRules.SniffMode contentSniffing;
	private final FileValidationRules.MismatchPolicy onMismatchPolicy;
	private final boolean computeSha256;

	// --- Image rules ---
	private final Integer imageMinWidth;
	private final Integer imageMinHeight;
	private final Integer imageMaxWidth;
	private final Integer imageMaxHeight;
	private final Double imageMaxMegapixels;

	// --- PDF rules ---
	private final Integer pdfMaxPages;
	private final boolean pdfForbidEncrypted;

	public FileValidationConfig() {

		this.allowedMimes = parseCsvSet(getenv(this.dotenv, "SPENDI_FILE_ALLOWED_MIMES", ""));
		this.allowedMimePrefixes = parseCsvSet(getenv(this.dotenv, "SPENDI_FILE_ALLOWED_MIME_PREFIXES", "image/"));
		this.deniedMimes = parseCsvSet(getenv(this.dotenv, "SPENDI_FILE_DENIED_MIMES", ""));
		this.deniedMimePrefixes = parseCsvSet(getenv(this.dotenv, "SPENDI_FILE_DENIED_PREFIXES", ""));
		this.allowedExtensions = parseCsvSet(getenv(this.dotenv, "SPENDI_FILE_ALLOWED_EXTS", ".png,.jpg,.jpeg,.pdf"));

		this.minFiles = (int) parseLong(getenv(this.dotenv, "SPENDI_FILE_MIN_COUNT", "1"), 1);
		this.maxFiles = (int) parseLong(getenv(this.dotenv, "SPENDI_FILE_MAX_COUNT", "10"), 10);
		this.maxPerFileBytes = parseLong(
				getenv(this.dotenv, "SPENDI_FILE_MAX_PER_BYTES", String.valueOf(5L * 1024 * 1024)),
				5L * 1024 * 1024);
		this.maxTotalBytes = parseLong(
				getenv(this.dotenv, "SPENDI_FILE_MAX_TOTAL_BYTES", String.valueOf(20L * 1024 * 1024)),
				20L * 1024 * 1024);
		this.maxFilenameLength = (int) parseLong(getenv(this.dotenv, "SPENDI_FILE_MAX_FILENAME", "255"), 255);

		this.sanitizeFilenames = parseBool(getenv(this.dotenv, "SPENDI_FILE_SANITIZE_NAMES", "true"), true);
		this.contentSniffing = parseEnum(getenv(this.dotenv, "SPENDI_FILE_SNIFFING", "STRICT"),
				FileValidationRules.SniffMode.STRICT);
		this.onMismatchPolicy = parseEnum(getenv(this.dotenv, "SPENDI_FILE_ON_MISMATCH", "ERROR"),
				FileValidationRules.MismatchPolicy.ERROR);
		this.computeSha256 = parseBool(getenv(this.dotenv, "SPENDI_FILE_COMPUTE_SHA256", "false"), false);

		this.imageMinWidth = parseNullableInt(getenv(this.dotenv, "SPENDI_FILE_IMAGE_MIN_W", null));
		this.imageMinHeight = parseNullableInt(getenv(this.dotenv, "SPENDI_FILE_IMAGE_MIN_H", null));
		this.imageMaxWidth = parseNullableInt(getenv(this.dotenv, "SPENDI_FILE_IMAGE_MAX_W", null));
		this.imageMaxHeight = parseNullableInt(getenv(this.dotenv, "SPENDI_FILE_IMAGE_MAX_H", null));
		this.imageMaxMegapixels = parseNullableDouble(getenv(this.dotenv, "SPENDI_FILE_IMAGE_MAX_MP", "50"));

		this.pdfMaxPages = (int) parseLong(getenv(this.dotenv, "SPENDI_FILE_PDF_MAX_PAGES", "50"), 50);
		this.pdfForbidEncrypted = parseBool(getenv(this.dotenv, "SPENDI_FILE_PDF_FORBID_ENCRYPTED", "true"), true);
	}

	// --- getters ---
	public Set<String> getAllowedMimes() {
		return allowedMimes;
	}

	public Set<String> getAllowedMimePrefixes() {
		return allowedMimePrefixes;
	}

	public Set<String> getDeniedMimes() {
		return deniedMimes;
	}

	public Set<String> getDeniedMimePrefixes() {
		return deniedMimePrefixes;
	}

	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}

	public int getMinFiles() {
		return minFiles;
	}

	public int getMaxFiles() {
		return maxFiles;
	}

	public long getMaxPerFileBytes() {
		return maxPerFileBytes;
	}

	public long getMaxTotalBytes() {
		return maxTotalBytes;
	}

	public int getMaxFilenameLength() {
		return maxFilenameLength;
	}

	public boolean isSanitizeFilenames() {
		return sanitizeFilenames;
	}

	public FileValidationRules.SniffMode getContentSniffing() {
		return contentSniffing;
	}

	public FileValidationRules.MismatchPolicy getOnMismatchPolicy() {
		return onMismatchPolicy;
	}

	public boolean isComputeSha256() {
		return computeSha256;
	}

	public Integer getImageMinWidth() {
		return imageMinWidth;
	}

	public Integer getImageMinHeight() {
		return imageMinHeight;
	}

	public Integer getImageMaxWidth() {
		return imageMaxWidth;
	}

	public Integer getImageMaxHeight() {
		return imageMaxHeight;
	}

	public Double getImageMaxMegapixels() {
		return imageMaxMegapixels;
	}

	public Integer getPdfMaxPages() {
		return pdfMaxPages;
	}

	public boolean isPdfForbidEncrypted() {
		return pdfForbidEncrypted;
	}
}
