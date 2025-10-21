package com.markitdown.core.markdown;

import java.util.Set;

/**
 * Information about the Markdown engine capabilities and configuration.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class EngineInfo {

    private final String name;
    private final String version;
    private final Set<String> supportedFeatures;
    private final Set<String> supportedLanguages;

    /**
     * Creates engine information.
     *
     * @param name engine name
     * @param version engine version
     * @param supportedFeatures set of supported features
     * @param supportedLanguages set of supported languages
     */
    public EngineInfo(String name, String version, Set<String> supportedFeatures, Set<String> supportedLanguages) {
        this.name = name;
        this.version = version;
        this.supportedFeatures = supportedFeatures;
        this.supportedLanguages = supportedLanguages;
    }

    /**
     * Gets the engine name.
     *
     * @return engine name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the engine version.
     *
     * @return engine version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets supported features.
     *
     * @return set of supported features
     */
    public Set<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Gets supported languages for syntax highlighting.
     *
     * @return set of supported languages
     */
    public Set<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    @Override
    public String toString() {
        return String.format("MarkdownEngine{name='%s', version='%s', features=%d, languages=%d}",
                name, version, supportedFeatures.size(), supportedLanguages.size());
    }
}