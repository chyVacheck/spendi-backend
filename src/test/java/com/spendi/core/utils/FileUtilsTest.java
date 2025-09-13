
package com.spendi.core.utils;

/**
 * ! lib imports
 */
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ! java imports
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileUtilsTest {

	@Test
	void constructor_shouldThrowUnsupportedOperationException() throws NoSuchMethodException {
		Constructor<InstantUtils> constructor = InstantUtils.class.getDeclaredConstructor();
		assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		assertThrows(InvocationTargetException.class, constructor::newInstance);
	}

	@TempDir
	Path tempDir;

	private Path testFile;

	@BeforeEach
	void setUp() throws IOException {
		testFile = tempDir.resolve("testFile.txt");
		Files.createFile(testFile);
		Files.writeString(testFile, "Hello World");
	}

	@AfterEach
	void tearDown() throws IOException {
		// Clean up temporary directory after each test
		Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(path -> {
			try {
				Files.delete(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	void createDir_createsDirectory() {
		Path newDir = tempDir.resolve("newDir");
		FileUtils.createDir(newDir.toString());
		assertTrue(Files.exists(newDir));
		assertTrue(Files.isDirectory(newDir));
	}

	@Test
	void createDir_createsNestedDirectories() {
		Path nestedDir = tempDir.resolve("parent").resolve("child");
		FileUtils.createDir(nestedDir.toString());
		assertTrue(Files.exists(nestedDir));
		assertTrue(Files.isDirectory(nestedDir));
	}

	@Test
	void createDirFromPath_createsParentDirectoryForFile() {
		Path fileInNewDir = tempDir.resolve("newParent").resolve("newFile.txt");
		FileUtils.createDirFromPath(fileInNewDir.toString());
		assertTrue(Files.exists(fileInNewDir.getParent()));
		assertTrue(Files.isDirectory(fileInNewDir.getParent()));
		assertFalse(Files.exists(fileInNewDir)); // Only parent dir should be created
	}

	@Test
	void getFileSize_returnsCorrectSizeForExistingFile() throws IOException {
		assertEquals(11, FileUtils.getFileSize(testFile.toString()));
	}

	@Test
	void getFileSize_returnsZeroForNonExistentFile() {
		assertEquals(0, FileUtils.getFileSize(tempDir.resolve("nonExistent.txt").toString()));
	}

	@Test
	void getFileExtension_returnsCorrectExtension() {
		assertEquals(".txt", FileUtils.getFileExtension("document.txt"));
		assertEquals(".jpeg", FileUtils.getFileExtension("image.jpeg"));
	}

	@Test
	void getFileExtension_returnsNullForNoExtension() {
		assertNull(FileUtils.getFileExtension("document"));
	}

	@Test
	void getFileExtension_returnsNullForEmptyExtension() {
		assertNull(FileUtils.getFileExtension("document."));
	}

	@Test
	void getFileExtension_returnsNullForNullFileName() {
		assertNull(FileUtils.getFileExtension(null));
	}

	@Test
	void deleteFile_deletesExistingFile() {
		FileUtils.deleteFile(testFile.toString());
		assertFalse(Files.exists(testFile));
	}

	@Test
	void deleteFile_doesNothingForNonExistentFile() {
		Path nonExistentFile = tempDir.resolve("nonExistent.txt");
		assertDoesNotThrow(() -> FileUtils.deleteFile(nonExistentFile.toString()));
		assertFalse(Files.exists(nonExistentFile));
	}

	@Test
	void moveFile_movesFileToNewLocation() throws IOException {
		Path destinationFile = tempDir.resolve("newLocation").resolve("movedFile.txt");
		FileUtils.moveFile(testFile.toString(), destinationFile.toString());

		assertFalse(Files.exists(testFile));
		assertTrue(Files.exists(destinationFile));
		assertEquals("Hello World", Files.readString(destinationFile));
	}

	@Test
	void moveFile_replacesExistingFileAtDestination() throws IOException {
		Path existingDestinationFile = tempDir.resolve("existingDestination.txt");
		Files.createFile(existingDestinationFile);
		Files.writeString(existingDestinationFile, "Old Content");

		FileUtils.moveFile(testFile.toString(), existingDestinationFile.toString());

		assertFalse(Files.exists(testFile));
		assertTrue(Files.exists(existingDestinationFile));
		assertEquals("Hello World", Files.readString(existingDestinationFile));
	}

	@Test
	void moveFile_createsDestinationDirectoryIfNotExist() throws IOException {
		Path destinationFile = tempDir.resolve("newDirForMove").resolve("movedFile.txt");
		FileUtils.moveFile(testFile.toString(), destinationFile.toString());

		assertTrue(Files.exists(destinationFile.getParent()));
		assertTrue(Files.exists(destinationFile));
	}
}