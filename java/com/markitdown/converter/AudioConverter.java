package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class AudioConverter
 * @brief 音频文件转换器，用于将音频文件转换为Markdown格式
 * @details 使用Apache Tika库提取音频文件元数据，支持多种音频格式
 *          提供转录功能框架，可集成语音转文本服务
 *          包含文件信息、音频元数据和转录建议的完整文档结构
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class AudioConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);

    /**
     * @brief 支持的音频格式集合
     * @details 包含所有此转换器支持的音频文件扩展名
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of("mp3", "wav", "ogg", "flac", "m4a", "aac");

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting audio file: {}", filePath);

        try {
            // Extract metadata from audio file
            Map<String, Object> metadata = extractAudioMetadata(filePath, options);

            // Generate transcription content (placeholder for now)
            String transcriptionContent = generateTranscription(filePath, options);

            // Convert to Markdown format
            String markdownContent = convertToMarkdown(filePath, metadata, transcriptionContent, options);

            List<String> warnings = new ArrayList<>();
            warnings.add("Audio transcription is not yet implemented. Consider using external speech-to-text services.");

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (Exception e) {
            String errorMessage = "Failed to process audio file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "audio/mpeg".equals(mimeType) ||
               "audio/mp3".equals(mimeType) ||
               "audio/wav".equals(mimeType) ||
               "audio/x-wav".equals(mimeType) ||
               "audio/ogg".equals(mimeType) ||
               "audio/x-flac".equals(mimeType) ||
               "audio/mp4".equals(mimeType) ||
               "audio/aac".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 30; // Medium priority
    }

    @Override
    public String getName() {
        return "AudioConverter";
    }

    /**
     * Extracts metadata from the audio file using Apache Tika.
     *
     * @param filePath the audio file path
     * @param options  conversion options
     * @return metadata map
     * @throws Exception if extraction fails
     */
    private Map<String, Object> extractAudioMetadata(Path filePath, ConversionOptions options) throws Exception {
        Map<String, Object> metadata = new HashMap<>();

        if (!options.isIncludeMetadata()) {
            return metadata;
        }

        String fileName = filePath.getFileName().toString();
        String fileExtension = getFileExtension(fileName).toLowerCase();

        // Basic file information
        metadata.put("fileName", fileName);
        metadata.put("fileSize", filePath.toFile().length());
        metadata.put("fileExtension", fileExtension);
        metadata.put("format", "audio/" + fileExtension);

        try {
            Tika tika = new Tika();
            String mimeType = tika.detect(filePath.toFile());
            metadata.put("detectedMimeType", mimeType);

            // Extract basic metadata using Tika
            String content = tika.parseToString(filePath.toFile());
            if (content != null && !content.trim().isEmpty()) {
                metadata.put("extractedContent", content.substring(0, Math.min(200, content.length())));
            }

            logger.debug("Successfully extracted metadata from audio file: {}", fileName);
        } catch (Exception e) {
            logger.warn("Failed to extract metadata using Tika: {}", e.getMessage());
            metadata.put("metadataExtractionError", e.getMessage());
        }

        metadata.put("conversionTime", LocalDateTime.now());
        return metadata;
    }

    /**
     * Generates transcription from audio file.
     * This is a placeholder implementation. In a production environment,
     * you would integrate with speech-to-text services like:
     * - Google Speech-to-Text
     * - AWS Transcribe
    * - Azure Speech Services
    * - OpenAI Whisper
     * - CMU Sphinx
     *
     * @param filePath the audio file path
     * @param options  conversion options
     * @return transcription text
     */
    private String generateTranscription(Path filePath, ConversionOptions options) {
        logger.warn("Audio transcription is not yet implemented for file: {}", filePath);

        // Placeholder content
        StringBuilder transcription = new StringBuilder();
        transcription.append("This audio file contains audio content that could be transcribed to text.\n\n");
        transcription.append("To enable audio transcription, consider integrating with speech-to-text services:\n");
        transcription.append("- Google Speech-to-Text API\n");
        transcription.append("- AWS Transcribe\n");
        transcription.append("- Azure Speech Services\n");
        transcription.append("- OpenAI Whisper API\n");
        transcription.append("- CMU Sphinx (offline)\n\n");
        transcription.append("File path: ").append(filePath.toString()).append("\n");

        return transcription.toString();
    }

    /**
     * Converts audio information and transcription to Markdown format.
     *
     * @param filePath           the audio file path
     * @param metadata           the extracted metadata
     * @param transcription      the transcription text
     * @param options            conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(Path filePath, Map<String, Object> metadata,
                                   String transcription, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        String fileName = filePath.getFileName().toString();
        String title = getFileNameWithoutExtension(fileName);

        // Add title
        markdown.append("# ").append(title).append("\n\n");

        // Add audio file information
        markdown.append("## Audio File Information\n\n");
        markdown.append("**File:** `").append(fileName).append("`\n\n");

        // Add metadata section if available
        if (!metadata.isEmpty()) {
            markdown.append("## Metadata\n\n");

            // File information
            markdown.append("### File Details\n\n");
            markdown.append("- **File Size:** ").append(formatFileSize((Long) metadata.get("fileSize"))).append("\n");
            markdown.append("- **Format:** ").append(metadata.get("format")).append("\n");

            // Audio metadata
            if (metadata.containsKey("title")) {
                markdown.append("- **Title:** ").append(metadata.get("title")).append("\n");
            }
            if (metadata.containsKey("artist")) {
                markdown.append("- **Artist:** ").append(metadata.get("artist")).append("\n");
            }
            if (metadata.containsKey("album")) {
                markdown.append("- **Album:** ").append(metadata.get("album")).append("\n");
            }
            if (metadata.containsKey("genre")) {
                markdown.append("- **Genre:** ").append(metadata.get("genre")).append("\n");
            }
            if (metadata.containsKey("year")) {
                markdown.append("- **Year:** ").append(metadata.get("year")).append("\n");
            }
            if (metadata.containsKey("duration")) {
                markdown.append("- **Duration:** ").append(metadata.get("duration")).append("\n");
            }

            markdown.append("\n");
        }

        // Add transcription section
        markdown.append("## Transcription\n\n");
        markdown.append(transcription);
        markdown.append("\n");

        // Add notes about transcription
        markdown.append("## Notes\n\n");
        markdown.append("*This is an audio file. For actual transcription, integrate with speech-to-text services.*\n\n");
        markdown.append("**Supported transcription services:**\n");
        markdown.append("- Google Speech-to-Text\n");
        markdown.append("- AWS Transcribe\n");
        markdown.append("- Azure Speech Services\n");
        markdown.append("- OpenAI Whisper\n\n");

        return markdown.toString();
    }

    
    /**
     * Gets the file extension from a file name.
     *
     * @param fileName the file name
     * @return the file extension (without the dot), or empty string if no extension
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

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
    private String getFileNameWithoutExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    /**
     * Formats file size for human readable display.
     *
     * @param fileSize the file size in bytes
     * @return formatted file size string
     */
    private String formatFileSize(long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Checks if a file format is supported by this converter.
     *
     * @param fileExtension the file extension
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * Gets all supported audio formats.
     *
     * @return a set of supported file extensions
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}