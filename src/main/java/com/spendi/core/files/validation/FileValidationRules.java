/**
 * @file FileValidationRules.java
 * @module core/files/validation
 *
 * Набор правил валидации для загружаемых файлов.
 * Иммутабельный объект с Builder'ом и фабрикой загрузки дефолтов из .env.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.files.validation;

/**
 * ! java imports
 */
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.config.FileValidationConfig;
import com.spendi.core.utils.SetUtils;

/**
 * Иммутабельные правила валидации файлов.
 */
public final class FileValidationRules {

	// --- MIME / Расширения ---
	private final Set<String> allowedMimes; // точные MIME, например image/png
	private final Set<String> allowedMimePrefixes; // префиксы MIME, например image/
	private final Set<String> deniedMimes; // запрет по MIME
	private final Set<String> deniedMimePrefixes; // запрет по префиксу
	private final Set<String> allowedExtensions; // .png, .jpg, .pdf (в нижнем регистре)

	// --- Лимиты ---
	private final int minFiles;
	private final int maxFiles;
	private final long maxPerFileBytes;
	private final long maxTotalBytes;
	private final int maxFilenameLength;

	// --- Контент и поведение ---
	private final boolean sanitizeFilenames;
	private final SniffMode contentSniffing;
	private final MismatchPolicy onMismatchPolicy;
	private final boolean computeSha256;

	// --- Правила изображений ---
	private final Integer imageMinWidth;
	private final Integer imageMinHeight;
	private final Integer imageMaxWidth;
	private final Integer imageMaxHeight;
	private final Double imageMaxMegapixels;

	// --- Правила PDF ---
	private final Integer pdfMaxPages;
	private final boolean pdfForbidEncrypted;

	private FileValidationRules(Builder b) {
		this.allowedMimes = unmodifiableLowerTrimmed(b.allowedMimes);
		this.allowedMimePrefixes = unmodifiableLowerTrimmed(b.allowedMimePrefixes);
		this.deniedMimes = unmodifiableLowerTrimmed(b.deniedMimes);
		this.deniedMimePrefixes = unmodifiableLowerTrimmed(b.deniedMimePrefixes);
		this.allowedExtensions = unmodifiableLowerTrimmed(b.allowedExtensions);

		this.minFiles = Math.max(0, b.minFiles);
		this.maxFiles = Math.max(0, b.maxFiles);
		this.maxPerFileBytes = Math.max(0L, b.maxPerFileBytes);
		this.maxTotalBytes = Math.max(0L, b.maxTotalBytes);
		this.maxFilenameLength = b.maxFilenameLength > 0 ? b.maxFilenameLength : 255;

		this.sanitizeFilenames = b.sanitizeFilenames;
		this.contentSniffing = Objects.requireNonNullElse(b.contentSniffing, SniffMode.STRICT);
		this.onMismatchPolicy = Objects.requireNonNullElse(b.onMismatchPolicy, MismatchPolicy.ERROR);
		this.computeSha256 = b.computeSha256;

		this.imageMinWidth = b.imageMinWidth;
		this.imageMinHeight = b.imageMinHeight;
		this.imageMaxWidth = b.imageMaxWidth;
		this.imageMaxHeight = b.imageMaxHeight;
		this.imageMaxMegapixels = b.imageMaxMegapixels;

		this.pdfMaxPages = b.pdfMaxPages;
		this.pdfForbidEncrypted = b.pdfForbidEncrypted;
	}

	// --- Builder ---
	public static final class Builder {
		private Set<String> allowedMimes = Set.of();
		private Set<String> allowedMimePrefixes = Set.of();
		private Set<String> deniedMimes = Set.of();
		private Set<String> deniedMimePrefixes = Set.of();
		private Set<String> allowedExtensions = Set.of();

		private int minFiles = 1;
		private int maxFiles = 10;
		private long maxPerFileBytes = 5L * 1024 * 1024; // 5MB
		private long maxTotalBytes = 20L * 1024 * 1024; // 20MB
		private int maxFilenameLength = 255;

		private boolean sanitizeFilenames = true;
		private SniffMode contentSniffing = SniffMode.STRICT;
		private MismatchPolicy onMismatchPolicy = MismatchPolicy.ERROR;
		private boolean computeSha256 = false;

		private Integer imageMinWidth = null;
		private Integer imageMinHeight = null;
		private Integer imageMaxWidth = null;
		private Integer imageMaxHeight = null;
		private Double imageMaxMegapixels = 50.0; // разумный дефолт

		private Integer pdfMaxPages = 50;
		private boolean pdfForbidEncrypted = true;

		public Builder allowedMimes(Set<String> m) {
			this.allowedMimes = safeSet(m);
			return this;
		}

