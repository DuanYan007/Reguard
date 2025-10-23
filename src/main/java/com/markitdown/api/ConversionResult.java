package com.markitdown.api;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @class ConversionResult
 * @brief 文档转换结果类，封装转换操作的所有输出信息
 * @details 包含转换后的Markdown内容、元数据、警告信息、文件信息和转换状态
 *          支持成功和失败两种结果状态，提供不可变的结果对象
 *          使用防御性拷贝确保数据的线程安全性
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConversionResult {

    // ==================== 实例变量 ====================

    /**
     * @brief 转换后的Markdown内容
     * @details 存储转换生成的Markdown格式文本内容
     */
    private final String markdownContent;

    /**
     * @brief 转换元数据
     * @details 包含文档属性、转换统计等元信息
     */
    private final Map<String, Object> metadata;

    /**
     * @brief 警告信息列表
     * @details 记录转换过程中产生的警告和错误信息
     */
    private final List<String> warnings;

    /**
     * @brief 转换时间戳
     * @details 记录转换操作执行的时间
     */
    private final LocalDateTime conversionTime;

    /**
     * @brief 原始文件大小
     * @details 原始文件的字节大小
     */
    private final long fileSize;

    /**
     * @brief 原始文件名
     * @details 转换前文件的原始名称
     */
    private final String originalFileName;

    /**
     * @brief 转换成功状态
     * @details 标识转换操作是否成功完成
     */
    private final boolean successful;

    // ==================== 构造函数 ====================

    /**
     * @brief 创建成功的转换结果
     * @details 构造一个表示转换成功的结果对象，包含完整的转换内容和元数据
     * @param markdownContent 转换后的Markdown内容，可以为null
     * @param metadata       转换元数据映射，可以为null
     * @param warnings       转换过程中的警告信息列表，可以为null
     * @param fileSize       原始文件大小（字节）
     * @param originalFileName 原始文件名，可以为null
     */
    public ConversionResult(String markdownContent, Map<String, Object> metadata,
                           List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = markdownContent != null ? markdownContent : "";
        this.metadata = new HashMap<>(metadata != null ? metadata : Collections.emptyMap());
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName != null ? originalFileName : "";
        this.successful = true;
    }

    /**
     * @brief 创建失败的转换结果
     * @details 构造一个表示转换失败的结果对象，仅包含错误信息和基本文件信息
     * @param warnings 转换失败时的错误信息列表，可以为null
     * @param fileSize 原始文件大小（字节）
     * @param originalFileName 原始文件名，可以为null
     */
    public ConversionResult(List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = "";
        this.metadata = Collections.emptyMap();
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName != null ? originalFileName : "";
        this.successful = false;
    }

    // ==================== Getter方法 ====================

    /**
     * @brief 获取转换后的Markdown内容
     * @details 返回转换生成的Markdown格式文本内容
     * @return String Markdown内容，转换失败时返回空字符串
     */
    public String getTextContent() {
        return markdownContent;
    }

    /**
     * @brief 获取转换元数据
     * @details 返回包含文档属性和转换统计信息的不可变映射
     * @return Map<String,Object> 不可变的元数据映射
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Gets the list of warnings.
     *
     * @return an immutable list of warnings
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Gets the time when the conversion was performed.
     *
     * @return the conversion timestamp
     */
    public LocalDateTime getConversionTime() {
        return conversionTime;
    }

    /**
     * Gets the original file size in bytes.
     *
     * @return the file size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the original file name.
     *
     * @return the original file name
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Checks if the conversion was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the Markdown content.
     *
     * @return the Markdown content
     */
    public String getMarkdown() {
        return markdownContent;
    }

    /**
     * Gets a specific metadata value.
     *
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Checks if there are any warnings.
     *
     * @return true if there are warnings, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "successful=" + successful +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileSize=" + fileSize +
                ", markdownContentLength=" + markdownContent.length() +
                ", warningsCount=" + warnings.size() +
                ", conversionTime=" + conversionTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversionResult that = (ConversionResult) o;
        return fileSize == that.fileSize &&
                successful == that.successful &&
                Objects.equals(markdownContent, that.markdownContent) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(warnings, that.warnings) &&
                Objects.equals(conversionTime, that.conversionTime) &&
                Objects.equals(originalFileName, that.originalFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markdownContent, metadata, warnings, conversionTime,
                          fileSize, originalFileName, successful);
    }
}