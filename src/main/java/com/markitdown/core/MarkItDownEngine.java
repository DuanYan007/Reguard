package com.markitdown.core;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import com.markitdown.utils.FileTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Core engine for document conversion operations.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkItDownEngine {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownEngine.class);

    private final ConverterRegistry converterRegistry;

    /**
     * Creates a new MarkItDownEngine with a default converter registry.
     */
    public MarkItDownEngine() {
        this.converterRegistry = new ConverterRegistry();
    }

    /**
     * Creates a new MarkItDownEngine with the specified converter registry.
     *
     * @param converterRegistry the converter registry to use
     */
    public MarkItDownEngine(ConverterRegistry converterRegistry) {
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
    }

    /**
     * Converts a document file to Markdown format using default options.
     *
     * @param filePath the path to the document file to convert
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     */
    public ConversionResult convert(Path filePath) throws ConversionException {
        return convert(filePath, new ConversionOptions());
    }

    /**
     * Converts a document file to Markdown format.
     *
     * @param filePath the path to the document file to convert
     * @param options  the conversion options to apply
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     */
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(options, "Conversion options cannot be null");

        logger.info("Starting conversion for file: {}", filePath);

        // Validate file
        validateFile(filePath, options);

        try {
            // Detect MIME type
            String mimeType = FileTypeDetector.detectMimeType(filePath);
            logger.debug("Detected MIME type: {}", mimeType);

            // Find appropriate converter
            Optional<DocumentConverter> converterOpt = converterRegistry.getConverter(mimeType);
            if (!converterOpt.isPresent()) {
                String supportedTypes = converterRegistry.getSupportedMimeTypes().toString();
                String errorMessage = String.format(
                        "No converter found for MIME type '%s'. Supported types: %s",
                        mimeType, supportedTypes);
                logger.error(errorMessage);

                // Safely get file size and name
                long fileSize = 0;
                String fileName = "unknown";
                try {
                    fileSize = Files.size(filePath);
                    fileName = filePath.getFileName().toString();
                } catch (Exception e) {
                    logger.warn("Cannot get file info: {}", e.getMessage());
                }

                return new ConversionResult(
                        List.of(errorMessage),
                        fileSize,
                        fileName
                );
            }

            DocumentConverter converter = converterOpt.get();
            logger.debug("Using converter: {}", converter.getName());

            // Perform conversion
            ConversionResult result = converter.convert(filePath, options);

            if (result.isSuccessful()) {
                logger.info("Successfully converted file: {} ({} bytes)", filePath, result.getFileSize());
            } else {
                logger.warn("Conversion completed with warnings for file: {}", filePath);
            }

            return result;

        } catch (IOException e) {
            String errorMessage = "I/O error during conversion: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), "unknown");
        } catch (Exception e) {
            String errorMessage = "Unexpected error during conversion: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), "unknown");
        }
    }

    /**
     * Validates the input file and options.
     *
     * @param filePath the file path to validate
     * @param options  the conversion options
     * @throws ConversionException if validation fails
     */
    private void validateFile(Path filePath, ConversionOptions options) throws ConversionException {
        // Check if file exists
        if (!Files.exists(filePath)) {
            throw new ConversionException("File does not exist: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }

        // Check if it's a regular file
        if (!Files.isRegularFile(filePath)) {
            throw new ConversionException("Path is not a regular file: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }

        // Check file size
        try {
            long fileSize = Files.size(filePath);
            long maxFileSize = options.getMaxFileSize();
            if (fileSize > maxFileSize) {
                String errorMessage = String.format(
                        "File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                        fileSize, maxFileSize);
                throw new ConversionException(errorMessage,
                        filePath.getFileName().toString(), "validation");
            }
        } catch (IOException e) {
            throw new ConversionException("Cannot determine file size: " + e.getMessage(),
                    filePath.getFileName().toString(), "validation");
        }

        // Check if file is readable
        if (!Files.isReadable(filePath)) {
            throw new ConversionException("File is not readable: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }
    }

    /**
     * Registers a document converter with the engine.
     *
     * @param converter the converter to register
     */
    public void registerConverter(DocumentConverter converter) {
        converterRegistry.registerConverter(converter);
    }

    /**
     * Unregisters a document converter by name.
     *
     * @param converterName the name of the converter to unregister
     * @return true if the converter was found and removed, false otherwise
     */
    public boolean unregisterConverter(String converterName) {
        return converterRegistry.unregisterConverter(converterName);
    }

    /**
     * Gets the converter registry.
     *
     * @return the converter registry
     */
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    /**
     * Checks if a file type is supported.
     *
     * @param filePath the file path to check
     * @return true if supported, false otherwise
     */
    public boolean isSupported(Path filePath) {
        try {
            String mimeType = FileTypeDetector.detectMimeType(filePath);
            return converterRegistry.isSupported(mimeType);
        } catch (IOException e) {
            logger.warn("Cannot determine MIME type for file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Gets all supported MIME types.
     *
     * @return a set of supported MIME types
     */
    public java.util.Set<String> getSupportedMimeTypes() {
        return converterRegistry.getSupportedMimeTypes();
    }

    /**
     * Gets information about all registered converters.
     *
     * @return a map of converter names to their information
     */
    public java.util.Map<String, String> getConverterInfo() {
        return converterRegistry.getConverterInfo();
    }
}