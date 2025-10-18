package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PdfConverter.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
class PdfConverterTest {

    private PdfConverter converter;
    private ConversionOptions defaultOptions;

    @BeforeEach
    void setUp() {
        converter = new PdfConverter();
        defaultOptions = new ConversionOptions();
    }

    @Test
    void testGetName() {
        assertEquals("PdfConverter", converter.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(100, converter.getPriority());
    }

    @Test
    void testSupportsPdf() {
        assertTrue(converter.supports("application/pdf"));
    }

    @Test
    void testDoesNotSupportTextPlain() {
        assertFalse(converter.supports("text/plain"));
    }

    @Test
    void testDoesNotSupportDocx() {
        assertFalse(converter.supports("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    @Test
    void testConvertWithNullPath() {
        assertThrows(NullPointerException.class, () -> {
            converter.convert(null, defaultOptions);
        });
    }

    @Test
    void testConvertWithNullOptions(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.pdf");
        // Create a minimal PDF file (this would normally be a valid PDF)
        Files.writeString(testFile, "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n4 0 obj\n<< /Length 44 >>\nstream\nBT /F1 12 Tf 100 700 Td (Hello World) Tj ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000010 00000 n\n0000000079 00000 n\n0000000173 00000 n\n0000000301 00000 n\ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n398\n%%EOF");

        assertThrows(NullPointerException.class, () -> {
            converter.convert(testFile, null);
        });
    }

    @Test
    void testConvertNonExistentFile() {
        Path nonExistentFile = Path.of("nonexistent.pdf");

        assertThrows(Exception.class, () -> {
            converter.convert(nonExistentFile, defaultOptions);
        });
    }

    @Test
    void testConvertInvalidPdfFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("invalid.pdf");
        Files.writeString(testFile, "This is not a PDF file");

        assertThrows(Exception.class, () -> {
            converter.convert(testFile, defaultOptions);
        });
    }

    @Test
    void testConvertEmptyFile(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("empty.pdf");
        Files.writeString(testFile, "");

        assertThrows(Exception.class, () -> {
            converter.convert(testFile, defaultOptions);
        });
    }

    // Note: Testing with actual valid PDF files would require creating proper PDF documents
    // which is complex to do programmatically. In a real test suite, you would include
    // sample PDF files in the test resources directory.
}