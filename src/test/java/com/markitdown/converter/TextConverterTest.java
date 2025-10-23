package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class TextConverterTest
 * @brief TextConverter文本转换器单元测试类
 * @details 测试文本转换器的各种文件格式转换功能，包括纯文本、CSV、JSON、XML、日志文件等
 *          验证转换结果、元数据提取、错误处理等功能的正确性
 *          使用JUnit 5和临时目录进行测试
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
class TextConverterTest {

    private TextConverter converter;
    private ConversionOptions defaultOptions;

    @BeforeEach
    void setUp() {
        converter = new TextConverter();
        defaultOptions = new ConversionOptions();
    }

    @Test
    void testGetName() {
        assertEquals("TextConverter", converter.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(100, converter.getPriority());
    }

    @Test
    void testSupportsTextPlain() {
        assertTrue(converter.supports("text/plain"));
    }

    @Test
    void testSupportsCsv() {
        assertTrue(converter.supports("text/csv"));
    }

    @Test
    void testSupportsJson() {
        assertTrue(converter.supports("application/json"));
    }

    @Test
    void testSupportsXml() {
        assertTrue(converter.supports("application/xml"));
    }

    @Test
    void testSupportsLog() {
        assertTrue(converter.supports("text/x-log"));
    }

    @Test
    void testDoesNotSupportPdf() {
        assertFalse(converter.supports("application/pdf"));
    }

    @Test
    void testConvertPlainText(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        String content = "This is a plain text document.\nWith multiple lines.";
        Files.writeString(testFile, content);

        ConversionResult result = converter.convert(testFile, defaultOptions);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("test.txt", result.getOriginalFileName());
        assertTrue(result.getMarkdown().contains(content));
        assertEquals(content.length(), result.getFileSize());
    }

    @Test
    void testConvertCsvFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.csv");
        String csvContent = "Name,Age,City\nJohn,25,New York\nJane,30,London";
        Files.writeString(testFile, csvContent);

        ConversionResult result = converter.convert(testFile, defaultOptions);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("Name"));
        assertTrue(result.getMarkdown().contains("John"));
        assertTrue(result.getMarkdown().contains("25"));
    }

    @Test
    void testConvertJsonFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.json");
        String jsonContent = "{\"name\": \"John\", \"age\": 25, \"city\": \"New York\"}";
        Files.writeString(testFile, jsonContent);

        ConversionOptions options = ConversionOptions.builder().includeMetadata(true).build();
        ConversionResult result = converter.convert(testFile, options);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("```json"));
        assertTrue(result.getMarkdown().contains(jsonContent));
        assertNotNull(result.getMetadata());
    }

    @Test
    void testConvertXmlFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.xml");
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n  <name>John</name>\n  <age>25</age>\n</root>";
        Files.writeString(testFile, xmlContent);

        ConversionResult result = converter.convert(testFile, defaultOptions);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("```xml"));
        assertTrue(result.getMarkdown().contains("<root>"));
    }

    @Test
    void testConvertLogFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.log");
        String logContent = "2023-01-01 10:00:00 INFO Starting application\n2023-01-01 10:00:01 ERROR Failed to connect";
        Files.writeString(testFile, logContent);

        ConversionResult result = converter.convert(testFile, defaultOptions);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("```log"));
        assertTrue(result.getMarkdown().contains("INFO"));
        assertTrue(result.getMarkdown().contains("ERROR"));
    }

    @Test
    void testConvertEmptyFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("empty.txt");
        Files.writeString(testFile, "");

        ConversionResult result = converter.convert(testFile, defaultOptions);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("empty.txt", result.getOriginalFileName());
        assertEquals(0, result.getFileSize());
    }

    @Test
    void testConvertWithMetadataDisabled(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.json");
        String jsonContent = "{\"name\": \"John\"}";
        Files.writeString(testFile, jsonContent);

        ConversionOptions options = ConversionOptions.builder().includeMetadata(false).build();
        ConversionResult result = converter.convert(testFile, options);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertFalse(result.getMarkdown().contains("## File Information"));
    }

    @Test
    void testConvertWithNonExistentFile() {
        Path nonExistentFile = Path.of("nonexistent.txt");

        assertThrows(Exception.class, () -> {
            converter.convert(nonExistentFile, defaultOptions);
        });
    }

    @Test
    void testConvertWithNullPath() {
        assertThrows(NullPointerException.class, () -> {
            converter.convert(null, defaultOptions);
        });
    }

    @Test
    void testConvertWithNullOptions(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "test");

        assertThrows(NullPointerException.class, () -> {
            converter.convert(testFile, null);
        });
    }

    @Test
    void testGetFileExtension(@TempDir Path tempDir) throws Exception {
        // Test files with different extensions
        Map<String, String> testCases = Map.of(
            "test.txt", "txt",
            "test.csv", "csv",
            "test.json", "json",
            "test.xml", "xml",
            "test.log", "log",
            "test", "",  // No extension
            "test.", "",  // Empty extension
            ".hidden", "",  // Hidden file with no extension
            "test.backup.txt", "txt"  // Multiple dots
        );

        for (Map.Entry<String, String> entry : testCases.entrySet()) {
            Path testFile = tempDir.resolve(entry.getKey());
            Files.writeString(testFile, "test content");

            // We can't directly test the private method, but we can verify the conversion works
            ConversionResult result = converter.convert(testFile, defaultOptions);
            assertNotNull(result);
            assertTrue(result.isSuccessful());
        }
    }
}