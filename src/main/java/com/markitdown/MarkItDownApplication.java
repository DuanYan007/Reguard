package com.markitdown;

import com.markitdown.cli.MarkItDownCommand;
import com.markitdown.converter.*;
import com.markitdown.core.ConverterRegistry;
import com.markitdown.core.MarkItDownEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for MarkItDown Java.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkItDownApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownApplication.class);

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Delegate to CLI command
        MarkItDownCommand.main(args);
    }

    /**
     * Creates and configures a MarkItDownEngine with default converters.
     *
     * @return configured engine instance
     */
    private static MarkItDownEngine createEngine() {
        ConverterRegistry registry = new ConverterRegistry();

        // Register all available converters
        registry.registerConverter(new PdfConverter());
        registry.registerConverter(new DocxConverter());
        registry.registerConverter(new PptxConverter());
        registry.registerConverter(new XlsxConverter());
        registry.registerConverter(new HtmlConverter());
        registry.registerConverter(new ImageConverter());
        registry.registerConverter(new AudioConverter());
        registry.registerConverter(new TextConverter());

        return new MarkItDownEngine(registry);
    }
}