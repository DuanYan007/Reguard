package com.markdown.engine.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration options for Markdown generation operations.
 * This configuration is specifically designed for the Markdown Engine,
 * completely independent from document conversion concerns.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownConfig {

    private boolean includeTables = true;
    private boolean includeMetadata = false;
    private String tableFormat = "github"; // github, markdown, pipe
    private String listStyle = "dash"; // dash, asterisk, plus
    private String headingStyle = "atx"; // atx, setext
    private boolean escapeHtml = true;
    private boolean wrapCodeBlocks = true;
    private int maxListDepth = 10;
    private boolean sortMapKeys = false;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private Map<String, Object> customOptions = new HashMap<>();

    /**
     * Creates a new MarkdownConfig with default settings.
     */
    public MarkdownConfig() {
    }

    /**
     * Creates a new MarkdownConfig as a copy of existing config.
     *
     * @param other config to copy
     */
    public MarkdownConfig(MarkdownConfig other) {
        this.includeTables = other.includeTables;
        this.includeMetadata = other.includeMetadata;
        this.tableFormat = other.tableFormat;
        this.listStyle = other.listStyle;
        this.headingStyle = other.headingStyle;
        this.escapeHtml = other.escapeHtml;
        this.wrapCodeBlocks = other.wrapCodeBlocks;
        this.maxListDepth = other.maxListDepth;
        this.sortMapKeys = other.sortMapKeys;
        this.dateFormat = other.dateFormat;
        this.customOptions = new HashMap<>(other.customOptions);
    }

    // Table related configuration
    public boolean isIncludeTables() {
        return includeTables;
    }

    public MarkdownConfig setIncludeTables(boolean includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    public String getTableFormat() {
        return tableFormat;
    }

    public MarkdownConfig setTableFormat(String tableFormat) {
        this.tableFormat = validateTableFormat(tableFormat);
        return this;
    }

    // Metadata configuration
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public MarkdownConfig setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    // List style configuration
    public String getListStyle() {
        return listStyle;
    }

    public MarkdownConfig setListStyle(String listStyle) {
        this.listStyle = validateListStyle(listStyle);
        return this;
    }

    // Heading style configuration
    public String getHeadingStyle() {
        return headingStyle;
    }

    public MarkdownConfig setHeadingStyle(String headingStyle) {
        this.headingStyle = validateHeadingStyle(headingStyle);
        return this;
    }

    // HTML escape configuration
    public boolean isEscapeHtml() {
        return escapeHtml;
    }

    public MarkdownConfig setEscapeHtml(boolean escapeHtml) {
        this.escapeHtml = escapeHtml;
        return this;
    }

    // Code block configuration
    public boolean isWrapCodeBlocks() {
        return wrapCodeBlocks;
    }

    public MarkdownConfig setWrapCodeBlocks(boolean wrapCodeBlocks) {
        this.wrapCodeBlocks = wrapCodeBlocks;
        return this;
    }

    // List depth configuration
    public int getMaxListDepth() {
        return maxListDepth;
    }

    public MarkdownConfig setMaxListDepth(int maxListDepth) {
        this.maxListDepth = Math.max(1, maxListDepth);
        return this;
    }

    // Map sorting configuration
    public boolean isSortMapKeys() {
        return sortMapKeys;
    }

    public MarkdownConfig setSortMapKeys(boolean sortMapKeys) {
        this.sortMapKeys = sortMapKeys;
        return this;
    }

    // Date format configuration
    public String getDateFormat() {
        return dateFormat;
    }

    public MarkdownConfig setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat != null ? dateFormat : "yyyy-MM-dd HH:mm:ss";
        return this;
    }

    // Custom options
    public Map<String, Object> getCustomOptions() {
        return new HashMap<>(customOptions);
    }

    public MarkdownConfig setCustomOption(String key, Object value) {
        this.customOptions.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCustomOption(String key) {
        return (T) customOptions.get(key);
    }

    // Validation methods
    private String validateTableFormat(String format) {
        Set<String> validFormats = Set.of("github", "markdown", "pipe");
        return validFormats.contains(format) ? format : "github";
    }

    private String validateListStyle(String style) {
        Set<String> validStyles = Set.of("dash", "asterisk", "plus");
        return validStyles.contains(style) ? style : "dash";
    }

    private String validateHeadingStyle(String style) {
        Set<String> validStyles = Set.of("atx", "setext");
        return validStyles.contains(style) ? style : "atx";
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern for MarkdownConfig.
     */
    public static class Builder {
        private final MarkdownConfig config = new MarkdownConfig();

        private Builder() {}

        public Builder includeTables(boolean includeTables) {
            config.setIncludeTables(includeTables);
            return this;
        }

        public Builder includeMetadata(boolean includeMetadata) {
            config.setIncludeMetadata(includeMetadata);
            return this;
        }

        public Builder tableFormat(String tableFormat) {
            config.setTableFormat(tableFormat);
            return this;
        }

        public Builder listStyle(String listStyle) {
            config.setListStyle(listStyle);
            return this;
        }

        public Builder headingStyle(String headingStyle) {
            config.setHeadingStyle(headingStyle);
            return this;
        }

        public Builder escapeHtml(boolean escapeHtml) {
            config.setEscapeHtml(escapeHtml);
            return this;
        }

        public Builder wrapCodeBlocks(boolean wrapCodeBlocks) {
            config.setWrapCodeBlocks(wrapCodeBlocks);
            return this;
        }

        public Builder maxListDepth(int maxListDepth) {
            config.setMaxListDepth(maxListDepth);
            return this;
        }

        public Builder sortMapKeys(boolean sortMapKeys) {
            config.setSortMapKeys(sortMapKeys);
            return this;
        }

        public Builder dateFormat(String dateFormat) {
            config.setDateFormat(dateFormat);
            return this;
        }

        public Builder customOption(String key, Object value) {
            config.setCustomOption(key, value);
            return this;
        }

        public MarkdownConfig build() {
            return new MarkdownConfig(config);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarkdownConfig)) return false;

        MarkdownConfig that = (MarkdownConfig) o;
        return includeTables == that.includeTables &&
               includeMetadata == that.includeMetadata &&
               escapeHtml == that.escapeHtml &&
               wrapCodeBlocks == that.wrapCodeBlocks &&
               maxListDepth == that.maxListDepth &&
               sortMapKeys == that.sortMapKeys &&
               tableFormat.equals(that.tableFormat) &&
               listStyle.equals(that.listStyle) &&
               headingStyle.equals(that.headingStyle) &&
               dateFormat.equals(that.dateFormat) &&
               customOptions.equals(that.customOptions);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(includeTables);
        result = 31 * result + Boolean.hashCode(includeMetadata);
        result = 31 * result + tableFormat.hashCode();
        result = 31 * result + listStyle.hashCode();
        result = 31 * result + headingStyle.hashCode();
        result = 31 * result + Boolean.hashCode(escapeHtml);
        result = 31 * result + Boolean.hashCode(wrapCodeBlocks);
        result = 31 * result + maxListDepth;
        result = 31 * result + Boolean.hashCode(sortMapKeys);
        result = 31 * result + dateFormat.hashCode();
        result = 31 * result + customOptions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MarkdownConfig{" +
               "includeTables=" + includeTables +
               ", includeMetadata=" + includeMetadata +
               ", tableFormat='" + tableFormat + '\'' +
               ", listStyle='" + listStyle + '\'' +
               ", headingStyle='" + headingStyle + '\'' +
               ", escapeHtml=" + escapeHtml +
               ", wrapCodeBlocks=" + wrapCodeBlocks +
               ", maxListDepth=" + maxListDepth +
               ", sortMapKeys=" + sortMapKeys +
               ", dateFormat='" + dateFormat + '\'' +
               ", customOptions=" + customOptions +
               '}';
    }
}