		public Builder allowedMimes(String... m) {
			this.allowedMimes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder allowedMimes(Iterable<String> m) {
			this.allowedMimes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder allowedMimePrefixes(Set<String> m) {
			this.allowedMimePrefixes = safeSet(m);
			return this;
		}

		public Builder allowedMimePrefixes(String... m) {
			this.allowedMimePrefixes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder allowedMimePrefixes(Iterable<String> m) {
			this.allowedMimePrefixes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder deniedMimes(Set<String> m) {
			this.deniedMimes = safeSet(m);
			return this;
		}

		public Builder deniedMimes(String... m) {
			this.deniedMimes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder deniedMimes(Iterable<String> m) {
			this.deniedMimes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder deniedMimePrefixes(Set<String> m) {
			this.deniedMimePrefixes = safeSet(m);
			return this;
		}

		public Builder deniedMimePrefixes(String... m) {
			this.deniedMimePrefixes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder deniedMimePrefixes(Iterable<String> m) {
			this.deniedMimePrefixes = SetUtils.ofNonNull(m);
			return this;
		}

		public Builder allowedExtensions(Set<String> exts) {
			this.allowedExtensions = safeSet(exts);
			return this;
		}

		/**
		 * Указать разрешённые расширения как varargs.
		 * Пример: allowedExtensions(".png", ".jpg")
		 */
		public Builder allowedExtensions(String... exts) {
			this.allowedExtensions = SetUtils.ofNonNull(exts);
			return this;
		}

		/**
		 * Указать разрешённые расширения из коллекции.
		 * Пример: allowedExtensions(List.of(".png", ".jpg"))
		 */
		public Builder allowedExtensions(Iterable<String> exts) {
			this.allowedExtensions = SetUtils.ofNonNull(exts);
			return this;
		}

		public Builder minFiles(int v) {
			this.minFiles = v;
			return this;
		}

		public Builder maxFiles(int v) {
			this.maxFiles = v;
			return this;
		}

		public Builder maxPerFileBytes(long v) {
			this.maxPerFileBytes = v;
			return this;
		}

		public Builder maxTotalBytes(long v) {
			this.maxTotalBytes = v;
			return this;
		}

		public Builder maxFilenameLength(int v) {
			this.maxFilenameLength = v;
			return this;
		}

		public Builder sanitizeFilenames(boolean v) {
			this.sanitizeFilenames = v;
			return this;
		}

		public Builder contentSniffing(SniffMode v) {
			this.contentSniffing = v;
			return this;
		}

		public Builder onMismatchPolicy(MismatchPolicy v) {
			this.onMismatchPolicy = v;
			return this;
		}

		public Builder computeSha256(boolean v) {
			this.computeSha256 = v;
			return this;
		}

		public Builder imageMinWidth(Integer v) {
			this.imageMinWidth = v;
			return this;
		}

		public Builder imageMinHeight(Integer v) {
			this.imageMinHeight = v;
			return this;
		}

		public Builder imageMaxWidth(Integer v) {
			this.imageMaxWidth = v;
			return this;
		}

		public Builder imageMaxHeight(Integer v) {
			this.imageMaxHeight = v;
			return this;
		}

		public Builder imageMaxMegapixels(Double v) {
			this.imageMaxMegapixels = v;
			return this;
		}

		public Builder pdfMaxPages(Integer v) {
			this.pdfMaxPages = v;
			return this;
		}

		public Builder pdfForbidEncrypted(boolean v) {
			this.pdfForbidEncrypted = v;
			return this;
		}

		public FileValidationRules build() {
			return new FileValidationRules(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	// --- Enums ---
	public static enum SniffMode {
		OFF, RELAXED, STRICT
	}

	public static enum MismatchPolicy {
		ERROR, WARN_ALLOW
	}

	// --- Defaults from .env ---
	public static FileValidationRules defaultsFromEnv() {
		var cfg = new FileValidationConfig();
		return builderFromConfig(cfg).build();
	}

	/**
	 * Построить правила из внешней конфигурации.
	 */
	public static FileValidationRules fromConfig(FileValidationConfig cfg) {
		Objects.requireNonNull(cfg);
		return builderFromConfig(cfg).build();
	}

	/**
	 * Вернуть Builder, заполненный значениями из конфигурации.
	 */
	public static Builder builderFromConfig(FileValidationConfig cfg) {
		Objects.requireNonNull(cfg);
		Builder b = builder();
		b.allowedMimes(cfg.getAllowedMimes())
				.allowedMimePrefixes(cfg.getAllowedMimePrefixes())
				.deniedMimes(cfg.getDeniedMimes())
				.deniedMimePrefixes(cfg.getDeniedMimePrefixes())
				.allowedExtensions(cfg.getAllowedExtensions())
				.minFiles(cfg.getMinFiles())
				.maxFiles(cfg.getMaxFiles())
				.maxPerFileBytes(cfg.getMaxPerFileBytes())
				.maxTotalBytes(cfg.getMaxTotalBytes())
				.maxFilenameLength(cfg.getMaxFilenameLength())
				.sanitizeFilenames(cfg.isSanitizeFilenames())
				.contentSniffing(cfg.getContentSniffing())
				.onMismatchPolicy(cfg.getOnMismatchPolicy())
				.computeSha256(cfg.isComputeSha256())
				.imageMinWidth(cfg.getImageMinWidth())
				.imageMinHeight(cfg.getImageMinHeight())
				.imageMaxWidth(cfg.getImageMaxWidth())
				.imageMaxHeight(cfg.getImageMaxHeight())
				.imageMaxMegapixels(cfg.getImageMaxMegapixels())
				.pdfMaxPages(cfg.getPdfMaxPages())
				.pdfForbidEncrypted(cfg.isPdfForbidEncrypted());
		return b;
	}

	// --- Getters ---
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

	public SniffMode getContentSniffing() {
		return contentSniffing;
	}

	public MismatchPolicy getOnMismatchPolicy() {
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

	// --- helpers ---
	private static Set<String> unmodifiableLowerTrimmed(Set<String> in) {
		if (in == null || in.isEmpty())
			return Set.of();
		LinkedHashSet<String> acc = new LinkedHashSet<>();
		for (String s : in) {
			if (s == null)
				continue;
			String x = s.trim();
			if (!x.isBlank())
				acc.add(x.toLowerCase());
		}
		return Collections.unmodifiableSet(acc);
	}

	private static Set<String> safeSet(Set<String> in) {
		if (in == null || in.isEmpty())
			return Set.of();
		return new LinkedHashSet<>(in);
	}
}
