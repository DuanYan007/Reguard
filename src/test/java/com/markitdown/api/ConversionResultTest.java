package com.markitdown.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversionResult.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
class ConversionResultTest {

    @Test
    void testSuccessfulConversionResult() {
        String markdown = "# Test Document\n\nThis is test content.";
        Map<String, Object> metadata = Map.of(
            "title", "Test Document",
            "author", "Test Author"
        );
        List<String> warnings = List.of("Minor formatting issue");
        long fileSize = 1024L;
        String fileName = "test.txt";

        ConversionResult result = new ConversionResult(markdown, metadata, warnings, fileSize, fileName);

        assertEquals(markdown, result.getMarkdown());
        assertEquals(metadata, result.getMetadata());
        assertEquals(warnings, result.getWarnings());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(fileName, result.getOriginalFileName());
        assertTrue(result.isSuccessful());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.getWarnings().size());
    }

    @Test
    void testSuccessfulConversionResultWithoutWarnings() {
        String markdown = "# Simple Document\n\nContent here.";
        Map<String, Object> metadata = new HashMap<>();
        long fileSize = 512L;
        String fileName = "simple.md";

        ConversionResult result = new ConversionResult(markdown, metadata, new ArrayList<>(), fileSize, fileName);

        assertEquals(markdown, result.getMarkdown());
        assertEquals(metadata, result.getMetadata());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(fileName, result.getOriginalFileName());
        assertTrue(result.isSuccessful());
        assertFalse(result.hasWarnings());
    }

    @Test
    void testFailedConversionResult() {
        List<String> errors = List.of(
            "Unsupported file type",
            "File is corrupted"
        );
        long fileSize = 0L;
        String fileName = "corrupted.pdf";

        ConversionResult result = new ConversionResult(errors, fileSize, fileName);

        assertEquals("", result.getMarkdown());
        assertEquals(Collections.emptyMap(), result.getMetadata());
        assertEquals(errors, result.getWarnings());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(fileName, result.getOriginalFileName());
        assertFalse(result.isSuccessful());
        assertTrue(result.hasWarnings());
        assertEquals(2, result.getWarnings().size());
    }

    @Test
    void testEmptyErrorList() {
        List<String> errors = List.of();
        long fileSize = 100L;
        String fileName = "empty.txt";

        ConversionResult result = new ConversionResult(errors, fileSize, fileName);

        // Empty error list should be considered successful
        assertTrue(result.isSuccessful());
        assertFalse(result.hasWarnings());
        assertEquals(0, result.getWarnings().size());
    }

    @Test
    void testConversionResultWithNullValues() {
        ConversionResult result = new ConversionResult(null, null, null, 0L, null);

        assertEquals("", result.getMarkdown());
        assertEquals(Collections.emptyMap(), result.getMetadata());
        assertEquals(Collections.emptyList(), result.getWarnings());
        assertNull(result.getOriginalFileName());
        assertEquals(0L, result.getFileSize());
        // Null markdown should be considered failed
        assertFalse(result.isSuccessful());
        // Null warnings should not cause hasWarnings to return true
        assertFalse(result.hasWarnings());
    }

    @Test
    void testGetOriginalFileName() {
        ConversionResult result = new ConversionResult(
            "# Test\n\nContent",
            Map.of(),
            List.of(),
            100L,
            "test.txt"
        );

        assertEquals("test.txt", result.getOriginalFileName());
    }

    @Test
    void testLargeFile() {
        String largeContent = "# Large Document\n\n" + "A".repeat(1000000);
        Map<String, Object> metadata = Map.of("size", "large");
        long fileSize = 1000000L;
        String fileName = "large.txt";

        ConversionResult result = new ConversionResult(largeContent, metadata, List.of(), fileSize, fileName);

        assertEquals(largeContent, result.getMarkdown());
        assertEquals(metadata, result.getMetadata());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(fileName, result.getOriginalFileName());
        assertTrue(result.isSuccessful());
        assertFalse(result.hasWarnings());
    }

    @Test
    void testManyWarnings() {
        List<String> manyWarnings = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyWarnings.add("Warning " + i);
        }

        ConversionResult result = new ConversionResult(
            "# Content\n\nSome content",
            Map.of(),
            manyWarnings,
            500L,
            "many-warnings.txt"
        );

        assertTrue(result.isSuccessful());
        assertTrue(result.hasWarnings());
        assertEquals(100, result.getWarnings().size());
    }

    @Test
    void testComplexMetadata() {
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("title", "Complex Document");
        complexMetadata.put("pageCount", 42);
        complexMetadata.put("hasImages", true);
        complexMetadata.put("tags", List.of("important", "draft", "review"));
        complexMetadata.put("nested", Map.of("key1", "value1", "key2", 42));

        ConversionResult result = new ConversionResult(
            "# Complex\n\nDocument with complex metadata",
            complexMetadata,
            List.of(),
            2048L,
            "complex.docx"
        );

        assertEquals(complexMetadata, result.getMetadata());
        assertEquals("Complex Document", result.getMetadata().get("title"));
        assertEquals(42, result.getMetadata().get("pageCount"));
        assertTrue((Boolean) result.getMetadata().get("hasImages"));
        assertEquals(3, ((List<?>) result.getMetadata().get("tags")).size());
    }

    @Test
    void testGetSpecificMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Document");
        metadata.put("pageCount", 42);

        ConversionResult result = new ConversionResult(
            "# Test\n\nContent",
            metadata,
            List.of(),
            100L,
            "test.txt"
        );

        assertEquals("Test Document", result.<String>getMetadata("title"));
        assertEquals(42, result.<Integer>getMetadata("pageCount"));
        assertNull(result.getMetadata("nonexistent"));
    }

    @Test
    void testEqualsAndHashCode() {
        ConversionResult result1 = new ConversionResult(
            "# Test\n\nContent",
            Map.of("title", "Test"),
            List.of("Warning 1"),
            100L,
            "test.txt"
        );

        ConversionResult result2 = new ConversionResult(
            "# Test\n\nContent",
            Map.of("title", "Test"),
            List.of("Warning 1"),
            100L,
            "test.txt"
        );

        ConversionResult result3 = new ConversionResult(
            "# Different\n\nContent",
            Map.of("title", "Different"),
            List.of("Warning 2"),
            200L,
            "different.txt"
        );

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    @Test
    void testToString() {
        ConversionResult result = new ConversionResult(
            "# Test\n\nContent",
            Map.of("title", "Test Document"),
            List.of("Minor warning"),
            1024L,
            "test.txt"
        );

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test.txt"));
        assertTrue(toString.contains("1024"));
    }

    @Test
    void testFailedResultToString() {
        ConversionResult result = new ConversionResult(
            List.of("Error 1", "Error 2"),
            0L,
            "failed.pdf"
        );

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("failed.pdf"));
    }

    @Test
    void testGetTextContent() {
        String markdown = "# Test\n\nContent";
        ConversionResult result = new ConversionResult(
            markdown,
            Map.of(),
            List.of(),
            100L,
            "test.txt"
        );

        assertEquals(markdown, result.getTextContent());
        assertEquals(markdown, result.getMarkdown()); // Both should return the same content
    }

    @Test
    void testGetConversionTime() {
        long beforeTime = System.currentTimeMillis();

        ConversionResult result = new ConversionResult(
            "# Test\n\nContent",
            Map.of(),
            List.of(),
            100L,
            "test.txt"
        );

        long afterTime = System.currentTimeMillis();

        assertNotNull(result.getConversionTime());
        // Conversion time should be between before and after creation
        assertTrue(result.getConversionTime().toString().length() > 0);
    }

    @Test
    void testEdgeCaseFileSizes() {
        // Test zero file size
        ConversionResult zeroSize = new ConversionResult(
            "# Empty\n\nNo content",
            Map.of(),
            List.of(),
            0L,
            "empty.txt"
        );
        assertEquals(0L, zeroSize.getFileSize());

        // Test very large file size
        long veryLargeSize = Long.MAX_VALUE;
        ConversionResult largeSize = new ConversionResult(
            "# Large\n\nVery large file",
            Map.of(),
            List.of(),
            veryLargeSize,
            "large.txt"
        );
        assertEquals(veryLargeSize, largeSize.getFileSize());
    }
}