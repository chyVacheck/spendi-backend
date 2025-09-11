
/**
 * @file CryptoUtils.java
 * @module core/utils
 *
 * @description
 * Утилитарные функции для работы с криптографией.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.utils;

/**
 * ! lib imports
 */
import com.spendi.config.CryptoConfig;

/**
 * ! java imports
 */
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Утилиты для работы с криптографией.
 * Реализован в стиле static-only (без состояния).
 */
public final class CryptoUtils {
	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private CryptoUtils() {
	}

	private static final CryptoConfig CFG = new CryptoConfig();
	private static final String PEPPER = CFG.getPepper();
	private static final int ITERATIONS = CFG.getIterations();
	private static final int KEY_LENGTH_BITS = CFG.getKeyLengthBits();
	private static final int SALT_BYTES = CFG.getSaltBytes();

	private static final SecureRandom RNG = new SecureRandom();

	public static String hashPassword(String rawPassword) {
		Objects.requireNonNull(rawPassword);
		byte[] salt = new byte[SALT_BYTES];
		RNG.nextBytes(salt);
		byte[] derived = deriveKey(rawPassword, salt, ITERATIONS, KEY_LENGTH_BITS, PEPPER);
		return "pbkdf2$iter=" + ITERATIONS +
				"$salt=" + Base64.getEncoder().encodeToString(salt) +
				"$hash=" + Base64.getEncoder().encodeToString(derived);
	}

	public static boolean verifyPassword(String rawPassword, String stored) {
		try {
			ParsedHash ph = ParsedHash.parse(stored);
			byte[] derived = deriveKey(rawPassword, ph.salt, ph.iterations, KEY_LENGTH_BITS, PEPPER);
			return slowEquals(derived, ph.hash);
		} catch (Exception e) {
			return false;
		}
	}

	private static byte[] deriveKey(String password, byte[] salt, int iter, int keyLenBits, String pepper) {
		char[] chars = (password + pepper).toCharArray();
		PBEKeySpec spec = new PBEKeySpec(chars, salt, iter, keyLenBits);
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
					.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		} finally {
			Arrays.fill(chars, '\0');
		}
	}

	private static boolean slowEquals(byte[] a, byte[] b) {
		if (a == null || b == null || a.length != b.length)
			return false;
		int diff = 0;
		for (int i = 0; i < a.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}

	private record ParsedHash(int iterations, byte[] salt, byte[] hash) {
		static ParsedHash parse(String s) {
			String[] parts = s.split("\\$");
			int it = Integer.parseInt(parts[1].substring("iter=".length()));
			byte[] salt = Base64.getDecoder().decode(parts[2].substring("salt=".length()));
			byte[] hash = Base64.getDecoder().decode(parts[3].substring("hash=".length()));
			return new ParsedHash(it, salt, hash);
		}
	}
}
