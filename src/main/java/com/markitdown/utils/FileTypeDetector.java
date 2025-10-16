package com.markitdown.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class for detecting file types and MIME types based on file extensions and content.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
// checked
public class FileTypeDetector {

    private static final Map<String, String> EXTENSION_TO_MIME_TYPE;
    private static final Set<String> TEXT_FILE_EXTENSIONS;

    static {
        EXTENSION_TO_MIME_TYPE = new HashMap<>();

        // Text files
        EXTENSION_TO_MIME_TYPE.put("txt", "text/plain");
        EXTENSION_TO_MIME_TYPE.put("md", "text/markdown");
        EXTENSION_TO_MIME_TYPE.put("markdown", "text/markdown");
        EXTENSION_TO_MIME_TYPE.put("csv", "text/csv");

        // Microsoft Office
        EXTENSION_TO_MIME_TYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_TO_MIME_TYPE.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXTENSION_TO_MIME_TYPE.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_TO_MIME_TYPE.put("doc", "application/msword");
        EXTENSION_TO_MIME_TYPE.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME_TYPE.put("xls", "application/vnd.ms-excel");

        // PDF
        EXTENSION_TO_MIME_TYPE.put("pdf", "application/pdf");

        // Web formats
        EXTENSION_TO_MIME_TYPE.put("html", "text/html");
        EXTENSION_TO_MIME_TYPE.put("htm", "text/html");
        EXTENSION_TO_MIME_TYPE.put("xml", "application/xml");
        EXTENSION_TO_MIME_TYPE.put("json", "application/json");

        // Images
        EXTENSION_TO_MIME_TYPE.put("png", "image/png");
        EXTENSION_TO_MIME_TYPE.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("gif", "image/gif");
        EXTENSION_TO_MIME_TYPE.put("bmp", "image/bmp");
        EXTENSION_TO_MIME_TYPE.put("tiff", "image/tiff");
        EXTENSION_TO_MIME_TYPE.put("tif", "image/tiff");

        // Archives
        EXTENSION_TO_MIME_TYPE.put("zip", "application/zip");
        EXTENSION_TO_MIME_TYPE.put("rar", "application/x-rar-compressed");
        EXTENSION_TO_MIME_TYPE.put("7z", "application/x-7z-compressed");

        // E-books
        EXTENSION_TO_MIME_TYPE.put("epub", "application/epub+zip");
        EXTENSION_TO_MIME_TYPE.put("mobi", "application/x-mobipocket-ebook");

        TEXT_FILE_EXTENSIONS = new HashSet<>(Arrays.asList(
                "txt", "md", "markdown", "csv", "json", "xml", "html", "htm", "log"
        ));
    }

    /**
     * Detects the MIME type of a file based on its extension and content.
     *
     * @param filePath the path to the file
     * @return the detected MIME type, or "application/octet-stream" if unknown
     * @throws IOException if an I/O error occurs
     */
    public static String detectMimeType(Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "File path cannot be null");

        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);

        if (extension.isEmpty()) {
            // Try to detect by content
            return detectByContent(filePath);
        }

        String mimeType = EXTENSION_TO_MIME_TYPE.get(extension.toLowerCase());
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * Checks if a file is a text file based on its extension.
     *
     * @param filePath the path to the file
     * @return true if the file is a text file
     */
    public static boolean isTextFile(Path filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");

        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);

        return TEXT_FILE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Gets the file extension from a file name.
     *
     * @param fileName the file name
     * @return the file extension (without the dot), or empty string if no extension
     */
    public static String getFileExtension(String fileName) {
        Objects.requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * Gets the file name without extension.
     *
     * @param fileName the file name
     * @return the file name without extension
     */
    public static String getFileNameWithoutExtension(String fileName) {
        Objects.requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    /**
     * Detects MIME type by analyzing file content.
     *
     * @param filePath the path to the file
     * @return the detected MIME type
     * @throws IOException if an I/O error occurs
     */
    private static String detectByContent(Path filePath) throws IOException {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return "application/octet-stream";
        }

        byte[] header = new byte[1024];
        try {
            int bytesRead = Files.newInputStream(filePath).read(header);
            if (bytesRead <= 0) {
                return "application/octet-stream";
            }

            // Check for common file signatures
            String headerStr = new String(header, 0, Math.min(bytesRead, 100)).toLowerCase();

            // PDF signature
            if (headerStr.startsWith("%pdf")) {
                return "application/pdf";
            }

            // HTML signatures
            if (headerStr.contains("<!doctype") || headerStr.contains("<html")) {
                return "text/html";
            }

            // XML signatures
            if (headerStr.trim().startsWith("<?xml")) {
                return "application/xml";
            }

            // JSON signatures
            // Todo: Json格式可以更加细致地判断，容易将纯文本误判为JSON
            String trimmed = headerStr.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return "application/json";
            }

            // Default to text if it looks like readable text
            if (isTextContent(header, bytesRead)) {
                return "text/plain";
            }

        } catch (IOException e) {
            // If we can't read the file, return default
        }

        return "application/octet-stream";
    }

    /**
     * Checks if the given byte array contains text content.
     *
     * @param bytes     the byte array to check
     * @param byteCount the number of valid bytes in the array
     * @return true if the content appears to be text
     */
    private static boolean isTextContent(byte[] bytes, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            byte b = bytes[i];
            // Check for non-text characters (excluding common whitespace and control characters)
            if (b < 0x20 && b != '\t' && b != '\n' && b != '\r') {
                return false;
            }

            if (b > 0x7F && !isValidUtf8Continuation(bytes, i, byteCount)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a byte sequence is a valid UTF-8 continuation.
     *
     * @param bytes     the byte array
     * @param index     the current index
     * @param maxLength the maximum valid index
     * @return true if this is a valid UTF-8 continuation
     */
    /*
     UTF-8编码分为单字节，双字节，三字节和四字节
     除了单字节外，剩余三者除首字节外 其余字节称为继续字节 形如 10xxxxxx(8位)
     其余首字节格式:
     双字节  110xxxxx
     三字节  1110xxxx
     四字节  11110xxx
     */
    private static boolean isValidUtf8Continuation(byte[] bytes, int index, int maxLength) {
        byte b = bytes[index];
        if ((b & 0xC0) == 0x80) {
            // Continuation byte
            return true;
        } else if ((b & 0xE0) == 0xC0 && index + 1 < maxLength) {
            // Two-byte sequence
            return (bytes[index + 1] & 0xC0) == 0x80;
        } else if ((b & 0xF0) == 0xE0 && index + 2 < maxLength) {
            // Three-byte sequence
            return (bytes[index + 1] & 0xC0) == 0x80 && (bytes[index + 2] & 0xC0) == 0x80;
        } else if ((b & 0xF8) == 0xF0 && index + 3 < maxLength) {
            // Four-byte sequence
            return (bytes[index + 1] & 0xC0) == 0x80 &&
                   (bytes[index + 2] & 0xC0) == 0x80 &&
                   (bytes[index + 3] & 0xC0) == 0x80;
        }
        return false;
    }

    /**
     * Gets all supported file extensions.
     *
     * @return a set of supported file extensions
     */
    public static Set<String> getSupportedExtensions() {
        return new HashSet<>(EXTENSION_TO_MIME_TYPE.keySet());
    }

    /**
     * Checks if a file extension is supported.
     *
     * @param extension the file extension
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedExtension(String extension) {
        return EXTENSION_TO_MIME_TYPE.containsKey(extension.toLowerCase());
    }
}
