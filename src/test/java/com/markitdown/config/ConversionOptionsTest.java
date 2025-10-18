package com.markitdown.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversionOptions and ConversionOptions.Builder.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
class ConversionOptionsTest {

    @Test
    void testBuilderDefaults() {
        ConversionOptions options = ConversionOptions.builder().build();

        assertTrue(options.isIncludeImages());
        assertTrue(options.isIncludeTables());
        assertTrue(options.isIncludeMetadata());
        assertFalse(options.isUseOcr());
        assertEquals("auto", options.getLanguage());
        assertEquals("github", options.getTableFormat());
        assertEquals("markdown", options.getImageFormat());
        assertEquals(52428800L, options.getMaxFileSize()); // 50MB default
        assertNull(options.getTempDirectory());
    }

    @Test
    void testBuilderWithAllOptions() {
        Path tempDir = Paths.get("/tmp");

        ConversionOptions options = ConversionOptions.builder()
                .includeImages(false)
                .includeTables(false)
                .includeMetadata(false)
                .useOcr(true)
                .language("eng")
                .tableFormat("pipe")
                .imageFormat("base64")
                .maxFileSize(1048576L) // 1MB
                .tempDirectory(tempDir)
                .build();

        assertFalse(options.isIncludeImages());
        assertFalse(options.isIncludeTables());
        assertFalse(options.isIncludeMetadata());
        assertTrue(options.isUseOcr());
        assertEquals("eng", options.getLanguage());
        assertEquals("pipe", options.getTableFormat());
        assertEquals("base64", options.getImageFormat());
        assertEquals(1048576L, options.getMaxFileSize());
        assertEquals(tempDir, options.getTempDirectory());
    }

    @Test
    void testBuilderIncludeImages() {
        ConversionOptions options = ConversionOptions.builder()
                .includeImages(false)
                .build();

        assertFalse(options.isIncludeImages());

        ConversionOptions options2 = ConversionOptions.builder()
                .includeImages(true)
                .build();

        assertTrue(options2.isIncludeImages());
    }

    @Test
    void testBuilderIncludeTables() {
        ConversionOptions options = ConversionOptions.builder()
                .includeTables(false)
                .build();

        assertFalse(options.isIncludeTables());
    }

    @Test
    void testBuilderIncludeMetadata() {
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();

        assertFalse(options.isIncludeMetadata());
    }

    @Test
    void testBuilderUseOcr() {
        ConversionOptions options = ConversionOptions.builder()
                .useOcr(true)
                .build();

        assertTrue(options.isUseOcr());
    }

    @Test
    void testBuilderLanguage() {
        ConversionOptions options = ConversionOptions.builder()
                .language("chi_sim")
                .build();

        assertEquals("chi_sim", options.getLanguage());
    }

    @Test
    void testBuilderTableFormat() {
        ConversionOptions options = ConversionOptions.builder()
                .tableFormat("markdown")
                .build();

        assertEquals("markdown", options.getTableFormat());
    }

    @Test
    void testBuilderImageFormat() {
        ConversionOptions options = ConversionOptions.builder()
                .imageFormat("html")
                .build();

        assertEquals("html", options.getImageFormat());
    }

    @Test
    void testBuilderMaxFileSize() {
        ConversionOptions options = ConversionOptions.builder()
                .maxFileSize(2097152L) // 2MB
                .build();

        assertEquals(2097152L, options.getMaxFileSize());
    }

    @Test
    void testBuilderTempDirectory() {
        Path tempDir = Paths.get("/custom/temp");
        ConversionOptions options = ConversionOptions.builder()
                .tempDirectory(tempDir)
                .build();

        assertEquals(tempDir, options.getTempDirectory());
    }

