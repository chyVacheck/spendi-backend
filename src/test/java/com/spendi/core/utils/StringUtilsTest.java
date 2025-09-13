
package com.spendi.core.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class StringUtilsTest {

	@Test
	void padString_withNullString_returnsPaddedSpaces() {
		assertEquals("    ", StringUtils.padString(null, 4));
	}

	@Test
	void padString_withShortString_returnsPaddedString() {
		assertEquals("abc  ", StringUtils.padString("abc", 5));
	}

	@Test
	void padString_withLongString_returnsOriginalString() {
		assertEquals("abcdef", StringUtils.padString("abcdef", 4));
	}

	@Test
	void padString_withEmptyString_returnsPaddedSpaces() {
		assertEquals("   ", StringUtils.padString("", 3));
	}

	@Test
	void padString_withZeroWidth_returnsOriginalString() {
		assertEquals("abc", StringUtils.padString("abc", 0));
	}

	@Test
	void padString_withNegativeWidth_returnsOriginalString() {
		assertEquals("abc", StringUtils.padString("abc", -5));
	}

	@Test
	void lowerOrNull_withMixedCaseString_returnsLowercaseString() {
		assertEquals("mixedcase", StringUtils.lowerOrNull("MiXeDcAsE"));
	}

	@Test
	void lowerOrNull_withOptionalPresent_returnsLowercaseString() {
		assertEquals("hello", StringUtils.lowerOrNull(Optional.of("Hello")));
	}

	@Test
	void lowerOrNull_withOptionalEmpty_returnsNull() {
		assertNull(StringUtils.lowerOrNull(Optional.empty()));
	}

	@Test
	void lowerOrNull_withNullOptional_returnsNull() {
		assertNull(StringUtils.lowerOrNull(Optional.ofNullable(null)));
	}

	@Test
	void lowerOrNull_withString_returnsLowercaseString() {
		assertEquals("world", StringUtils.lowerOrNull("World"));
	}

	@Test
	void lowerOrNull_withNullString_returnsNull() {
		assertNull(StringUtils.lowerOrNull((String) null));
	}

	@Test
	void lowerOrNull_withEmptyString_returnsEmptyString() {
		assertEquals("", StringUtils.lowerOrNull(""));
	}
}