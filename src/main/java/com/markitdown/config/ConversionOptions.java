package com.markitdown.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @class ConversionOptions
 * @brief 文档转换配置选项类，控制转换过程的各种行为
 * @details 提供丰富的配置选项来控制文档转换的各个方面
 *          包括内容包含策略、格式选项、OCR设置、文件大小限制等
 *          支持链式调用和Builder模式创建配置实例
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConversionOptions {

    // ==================== 实例变量 ====================

    /**
     * @brief 是否包含图片内容
     * @details 控制转换结果中是否包含图片相关信息
     */
    private boolean includeImages = true;

    /**
     * @brief 是否包含表格内容
     * @details 控制转换结果中是否包含表格数据
     */
    private boolean includeTables = true;

    /**
     * @brief 是否包含元数据信息
     * @details 控制转换结果中是否包含文档元数据
     */
    private boolean includeMetadata = true;

    /**
     * @brief 表格格式配置
     * @details 指定表格在Markdown中的格式风格，支持github、markdown、pipe三种格式
     */
    private String tableFormat = "github";

    /**
     * @brief 图片格式配置
     * @details 指定图片在Markdown中的表示方式，支持markdown、html、base64三种格式
     */
    private String imageFormat = "markdown";

    /**
     * @brief OCR语言配置
     * @details 指定OCR文本识别时使用的语言，默认为auto自动检测
     */
    private String language = "auto";

    /**
     * @brief 最大文件大小限制
     * @details 设置可转换文件的最大字节大小，默认为50MB
     */
    private long maxFileSize = 50 * 1024 * 1024;

    /**
     * @brief 临时目录配置
     * @details 指定转换过程中使用的临时目录路径
     */
    private Path tempDirectory;

    /**
     * @brief OCR使用配置
     * @details 控制是否对图片和PDF等文件使用OCR进行文本识别
     */
    private boolean useOcr = false;

    /**
     * @brief 自定义选项映射
     * @details 存储特定转换器的自定义配置选项
     */
    private Map<String, Object> customOptions = new HashMap<>();

    /**
     * Creates a new ConversionOptions with default settings.
     */
    public ConversionOptions() {
    }

    /**
     * Creates a new ConversionOptions as a copy of existing options.
     *
     * @param other the options to copy
     */
    public ConversionOptions(ConversionOptions other) {
        this.includeImages = other.includeImages;
        this.includeTables = other.includeTables;
        this.includeMetadata = other.includeMetadata;
        this.tableFormat = other.tableFormat;
        this.imageFormat = other.imageFormat;
        this.language = other.language;
        this.maxFileSize = other.maxFileSize;
        this.tempDirectory = other.tempDirectory;
        this.useOcr = other.useOcr;
        this.customOptions = new HashMap<>(other.customOptions);
    }

    // Getters and setters

    /**
     * Checks if images should be included in the output.
     *
     * @return true if images should be included
     */
    public boolean isIncludeImages() {
        return includeImages;
    }

    /**
     * Sets whether images should be included in the output.
     *
     * @param includeImages true to include images
     * @return this instance for method chaining
     */
    public ConversionOptions setIncludeImages(boolean includeImages) {
        this.includeImages = includeImages;
        return this;
    }

    /**
     * Checks if tables should be included in the output.
     *
     * @return true if tables should be included
     */
    public boolean isIncludeTables() {
        return includeTables;
    }

    /**
     * Sets whether tables should be included in the output.
     *
     * @param includeTables true to include tables
     * @return this instance for method chaining
     */
    public ConversionOptions setIncludeTables(boolean includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    /**
     * Checks if metadata should be included in the output.
     *
     * @return true if metadata should be included
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets whether metadata should be included in the output.
     *
     * @param includeMetadata true to include metadata
     * @return this instance for method chaining
     */
    public ConversionOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * Gets the table format to use.
     *
     * @return the table format
     */
    public String getTableFormat() {
        return tableFormat;
    }

    /**
     * Sets the table format to use.
     *
     * @param tableFormat the table format (github, markdown, pipe)
     * @return this instance for method chaining
     */
    public ConversionOptions setTableFormat(String tableFormat) {
        this.tableFormat = tableFormat;
        return this;
    }

    /**
     * Gets the image format to use.
     *
     * @return the image format
     */
    public String getImageFormat() {
        return imageFormat;
    }

    /**
     * Sets the image format to use.
     *
     * @param imageFormat the image format (markdown, html, base64)
     * @return this instance for method chaining
     */
    public ConversionOptions setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
        return this;
    }

    /**
     * Gets the language for OCR operations.
     *
     * @return the language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language for OCR operations.
     *
     * @param language the language code
     * @return this instance for method chaining
     */
    public ConversionOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets the maximum file size for conversion.
     *
     * @return the maximum file size in bytes
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the maximum file size for conversion.
     *
     * @param maxFileSize the maximum file size in bytes
     * @return this instance for method chaining
     */
    public ConversionOptions setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    /**
     * Gets the temporary directory for file operations.
     *
     * @return the temporary directory, or null if not set
     */
    public Path getTempDirectory() {
        return tempDirectory;
    }

    /**
     * Sets the temporary directory for file operations.
     *
     * @param tempDirectory the temporary directory
     * @return this instance for method chaining
     */
    public ConversionOptions setTempDirectory(Path tempDirectory) {
        this.tempDirectory = tempDirectory;
        return this;
    }

    /**
     * Checks if OCR should be used for text extraction.
     *
     * @return true if OCR should be used
     */
    public boolean isUseOcr() {
        return useOcr;
    }

    /**
     * Sets whether OCR should be used for text extraction.
     *
     * @param useOcr true to use OCR
     * @return this instance for method chaining
     */
    public ConversionOptions setUseOcr(boolean useOcr) {
        this.useOcr = useOcr;
        return this;
    }

    /**
     * Gets custom options specific to converters.
     *
     * @return an immutable map of custom options
     */
    public Map<String, Object> getCustomOptions() {
        return new HashMap<>(customOptions);
    }

    /**
     * Sets a custom option.
     *
     * @param key   the option key
     * @param value the option value
     * @return this instance for method chaining
     */
    public ConversionOptions setCustomOption(String key, Object value) {
        this.customOptions.put(key, value);
        return this;
    }

    /**
     * Gets a specific custom option.
     *
     * @param key the option key
     * @return the option value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomOption(String key) {
        return (T) customOptions.get(key);
    }

    /**
     * Creates a new builder for ConversionOptions.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern for ConversionOptions.
     */
    public static class Builder {
        private final ConversionOptions options = new ConversionOptions();

        private Builder() {}

        public Builder includeImages(boolean includeImages) {
            options.setIncludeImages(includeImages);
            return this;
        }

        public Builder includeTables(boolean includeTables) {
            options.setIncludeTables(includeTables);
            return this;
        }

        public Builder includeMetadata(boolean includeMetadata) {
            options.setIncludeMetadata(includeMetadata);
            return this;
        }

        public Builder tableFormat(String tableFormat) {
            options.setTableFormat(tableFormat);
            return this;
        }

        public Builder imageFormat(String imageFormat) {
            options.setImageFormat(imageFormat);
            return this;
        }

        public Builder language(String language) {
            options.setLanguage(language);
            return this;
        }

        public Builder maxFileSize(long maxFileSize) {
            options.setMaxFileSize(maxFileSize);
            return this;
        }

        public Builder tempDirectory(Path tempDirectory) {
            options.setTempDirectory(tempDirectory);
            return this;
        }

        public Builder useOcr(boolean useOcr) {
            options.setUseOcr(useOcr);
            return this;
        }

        public Builder customOption(String key, Object value) {
            options.setCustomOption(key, value);
            return this;
        }

        public ConversionOptions build() {
            return new ConversionOptions(options);
        }
    }
}