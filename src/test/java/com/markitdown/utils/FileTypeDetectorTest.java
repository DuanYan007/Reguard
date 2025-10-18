package com.markitdown.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileTypeDetector.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
class FileTypeDetectorTest {

    @Test
    void testDetectTextPlainMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a plain text file");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/plain", mimeType);
    }

    @Test
    void testDetectCsvMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.csv");
        Files.writeString(testFile, "Name,Age,City\nJohn,25,NYC");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/csv", mimeType);
    }

    @Test
    void testDetectJsonMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.json");
        Files.writeString(testFile, "{\"name\": \"John\", \"age\": 25}");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/json", mimeType);
    }

    @Test
    void testDetectXmlMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.xml");
        Files.writeString(testFile, "<?xml version=\"1.0\"?><root><name>John</name></root>");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/xml", mimeType);
    }

    @Test
    void testDetectHtmlMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.html");
        Files.writeString(testFile, "<html><head><title>Test</title></head><body><p>Hello</p></body></html>");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/html", mimeType);
    }

    @Test
    void testDetectMarkdownMimeType(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.md");
        Files.writeString(testFile, "# Title\n\nThis is **markdown** content.");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/markdown", mimeType);
    }

    @Test
    void testDetectPdfMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.pdf");
        // Write some content (not a real PDF, but extension should be used)
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/pdf", mimeType);
    }

    @Test
    void testDetectDocxMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.docx");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", mimeType);
    }

    @Test
    void testDetectXlsxMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.xlsx");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", mimeType);
    }

    @Test
    void testDetectPptxMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.pptx");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", mimeType);
    }

    @Test
    void testDetectJpegMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.jpg");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/jpeg", mimeType);
    }

    @Test
    void testDetectJpegMimeTypeByAlternativeExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.jpeg");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/jpeg", mimeType);
    }

    @Test
    void testDetectPngMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.png");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/png", mimeType);
    }

    @Test
    void testDetectGifMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.gif");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/gif", mimeType);
    }

    @Test
    void testDetectBmpMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.bmp");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/bmp", mimeType);
    }

    @Test
    void testDetectTiffMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tiff");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/tiff", mimeType);
    }

    @Test
    void testDetectTifMimeTypeByAlternativeExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.tif");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/tiff", mimeType);
    }

    @Test
    void testDetectWebpMimeTypeByExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.webp");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("image/webp", mimeType);
    }

    @Test
    void testDetectUnknownFileExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.unknown");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("application/octet-stream", mimeType);
    }

    @Test
    void testDetectFileWithoutExtension(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test");
        Files.writeString(testFile, "Some content");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/plain", mimeType); // Default for content without extension
    }

    @Test
    void testDetectEmptyFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("empty.txt");
        Files.writeString(testFile, "");

        String mimeType = FileTypeDetector.detectMimeType(testFile);
        assertEquals("text/plain", mimeType);
    }

    @Test
    void testDetectNonExistentFile() {
        Path nonExistentFile = Path.of("nonexistent.txt");

        assertThrows(IOException.class, () -> {
            FileTypeDetector.detectMimeType(nonExistentFile);
        });
    }

    @Test
    void testDetectNullPath() {
        assertThrows(NullPointerException.class, () -> {
            FileTypeDetector.detectMimeType(null);
        });
    }

    @Test
    void testGetFileExtension() {
        assertEquals("txt", FileTypeDetector.getFileExtension("document.txt"));
        assertEquals("pdf", FileTypeDetector.getFileExtension("report.pdf"));
        assertEquals("docx", FileTypeDetector.getFileExtension("mydocument.docx"));
        assertEquals("", FileTypeDetector.getFileExtension("noextension"));
        assertEquals("", FileTypeDetector.getFileExtension("file."));
        assertEquals("", FileTypeDetector.getFileExtension(".hidden"));
        assertEquals("txt", FileTypeDetector.getFileExtension("file.backup.txt"));
    }

    @Test
    void testGetFileExtensionWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            FileTypeDetector.getFileExtension(null);
        });
    }

    @Test
    void testGetFileExtensionWithEmptyString() {
        assertEquals("", FileTypeDetector.getFileExtension(""));
        assertEquals("", FileTypeDetector.getFileExtension("   "));
    }

    @Test
    void testGetFileExtensionWithSpecialCharacters() {
        assertEquals("pdf", FileTypeDetector.getFileExtension("my document (1).pdf"));
        assertEquals("txt", FileTypeDetector.getFileExtension("file-with-dashes.txt"));
        assertEquals("json", FileTypeDetector.getFileExtension("file_with_underscores.json"));
        assertEquals("JPG", FileTypeDetector.getFileExtension("PHOTO.JPG")); // Case should be preserved
    }

    @Test
    void testGetFileNameWithoutExtension() {
        assertEquals("document", FileTypeDetector.getFileNameWithoutExtension("document.txt"));
        assertEquals("report", FileTypeDetector.getFileNameWithoutExtension("report.pdf"));
        assertEquals("mydocument", FileTypeDetector.getFileNameWithoutExtension("mydocument.docx"));
        assertEquals("noextension", FileTypeDetector.getFileNameWithoutExtension("noextension"));
        assertEquals("file", FileTypeDetector.getFileNameWithoutExtension("file."));
        assertEquals(".hidden", FileTypeDetector.getFileNameWithoutExtension(".hidden"));
        assertEquals("file.backup", FileTypeDetector.getFileNameWithoutExtension("file.backup.txt"));
    }

    @Test
    void testGetFileNameWithoutExtensionWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            FileTypeDetector.getFileNameWithoutExtension(null);
        });
    }

    @Test
    void testGetFileNameWithoutExtensionWithEmptyString() {
        assertEquals("", FileTypeDetector.getFileNameWithoutExtension(""));
        assertEquals("", FileTypeDetector.getFileNameWithoutExtension("   "));
    }

    @Test
    void testIsTextFile(@TempDir Path tempDir) throws IOException {
        // Test text files
        Path textFile = tempDir.resolve("test.txt");
        Files.writeString(textFile, "This is text");
        assertTrue(FileTypeDetector.isTextFile(textFile));

        Path mdFile = tempDir.resolve("test.md");
        Files.writeString(mdFile, "# Markdown");
        assertTrue(FileTypeDetector.isTextFile(mdFile));

        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "a,b,c\n1,2,3");
        assertTrue(FileTypeDetector.isTextFile(csvFile));

        // Test non-text files
        Path pdfFile = tempDir.resolve("test.pdf");
        Files.writeString(pdfFile, "PDF content");
        assertFalse(FileTypeDetector.isTextFile(pdfFile));

        Path imageFile = tempDir.resolve("test.jpg");
        Files.writeString(imageFile, "Image content");
        assertFalse(FileTypeDetector.isTextFile(imageFile));
    }

    @Test
    void testIsTextFileWithNullPath() {
        assertThrows(NullPointerException.class, () -> {
            FileTypeDetector.isTextFile(null);
        });
    }

    @Test
    void testGetSupportedExtensions() {
        var extensions = FileTypeDetector.getSupportedExtensions();
        assertNotNull(extensions);
        assertFalse(extensions.isEmpty());
        assertTrue(extensions.contains("txt"));
        assertTrue(extensions.contains("pdf"));
        assertTrue(extensions.contains("jpg"));
        assertTrue(extensions.contains("docx"));
    }

    @Test
    void testIsSupportedExtension() {
        assertTrue(FileTypeDetector.isSupportedExtension("txt"));
        assertTrue(FileTypeDetector.isSupportedExtension("pdf"));
        assertTrue(FileTypeDetector.isSupportedExtension("jpg"));
        assertTrue(FileTypeDetector.isSupportedExtension("docx"));
        assertFalse(FileTypeDetector.isSupportedExtension("unknown"));
        assertFalse(FileTypeDetector.isSupportedExtension("xyz"));
        assertFalse(FileTypeDetector.isSupportedExtension(""));
    }

    @Test
    void testIsSupportedExtensionWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            FileTypeDetector.isSupportedExtension(null);
        });
    }

    @Test
    void testIsSupportedExtensionCaseInsensitive() {
        assertTrue(FileTypeDetector.isSupportedExtension("TXT")); // Upper case
        assertTrue(FileTypeDetector.isSupportedExtension("Pdf")); // Mixed case
        assertTrue(FileTypeDetector.isSupportedExtension("JPG")); // Upper case
    }
}