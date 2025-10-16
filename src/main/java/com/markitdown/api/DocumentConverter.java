package com.markitdown.api;

import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;

import java.nio.file.Path;

/**
 * Interface for document converters that can convert various file formats to Markdown.
 *
 * @author MarkItDown Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DocumentConverter {

    /**
     * Converts a document file to Markdown format.
     *
     * @param filePath the path to the document file to convert
     * @param options  the conversion options to apply
     * @return the conversion result containing Markdown content and metadata
     * @throws ConversionException if the conversion fails
     */
    ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException;

    /**
     * Checks if this converter supports the specified MIME type.
     *
     * @param mimeType the MIME type to check
     * @return true if this converter supports the MIME type, false otherwise
     */
    boolean supports(String mimeType);

    /**
     * Gets the priority of this converter. Higher values indicate higher priority.
     * Used when multiple converters support the same MIME type.
     *
     * @return the priority value (default is 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Gets the name of this converter.
     *
     * @return the converter name
     */
    String getName();
}