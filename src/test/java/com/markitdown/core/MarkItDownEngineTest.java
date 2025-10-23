package com.markitdown.core;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.converter.TextConverter;
import com.markitdown.exception.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class MarkItDownEngineTest
 * @brief MarkItDown引擎单元测试类
 * @details 测试核心引擎的文档转换功能，包括文件验证、转换器管理、
 *          错误处理等核心功能的正确性
 *          使用JUnit 5和临时目录进行测试
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
class MarkItDownEngineTest {

    private MarkItDownEngine engine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ConverterRegistry registry = new ConverterRegistry();
        registry.registerConverter(new TextConverter());
        engine = new MarkItDownEngine(registry);
    }

    @Test
    void testConvertWithValidTextFile() throws Exception {
        // Create a test text file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a test document.\nWith multiple lines.");

        // Convert the file
        ConversionResult result = engine.convert(testFile);

        // Assertions
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getMarkdown());
        assertTrue(result.getMarkdown().contains("This is a test document"));
        assertEquals("test.txt", result.getOriginalFileName());
        assertTrue(result.getFileSize() > 0);
    }


    @Test
    void testConvertWithNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("nonexistent.txt");

        ConversionException exception = assertThrows(ConversionException.class, () -> {
            engine.convert(nonExistentFile);
        });

        assertTrue(exception.getMessage().contains("File does not exist"));
    }

    @Test
    void testConvertWithCustomOptions() throws Exception {
        // Create a test text file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Test content");

        // Create custom options
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .maxFileSize(1024)
                .build();


        // Convert with custom options
        ConversionResult result = engine.convert(testFile, options);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("Test content"));
    }

    @Test
    void testConvertWithOversizedFile() throws Exception {
        // Create a test text file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Small content");

        // Set very small max file size
        ConversionOptions options = ConversionOptions.builder()
                .maxFileSize(10) // 10 bytes
                .build();

        ConversionException exception = assertThrows(ConversionException.class, () -> {
            engine.convert(testFile, options);
        });

        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    void testConvertWithUnreadableFile() throws Exception {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Test content");

        // Make file unreadable (on Unix-like systems)
        try {
            testFile.toFile().setReadable(false);

            ConversionException exception = assertThrows(ConversionException.class, () -> {
                engine.convert(testFile);
            });

            assertTrue(exception.getMessage().contains("not readable"));
        } catch (UnsupportedOperationException e) {
            // Skip this test on Windows where setReadable(false) might not work
        }
    }

    @Test
    void testIsSupported() throws Exception {
        // Create a test text file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Test content");

        assertTrue(engine.isSupported(testFile));
    }

    @Test
    void testIsSupportedWithNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("nonexistent.txt");
        assertFalse(engine.isSupported(nonExistentFile));
    }

    @Test
    void testGetSupportedMimeTypes() {
        var supportedTypes = engine.getSupportedMimeTypes();
        assertNotNull(supportedTypes);
        assertFalse(supportedTypes.isEmpty());
        assertTrue(supportedTypes.contains("text/plain"));
    }

    @Test
    void testGetConverterInfo() {
        var converterInfo = engine.getConverterInfo();
        assertNotNull(converterInfo);
        assertFalse(converterInfo.isEmpty());
        assertTrue(converterInfo.containsKey("TextConverter"));
    }

    @Test
    void testRegisterConverter() {
        // Get initial converter count
        int initialCount = engine.getConverterRegistry().getConverterCount();

        // Register a new converter
        engine.registerConverter(new TextConverter());

        // Verify converter was added
        assertEquals(initialCount + 1, engine.getConverterRegistry().getConverterCount());
    }

    @Test
    void testUnregisterConverter() {
        // Register a converter with a specific name
        TextConverter converter = new TextConverter();
        engine.registerConverter(converter);

        int initialCount = engine.getConverterRegistry().getConverterCount();

        // Unregister the converter
        boolean result = engine.unregisterConverter(converter.getName());

        // Verify converter was removed
        assertTrue(result);
        assertEquals(initialCount - 1, engine.getConverterRegistry().getConverterCount());
    }

    @Test
    void testConvertWithNullFilePath() {
        assertThrows(NullPointerException.class, () -> {
            engine.convert(null);
        });
    }

    @Test
    void testConvertWithNullOptions() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Test content");

        assertThrows(NullPointerException.class, () -> {
            engine.convert(testFile, null);
        });
    }
}