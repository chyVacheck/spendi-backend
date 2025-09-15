
/**
 * @file CryptoConfig.java
 * @module config
 * @author Dmytro Shakh
 */

package com.spendi.config;

import com.spendi.core.base.BaseConfig;

public final class CryptoConfig extends BaseConfig {
	private static final CryptoConfig INSTANCE = new CryptoConfig();

	private final String pepper;
	private final int iterations;
	private final int keyLengthBits;
	private final int saltBytes;

	private CryptoConfig() {
		this.pepper = getenv(this.dotenv, "SPENDI_SECURITY_PEPPER", "dev-pepper-change");
		this.iterations = (int) parseLong(getenv(this.dotenv, "SPENDI_PBKDF2_ITER", "210000"), 210_000);
		this.keyLengthBits = (int) parseLong(getenv(this.dotenv, "SPENDI_PBKDF2_KEYLEN_BITS", "256"), 256);
		this.saltBytes = (int) parseLong(getenv(this.dotenv, "SPENDI_PBKDF2_SALT_BYTES", "16"), 16);
	}

	public static CryptoConfig getConfig() {
		return INSTANCE;
	}

	public String getPepper() {
		return pepper;
	}

	public int getIterations() {
		return iterations;
	}

	public int getKeyLengthBits() {
		return keyLengthBits;
	}

	public int getSaltBytes() {
		return saltBytes;
	}

	@Override
	public String toString() {
		return "CryptoConfig{iterations=%d, keyLenBits=%d, saltBytes=%d}".formatted(iterations, keyLengthBits,
				saltBytes);
	}
}
