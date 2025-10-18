package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Image converter that extracts text from images using OCR (Optical Character Recognition).
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class ImageConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);
    private static final Set<String> SUPPORTED_FORMATS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif");

    private final ITesseract tesseract;

    /**
     * Creates a new ImageConverter with default Tesseract instance.
     */
    public ImageConverter() {
        this.tesseract = new Tesseract();
    }

    /**
     * Creates a new ImageConverter with custom Tesseract instance.
     *
     * @param tesseract the Tesseract OCR engine
     */
    public ImageConverter(ITesseract tesseract) {
        this.tesseract = requireNonNull(tesseract, "Tesseract instance cannot be null");
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting image file: {}", filePath);

        try {
            // Load the image
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image == null) {
                throw new ConversionException("Cannot load image file: " + filePath,
                        filePath.getFileName().toString(), getName());
            }

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(filePath, image, options);

            // Perform OCR if enabled
            String extractedText = "";
            if (options.isUseOcr()) {
                extractedText = performOcr(image, options);
            } else {
                extractedText = "*OCR is disabled in conversion options*";
            }

            // Convert to Markdown
            String markdownContent = convertToMarkdown(extractedText, metadata, options, filePath);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to read image file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType.startsWith("image/");
    }

    @Override
    public int getPriority() {
        return 80; // Lower priority as OCR is resource-intensive
    }

    @Override
    public String getName() {
        return "ImageConverter";
    }

    /**
     * Extracts metadata from the image file.
     *
     * @param filePath the image file path
     * @param image    the loaded image
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(Path filePath, BufferedImage image, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // Image dimensions
            metadata.put("width", image.getWidth());
            metadata.put("height", image.getHeight());

            // File information
            metadata.put("fileName", filePath.getFileName().toString());
            metadata.put("fileSize", filePath.toFile().length());

            // Image format
            String fileName = filePath.getFileName().toString();
            String format = getFileExtension(fileName).toLowerCase();
            metadata.put("format", format);

            // Color information
            metadata.put("colorType", getColorType(image));
            metadata.put("conversionTime", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Performs OCR on the image using Tesseract.
     *
     * @param image   the image to process
     * @param options conversion options
     * @return extracted text
     * @throws ConversionException if OCR fails
     */
    private String performOcr(BufferedImage image, ConversionOptions options) throws ConversionException {
        try {
            // Set language for OCR
            String language = options.getLanguage();
            if (!"auto".equals(language) && !language.isEmpty()) {
                tesseract.setLanguage(language);
            }

            // Perform OCR
            String result = tesseract.doOCR(image);

            // Clean up the result
            return cleanupOcrResult(result);

        } catch (TesseractException e) {
            String errorMessage = "OCR processing failed: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, "image", getName());
        }
    }

    /**
     * Cleans up OCR result by fixing common issues.
     *
     * @param ocrText the raw OCR text
     * @return cleaned text
     */
    private String cleanupOcrResult(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }

        // Remove excessive whitespace
        String cleaned = ocrText.replaceAll("\\s+", " ");

        // Fix common OCR errors
        cleaned = cleaned.replaceAll("\\|", "I"); // Common confusion between pipe and I
        cleaned = cleaned.replaceAll("0", "O"); // Common confusion in some contexts

        // Remove leading/trailing whitespace
        return cleaned.trim();
    }

    /**
     * Converts extracted text to Markdown format.
     *
     * @param extractedText the OCR extracted text
     * @param metadata      the image metadata
     * @param options       conversion options
     * @param filePath      the original file path
     * @return Markdown formatted content
     */
    private String convertToMarkdown(String extractedText, Map<String, Object> metadata,
                                   ConversionOptions options, Path filePath) {
        StringBuilder markdown = new StringBuilder();

        // Add image reference if enabled
        if (options.isIncludeImages()) {
            String fileName = filePath.getFileName().toString();
            markdown.append("![Image](").append(fileName).append(")\n\n");
        }

        // Add metadata section if enabled
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Image Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Add extracted text content
        markdown.append("## Extracted Text\n\n");

        if (extractedText.isEmpty()) {
            markdown.append("*No text could be extracted from this image*\n\n");
        } else {
            // Format the extracted text for better readability
            String formattedText = formatExtractedText(extractedText);
            markdown.append(formattedText).append("\n\n");
        }

        return markdown.toString();
    }

    /**
     * Formats extracted text for better readability.
     *
     * @param text the extracted text
     * @return formatted text
     */
    private String formatExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Split into paragraphs and format
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder formatted = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                // Detect potential headings (short lines followed by longer text)
                if (trimmed.length() < 100 && trimmed.length() > 0 &&
                    Character.isUpperCase(trimmed.charAt(0))) {
                    formatted.append("### ").append(trimmed).append("\n\n");
                } else {
                    formatted.append(trimmed).append("\n\n");
                }
            }
        }

        return formatted.toString();
    }

    /**
     * Gets the color type of the image.
     *
     * @param image the image to analyze
     * @return color type description
     */
    private String getColorType(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB";
            case BufferedImage.TYPE_INT_BGR:
                return "BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "4BYTE_ABGR";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "Grayscale";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "Binary";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "USHORT_555_RGB";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "USHORT_565_RGB";
            default:
                return "Unknown (" + image.getType() + ")";
        }
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
     * Formats metadata keys for display.
     *
     * @param key the metadata key
     * @return formatted key
     */
    private String formatMetadataKey(String key) {
        // Convert camelCase to Title Case
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
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
     * Gets all supported image formats.
     *
     * @return a set of supported file extensions
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}