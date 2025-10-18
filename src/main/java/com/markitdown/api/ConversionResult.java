package com.markitdown.api;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the result of a document conversion operation.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
// checked
public class ConversionResult {

    private final String markdownContent;
    private final Map<String, Object> metadata;
    private final List<String> warnings;
    private final LocalDateTime conversionTime;
    private final long fileSize;
    private final String originalFileName;
    private final boolean successful;

    /**
     * Creates a new successful conversion result.
     *
     * @param markdownContent the converted Markdown content
     * @param metadata       conversion metadata
     * @param warnings       list of warnings that occurred during conversion
     * @param fileSize       the original file size in bytes
     * @param originalFileName the original file name
     */
    public ConversionResult(String markdownContent, Map<String, Object> metadata,
                           List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = markdownContent;
        this.metadata = new HashMap<>(metadata != null ? metadata : Collections.emptyMap());
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName;
        this.successful = true;
    }

    /**
     * Creates a new failed conversion result.
     *
     * @param warnings list of warnings/errors
     * @param fileSize the original file size in bytes
     * @param originalFileName the original file name
     */
    public ConversionResult(List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = "";
        this.metadata = Collections.emptyMap();
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName;
        this.successful = false;
    }

    /**
     * Gets the converted Markdown content.
     *
     * @return the Markdown content, or empty string if conversion failed
     */
    public String getTextContent() {
        return markdownContent;
    }

    /**
     * Gets the conversion metadata.
     *
     * @return an immutable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Gets the list of warnings.
     *
     * @return an immutable list of warnings
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Gets the time when the conversion was performed.
     *
     * @return the conversion timestamp
     */
    public LocalDateTime getConversionTime() {
        return conversionTime;
    }

    /**
     * Gets the original file size in bytes.
     *
     * @return the file size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the original file name.
     *
     * @return the original file name
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Checks if the conversion was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the Markdown content.
     *
     * @return the Markdown content
     */
    public String getMarkdown() {
        return markdownContent;
    }

    /**
     * Gets a specific metadata value.
     *
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Checks if there are any warnings.
     *
     * @return true if there are warnings, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "successful=" + successful +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileSize=" + fileSize +
                ", markdownContentLength=" + markdownContent.length() +
                ", warningsCount=" + warnings.size() +
                ", conversionTime=" + conversionTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversionResult that = (ConversionResult) o;
        return fileSize == that.fileSize &&
                successful == that.successful &&
                Objects.equals(markdownContent, that.markdownContent) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(warnings, that.warnings) &&
                Objects.equals(conversionTime, that.conversionTime) &&
                Objects.equals(originalFileName, that.originalFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markdownContent, metadata, warnings, conversionTime,
                          fileSize, originalFileName, successful);
    }
}