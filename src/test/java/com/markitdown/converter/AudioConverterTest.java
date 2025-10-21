package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AudioConverter class.
 */
class AudioConverterTest {

    private AudioConverter audioConverter;
    private ConversionOptions options;

    @BeforeEach
    void setUp() {
        audioConverter = new AudioConverter();
        options = new ConversionOptions();
    }

    @Test
    void testGetName() {
        assertEquals("AudioConverter", audioConverter.getName());
    }

    @Test
    void testGetPriority() {
        assertEquals(30, audioConverter.getPriority());
    }

    @Test
    void testSupportsAudioMimeTypes() {
        assertTrue(audioConverter.supports("audio/mpeg"));
        assertTrue(audioConverter.supports("audio/mp3"));
        assertTrue(audioConverter.supports("audio/wav"));
        assertTrue(audioConverter.supports("audio/x-wav"));
        assertTrue(audioConverter.supports("audio/ogg"));
        assertTrue(audioConverter.supports("audio/x-flac"));
        assertTrue(audioConverter.supports("audio/mp4"));
        assertTrue(audioConverter.supports("audio/aac"));
    }

    @Test
    void testDoesNotSupportNonAudioMimeTypes() {
        assertFalse(audioConverter.supports("text/plain"));
        assertFalse(audioConverter.supports("application/pdf"));
        assertFalse(audioConverter.supports("image/jpeg"));
        assertFalse(audioConverter.supports("video/mp4"));
    }

    @Test
    void testIsSupportedFormat() {
        assertTrue(AudioConverter.isSupportedFormat("mp3"));
        assertTrue(AudioConverter.isSupportedFormat("wav"));
        assertTrue(AudioConverter.isSupportedFormat("ogg"));
        assertTrue(AudioConverter.isSupportedFormat("flac"));
        assertTrue(AudioConverter.isSupportedFormat("m4a"));
        assertTrue(AudioConverter.isSupportedFormat("aac"));
        assertFalse(AudioConverter.isSupportedFormat("txt"));
        assertFalse(AudioConverter.isSupportedFormat("pdf"));
    }

    @Test
    void testGetSupportedFormats() {
        var formats = AudioConverter.getSupportedFormats();
        assertNotNull(formats);
        assertTrue(formats.contains("mp3"));
        assertTrue(formats.contains("wav"));
        assertTrue(formats.contains("ogg"));
        assertTrue(formats.contains("flac"));
    }

    @Test
    void testConvertWithUnsupportedMimeType(@TempDir Path tempDir) throws IOException, ConversionException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is not an audio file");

        // Should handle gracefully even with unsupported file
        ConversionResult result = audioConverter.convert(testFile, options);

        assertNotNull(result);
        assertNotNull(result.getTextContent());
        assertNotNull(result.getMetadata());
        assertFalse(result.getWarnings().isEmpty());
    }
}