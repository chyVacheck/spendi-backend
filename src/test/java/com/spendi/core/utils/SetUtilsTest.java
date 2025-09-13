
package com.spendi.core.utils;

/**
 * ! lib imports
 */
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ! java imports
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetUtilsTest {

	@Test
	void constructor_shouldThrowUnsupportedOperationException() throws NoSuchMethodException {
		Constructor<InstantUtils> constructor = InstantUtils.class.getDeclaredConstructor();
		assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		assertThrows(InvocationTargetException.class, constructor::newInstance);
	}

	@Test
	void ofNonNull_varargs_withNullItspendi() {
		Set<String> expected = new HashSet<>(Arrays.asList("a", "b"));
		Set<String> actual = SetUtils.ofNonNull("a", null, "b", null);
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_varargs_withNoItspendi() {
		Set<String> expected = Collections.emptySet();
		Set<String> actual = SetUtils.ofNonNull();
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_varargs_withAllNullItspendi() {
		Set<String> expected = Collections.emptySet();
		Set<String> actual = SetUtils.ofNonNull(null, null);
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_varargs_withValidItspendi() {
		Set<String> expected = new HashSet<>(Arrays.asList("x", "y", "z"));
		Set<String> actual = SetUtils.ofNonNull("x", "y", "z");
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_iterable_withNullItspendi() {
		Set<String> expected = new HashSet<>(Arrays.asList("a", "b"));
		Set<String> actual = SetUtils.ofNonNull(Arrays.asList("a", null, "b", null));
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_iterable_withEmptyList() {
		Set<String> expected = Collections.emptySet();
		Set<String> actual = SetUtils.ofNonNull(Collections.emptyList());
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_iterable_withNullIterable() {
		Set<String> expected = Collections.emptySet();
		Set<String> actual = SetUtils.ofNonNull((Iterable<String>) null);
		assertEquals(expected, actual);
	}

	@Test
	void ofNonNull_iterable_withValidItspendi() {
		Set<String> expected = new HashSet<>(Arrays.asList("x", "y", "z"));
		Set<String> actual = SetUtils.ofNonNull(Arrays.asList("x", "y", "z"));
		assertEquals(expected, actual);
	}
}