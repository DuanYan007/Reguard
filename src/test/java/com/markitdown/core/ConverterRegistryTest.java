package com.markitdown.core;

import com.markitdown.api.DocumentConverter;
import com.markitdown.converter.TextConverter;
import com.markitdown.converter.PdfConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConverterRegistry.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
class ConverterRegistryTest {

    private ConverterRegistry registry;
    private TextConverter textConverter;
    private PdfConverter pdfConverter;

    @BeforeEach
    void setUp() {
        registry = new ConverterRegistry();
        textConverter = new TextConverter();
        pdfConverter = new PdfConverter();
    }

    @Test
    void testRegisterConverter() {
        registry.registerConverter(textConverter);

        Optional<DocumentConverter> result = registry.getConverter("text/plain");
        assertTrue(result.isPresent());
        assertEquals(textConverter, result.get());
    }

    @Test
    void testRegisterDuplicateConverter() {
        registry.registerConverter(textConverter);
        registry.registerConverter(new TextConverter()); // Same name

        assertEquals(1, registry.getConverterCount());
    }

    @Test
    void testUnregisterConverter() {
        registry.registerConverter(textConverter);
        registry.registerConverter(pdfConverter);

        boolean result = registry.unregisterConverter("TextConverter");
        assertTrue(result);
        assertEquals(1, registry.getConverterCount());

        Optional<DocumentConverter> textResult = registry.getConverter("text/plain");
        assertFalse(textResult.isPresent());

        Optional<DocumentConverter> pdfResult = registry.getConverter("application/pdf");
        assertTrue(pdfResult.isPresent());
    }

    @Test
    void testUnregisterNonExistentConverter() {
        boolean result = registry.unregisterConverter("NonExistentConverter");
        assertFalse(result);
    }

    @Test
    void testGetConverter() {
        registry.registerConverter(textConverter);

        Optional<DocumentConverter> result = registry.getConverter("text/plain");
        assertTrue(result.isPresent());
        assertEquals(textConverter, result.get());
    }

    @Test
    void testGetConverterNotFound() {
        Optional<DocumentConverter> result = registry.getConverter("application/unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void testIsSupported() {
        registry.registerConverter(textConverter);

        assertTrue(registry.isSupported("text/plain"));
        assertFalse(registry.isSupported("application/pdf"));
    }

    @Test
    void testGetSupportedMimeTypes() {
        registry.registerConverter(textConverter);
        registry.registerConverter(pdfConverter);

        Set<String> supportedTypes = registry.getSupportedMimeTypes();
        assertNotNull(supportedTypes);
        assertTrue(supportedTypes.contains("text/plain"));
        assertTrue(supportedTypes.contains("application/pdf"));
        assertTrue(supportedTypes.size() >= 2);
    }

    @Test
    void testGetConverterInfo() {
        registry.registerConverter(textConverter);
        registry.registerConverter(pdfConverter);

        var converterInfo = registry.getConverterInfo();
        assertNotNull(converterInfo);
        assertTrue(converterInfo.containsKey("TextConverter"));
        assertTrue(converterInfo.containsKey("PdfConverter"));
    }

    @Test
    void testGetConverterCount() {
        assertEquals(0, registry.getConverterCount());

        registry.registerConverter(textConverter);
        assertEquals(1, registry.getConverterCount());

        registry.registerConverter(pdfConverter);
        assertEquals(2, registry.getConverterCount());
    }

    @Test
    void testRegisterNullConverter() {
        assertThrows(NullPointerException.class, () -> {
            registry.registerConverter(null);
        });
    }

    @Test
    void testGetConverterWithNullMime() {
        assertThrows(NullPointerException.class, () -> {
            registry.getConverter(null);
        });
    }

    @Test
    void testIsSupportedWithNullMime() {
        assertThrows(NullPointerException.class, () -> {
            registry.isSupported(null);
        });
    }
}