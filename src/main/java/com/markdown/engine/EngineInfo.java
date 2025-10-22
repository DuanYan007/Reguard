package com.markdown.engine;

import java.util.Set;

/**
 * Information about the Markdown engine capabilities and version.
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
    private final String description;
    private final String author;

    /**
     * Creates engine information.
     *
     * @param name               engine name
     * @param version            engine version
     * @param supportedFeatures   set of supported features
     * @param supportedLanguages  set of supported syntax highlighting languages
     */
    public EngineInfo(String name, String version, Set<String> supportedFeatures, Set<String> supportedLanguages) {
        this(name, version, supportedFeatures, supportedLanguages, null, null);
    }

    /**
     * Creates engine information with full details.
     *
     * @param name               engine name
     * @param version            engine version
     * @param supportedFeatures   set of supported features
     * @param supportedLanguages  set of supported syntax highlighting languages
     * @param description        engine description
     * @param author             engine author
     */
    public EngineInfo(String name, String version, Set<String> supportedFeatures, Set<String> supportedLanguages,
                    String description, String author) {
        this.name = name != null ? name : "Unknown Engine";
        this.version = version != null ? version : "1.0.0";
        this.supportedFeatures = supportedFeatures != null ? Set.copyOf(supportedFeatures) : Set.of();
        this.supportedLanguages = supportedLanguages != null ? Set.copyOf(supportedLanguages) : Set.of();
        this.description = description;
        this.author = author;
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
     * Gets the supported features.
     *
     * @return immutable set of supported features
     */
    public Set<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Gets the supported syntax highlighting languages.
     *
     * @return immutable set of supported languages
     */
    public Set<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    /**
     * Gets the engine description.
     *
     * @return engine description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the engine author.
     *
     * @return engine author, or null if not set
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Checks if a specific feature is supported.
     *
     * @param feature the feature to check
     * @return true if the feature is supported
     */
    public boolean supportsFeature(String feature) {
        return supportedFeatures.contains(feature);
    }

    /**
     * Checks if a specific language is supported for syntax highlighting.
     *
     * @param language the language to check
     * @return true if the language is supported
     */
    public boolean supportsLanguage(String language) {
        return supportedLanguages.contains(language);
    }

    @Override
    public String toString() {
        return "EngineInfo{" +
               "name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", description='" + description + '\'' +
               ", author='" + author + '\'' +
               ", supportedFeatures=" + supportedFeatures +
               ", supportedLanguages=" + supportedLanguages +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineInfo)) return false;

        EngineInfo that = (EngineInfo) o;
        return name.equals(that.name) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}