package com.markitdown.core.markdown;

/**
 * Configuration options for Markdown engine behavior.
 * Allows fine-tuning of markdown generation process.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineConfig {

    private boolean enableSyntaxHighlighting = true;
    private boolean enableSmartLists = true;
    private boolean enableTableOptimization = true;
    private boolean enableEmojiSupport = true;
    private boolean enableMathSupport = false;
    private int maxNestingLevel = 6;
    private String defaultTableFormat = "github";
    private boolean enableCodeFolding = false;

    /**
     * Creates a new markdown engine configuration with default settings.
     */
    public MarkdownEngineConfig() {
    }

    /**
     * Creates a new configuration as copy of existing one.
     *
     * @param other configuration to copy
     */
    public MarkdownEngineConfig(MarkdownEngineConfig other) {
        this.enableSyntaxHighlighting = other.enableSyntaxHighlighting;
        this.enableSmartLists = other.enableSmartLists;
        this.enableTableOptimization = other.enableTableOptimization;
        this.enableEmojiSupport = other.enableEmojiSupport;
        this.enableMathSupport = other.enableMathSupport;
        this.maxNestingLevel = other.maxNestingLevel;
        this.defaultTableFormat = other.defaultTableFormat;
        this.enableCodeFolding = other.enableCodeFolding;
    }

    // Getters and setters

    public boolean isEnableSyntaxHighlighting() {
        return enableSyntaxHighlighting;
    }

    public MarkdownEngineConfig setEnableSyntaxHighlighting(boolean enableSyntaxHighlighting) {
        this.enableSyntaxHighlighting = enableSyntaxHighlighting;
        return this;
    }

    public boolean isEnableSmartLists() {
        return enableSmartLists;
    }

    public MarkdownEngineConfig setEnableSmartLists(boolean enableSmartLists) {
        this.enableSmartLists = enableSmartLists;
        return this;
    }

    public boolean isEnableTableOptimization() {
        return enableTableOptimization;
    }

    public MarkdownEngineConfig setEnableTableOptimization(boolean enableTableOptimization) {
        this.enableTableOptimization = enableTableOptimization;
        return this;
    }

    public boolean isEnableEmojiSupport() {
        return enableEmojiSupport;
    }

    public MarkdownEngineConfig setEnableEmojiSupport(boolean enableEmojiSupport) {
        this.enableEmojiSupport = enableEmojiSupport;
        return this;
    }

    public boolean isEnableMathSupport() {
        return enableMathSupport;
    }

    public MarkdownEngineConfig setEnableMathSupport(boolean enableMathSupport) {
        this.enableMathSupport = enableMathSupport;
        return this;
    }

    public int getMaxNestingLevel() {
        return maxNestingLevel;
    }

    public MarkdownEngineConfig setMaxNestingLevel(int maxNestingLevel) {
        this.maxNestingLevel = Math.max(1, Math.min(10, maxNestingLevel));
        return this;
    }

    public String getDefaultTableFormat() {
        return defaultTableFormat;
    }

    public MarkdownEngineConfig setDefaultTableFormat(String defaultTableFormat) {
        this.defaultTableFormat = defaultTableFormat;
        return this;
    }

    public boolean isEnableCodeFolding() {
        return enableCodeFolding;
    }

    public MarkdownEngineConfig setEnableCodeFolding(boolean enableCodeFolding) {
        this.enableCodeFolding = enableCodeFolding;
        return this;
    }

    /**
     * Creates a new builder for MarkdownEngineConfig.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern for MarkdownEngineConfig.
     */
    public static class Builder {
        private final MarkdownEngineConfig config = new MarkdownEngineConfig();

        public Builder enableSyntaxHighlighting(boolean enable) {
            config.setEnableSyntaxHighlighting(enable);
            return this;
        }

        public Builder enableSmartLists(boolean enable) {
            config.setEnableSmartLists(enable);
            return this;
        }

        public Builder enableTableOptimization(boolean enable) {
            config.setEnableTableOptimization(enable);
            return this;
        }

        public Builder enableEmojiSupport(boolean enable) {
            config.setEnableEmojiSupport(enable);
            return this;
        }

        public Builder enableMathSupport(boolean enable) {
            config.setEnableMathSupport(enable);
            return this;
        }

        public Builder maxNestingLevel(int level) {
            config.setMaxNestingLevel(level);
            return this;
        }

        public Builder defaultTableFormat(String format) {
            config.setDefaultTableFormat(format);
            return this;
        }

        public Builder enableCodeFolding(boolean enable) {
            config.setEnableCodeFolding(enable);
            return this;
        }

        public MarkdownEngineConfig build() {
            return new MarkdownEngineConfig(config);
        }
    }

    @Override
    public String toString() {
        return String.format("MarkdownEngineConfig{" +
                "syntaxHighlighting=%s, " +
                "smartLists=%s, " +
                "tableOptimization=%s, " +
                "emojiSupport=%s, " +
                "mathSupport=%s, " +
                "maxNesting=%d, " +
                "defaultTable='%s'}",
                enableSyntaxHighlighting,
                enableSmartLists,
                enableTableOptimization,
                enableEmojiSupport,
                enableMathSupport,
                maxNestingLevel,
                defaultTableFormat);
    }
}