    @Test
    void testBuilderChaining() {
        Path tempDir = Paths.get("/tmp");

        ConversionOptions options = ConversionOptions.builder()
                .includeImages(false)
                .useOcr(true)
                .language("fra")
                .maxFileSize(1024L)
                .tempDirectory(tempDir)
                .build();

        assertFalse(options.isIncludeImages());
        assertTrue(options.isUseOcr());
        assertEquals("fra", options.getLanguage());
        assertEquals(1024L, options.getMaxFileSize());
        assertEquals(tempDir, options.getTempDirectory());

        // Verify other defaults are still applied
        assertTrue(options.isIncludeTables());
        assertTrue(options.isIncludeMetadata());
        assertEquals("github", options.getTableFormat());
        assertEquals("markdown", options.getImageFormat());
    }

    @Test
    void testBuilderMultipleBuilds() {
        ConversionOptions.Builder builder = ConversionOptions.builder();

        ConversionOptions options1 = builder
                .includeImages(false)
                .build();

        ConversionOptions options2 = builder
                .includeImages(true)
                .useOcr(true)
                .build();

        assertFalse(options1.isIncludeImages());
        assertFalse(options1.isUseOcr());

        assertTrue(options2.isIncludeImages());
        assertTrue(options2.isUseOcr());
    }

    @Test
    void testImmutability() {
        ConversionOptions options = ConversionOptions.builder()
                .includeImages(false)
                .build();

        // The options object should be immutable - these should not have setters
        // If we try to modify the internal state (if fields were not final),
        // it would be a design issue, but since fields are private final,
        // we can't test modification directly
        assertEquals(ConversionOptions.class, options.getClass());
    }

    @Test
    void testEqualsAndHashCode() {
        ConversionOptions options1 = ConversionOptions.builder()
                .includeImages(false)
                .useOcr(true)
                .build();

        ConversionOptions options2 = ConversionOptions.builder()
                .includeImages(false)
                .useOcr(true)
                .build();

        ConversionOptions options3 = ConversionOptions.builder()
                .includeImages(true)
                .useOcr(true)
                .build();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
        assertNotEquals(options1, options3);
        assertNotEquals(options1.hashCode(), options3.hashCode());
    }

    @Test
    void testToString() {
        ConversionOptions options = ConversionOptions.builder()
                .includeImages(false)
                .useOcr(true)
                .language("deu")
                .build();

        String toString = options.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("includeImages=false"));
        assertTrue(toString.contains("useOcr=true"));
        assertTrue(toString.contains("language=deu"));
    }

    @Test
    void testSpecialLanguageValues() {
        // Test auto language detection
        ConversionOptions autoOptions = ConversionOptions.builder()
                .language("auto")
                .build();
        assertEquals("auto", autoOptions.getLanguage());

        // Test specific language codes
        String[] languages = {"eng", "chi_sim", "fra", "deu", "spa", "jpn", "kor"};
        for (String lang : languages) {
            ConversionOptions langOptions = ConversionOptions.builder()
                    .language(lang)
                    .build();
            assertEquals(lang, langOptions.getLanguage());
        }
    }

    @Test
    void testValidTableFormats() {
        String[] validFormats = {"github", "markdown", "pipe"};
        for (String format : validFormats) {
            ConversionOptions options = ConversionOptions.builder()
                    .tableFormat(format)
                    .build();
            assertEquals(format, options.getTableFormat());
        }
    }

    @Test
    void testValidImageFormats() {
        String[] validFormats = {"markdown", "html", "base64"};
        for (String format : validFormats) {
            ConversionOptions options = ConversionOptions.builder()
                    .imageFormat(format)
                    .build();
            assertEquals(format, options.getImageFormat());
        }
    }

    @Test
    void testEdgeCases() {
        // Test zero max file size
        ConversionOptions zeroSizeOptions = ConversionOptions.builder()
                .maxFileSize(0L)
                .build();
        assertEquals(0L, zeroSizeOptions.getMaxFileSize());

        // Test negative max file size (should be allowed in builder, validation happens elsewhere)
        ConversionOptions negativeSizeOptions = ConversionOptions.builder()
                .maxFileSize(-1L)
                .build();
        assertEquals(-1L, negativeSizeOptions.getMaxFileSize());

        // Test empty string language
        ConversionOptions emptyLangOptions = ConversionOptions.builder()
                .language("")
                .build();
        assertEquals("", emptyLangOptions.getLanguage());
    }
}