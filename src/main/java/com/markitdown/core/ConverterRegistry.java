package com.markitdown.core;

import com.markitdown.api.DocumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing document converters. This class is thread-safe and follows a singleton pattern.
 *
 * @author MarkItDown Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConverterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConverterRegistry.class);

    // Maps MIME types to lists of converters that support them
    private final Map<String, List<DocumentConverter>> mimeTypeToConverters;

    // Maps converter names to converter instances
    private final Map<String, DocumentConverter> nameToConverter;

    /**
     * Creates a new ConverterRegistry.
     */
    public ConverterRegistry() {
        this.mimeTypeToConverters = new ConcurrentHashMap<>();
        this.nameToConverter = new ConcurrentHashMap<>();
    }

    /**
     * Registers a document converter.
     *
     * @param converter the converter to register
     * @throws IllegalArgumentException if the converter is null or already registered
     */
    public synchronized void registerConverter(DocumentConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");

        String name = converter.getName();
        if (nameToConverter.containsKey(name)) {
            throw new IllegalArgumentException("Converter with name '" + name + "' is already registered");
        }

        nameToConverter.put(name, converter);
        logger.info("Registered converter: {}", name);

        // We can't determine MIME types here since converters don't expose them directly
        // This will be handled by the getConverter method which checks each converter
    }

    /**
     * Unregisters a document converter by name.
     *
     * @param converterName the name of the converter to unregister
     * @return true if the converter was found and removed, false otherwise
     */
    public synchronized boolean unregisterConverter(String converterName) {
        DocumentConverter removed = nameToConverter.remove(converterName);
        if (removed != null) {
            logger.info("Unregistered converter: {}", converterName);
            // Clear the MIME type cache to force re-evaluation
            mimeTypeToConverters.clear();
            return true;
        }
        return false;
    }

    /**
     * Gets a converter that supports the specified MIME type.
     *
     * @param mimeType the MIME type to find a converter for
     * @return an optional containing the converter, or empty if no converter is found
     */
    public Optional<DocumentConverter> getConverter(String mimeType) {
        Objects.requireNonNull(mimeType, "MIME type cannot be null");

        // Check cache first
        List<DocumentConverter> converters = mimeTypeToConverters.get(mimeType);
        if (converters != null && !converters.isEmpty()) {
            return Optional.of(converters.get(0)); // Return the highest priority converter
        }

        // Find converters that support this MIME type
        List<DocumentConverter> matchingConverters = nameToConverter.values().stream()
                .filter(converter -> converter.supports(mimeType))
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())) // Higher priority first
                .collect(Collectors.toList());

        if (matchingConverters.isEmpty()) {
            logger.debug("No converter found for MIME type: {}", mimeType);
            return Optional.empty();
        }

        // Cache the result
        mimeTypeToConverters.put(mimeType, matchingConverters);
        logger.debug("Found {} converter(s) for MIME type: {}", matchingConverters.size(), mimeType);

        return Optional.of(matchingConverters.get(0));
    }

    /**
     * Gets all converters that support the specified MIME type.
     *
     * @param mimeType the MIME type to find converters for
     * @return a list of converters supporting the MIME type, ordered by priority
     */
    public List<DocumentConverter> getAllConverters(String mimeType) {
        Objects.requireNonNull(mimeType, "MIME type cannot be null");

        List<DocumentConverter> converters = nameToConverter.values().stream()
                .filter(converter -> converter.supports(mimeType))
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(converters);
    }

    /**
     * Gets a converter by name.
     *
     * @param name the name of the converter
     * @return an optional containing the converter, or empty if not found
     */
    public Optional<DocumentConverter> getConverterByName(String name) {
        Objects.requireNonNull(name, "Converter name cannot be null");
        return Optional.ofNullable(nameToConverter.get(name));
    }

    /**
     * Gets all registered converters.
     *
     * @return an unmodifiable collection of all registered converters
     */
    public Collection<DocumentConverter> getAllConverters() {
        return Collections.unmodifiableCollection(nameToConverter.values());
    }

    /**
     * Gets all supported MIME types.
     *
     * @return a set of supported MIME types
     */
    public Set<String> getSupportedMimeTypes() {
        Set<String> mimeTypes = new HashSet<>();

        for (DocumentConverter converter : nameToConverter.values()) {
            // We need to check common MIME types since converters don't expose their supported types
            // This is a limitation of the current design
            mimeTypes.addAll(getCommonMimeTypesForConverter(converter));
        }

        return Collections.unmodifiableSet(mimeTypes);
    }

    /**
     * Checks if a MIME type is supported by any registered converter.
     *
     * @param mimeType the MIME type to check
     * @return true if supported, false otherwise
     */
    public boolean isSupported(String mimeType) {
        return getConverter(mimeType).isPresent();
    }

    /**
     * Clears all registered converters.
     */
    public synchronized void clear() {
        logger.info("Clearing all converters from registry");
        nameToConverter.clear();
        mimeTypeToConverters.clear();
    }

    /**
     * Gets the number of registered converters.
     *
     * @return the number of converters
     */
    public int getConverterCount() {
        return nameToConverter.size();
    }

    /**
     * Gets information about all registered converters.
     *
     * @return a map of converter names to their information
     */
    public Map<String, String> getConverterInfo() {
        Map<String, String> info = new LinkedHashMap<>();

        for (Map.Entry<String, DocumentConverter> entry : nameToConverter.entrySet()) {
            DocumentConverter converter = entry.getValue();
            info.put(entry.getKey(),
                    String.format("Priority: %d, Class: %s",
                    converter.getPriority(),
                    converter.getClass().getSimpleName()));
        }

        return Collections.unmodifiableMap(info);
    }

    /**
     * Determines common MIME types that a converter might support.
     * This is a temporary solution until converters expose their supported MIME types.
     *
     * @param converter the converter to check
     * @return a set of possible MIME types
     */
    private Set<String> getCommonMimeTypesForConverter(DocumentConverter converter) {
        Set<String> mimeTypes = new HashSet<>();
        String className = converter.getClass().getSimpleName().toLowerCase();

        // Guess based on class name
        if (className.contains("pdf")) {
            mimeTypes.add("application/pdf");
        }
        if (className.contains("docx") || className.contains("word")) {
            mimeTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (className.contains("pptx") || className.contains("powerpoint")) {
            mimeTypes.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        }
        if (className.contains("xlsx") || className.contains("excel")) {
            mimeTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        if (className.contains("html")) {
            mimeTypes.add("text/html");
        }
        if (className.contains("text")) {
            mimeTypes.add("text/plain");
            mimeTypes.add("text/csv");
            mimeTypes.add("text/markdown");
        }
        if (className.contains("json")) {
            mimeTypes.add("application/json");
        }
        if (className.contains("xml")) {
            mimeTypes.add("application/xml");
        }
        if (className.contains("image")) {
            mimeTypes.addAll(Arrays.asList("image/png", "image/jpeg", "image/gif", "image/bmp"));
        }

        return mimeTypes;
    }

    @Override
    public String toString() {
        return "ConverterRegistry{" +
                "converterCount=" + nameToConverter.size() +
                ", supportedMimeTypes=" + getSupportedMimeTypes().size() +
                '}';
    }
}