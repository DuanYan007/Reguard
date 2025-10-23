package com.markdown.engine.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @class MarkdownConfig
 * @brief Markdown生成操作配置类
 * @details 管理Markdown生成的各种配置选项，包括表格格式、列表样式、标题样式等
 *          支持链式调用和Builder模式创建配置实例
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
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
     * @brief 默认构造函数 - 创建默认配置实例
     * @details 使用默认配置值创建MarkdownConfig实例，包括表格支持、HTML转义等标准设置
     */
    public MarkdownConfig() {
    }

    /**
     * @brief 拷贝构造函数 - 创建现有配置的副本
     * @details 深拷贝所有配置选项，包括表格格式、列表样式、标题样式等
     *          创建新的customOptions映射以避免共享状态
     * @param other 要拷贝的配置对象，不能为null
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

    // ==================== 表格相关配置 ====================

    /**
     * @brief 获取表格支持配置
     * @details 查询是否在Markdown生成过程中包含表格内容
     * @return boolean true表示支持表格渲染，false表示跳过表格
     */
    public boolean isIncludeTables() {
        return includeTables;
    }

    /**
     * @brief 设置表格支持配置
     * @details 控制Markdown生成过程中是否包含表格内容，支持链式调用
     * @param includeTables 是否支持表格渲染
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setIncludeTables(boolean includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    /**
     * @brief 获取表格格式配置
     * @details 查询当前使用的表格格式样式，影响表格边框和分隔符的生成方式
     * @return String 表格格式标识符，支持github/markdown/pipe三种格式
     */
    public String getTableFormat() {
        return tableFormat;
    }

    /**
     * @brief 设置表格格式配置
     * @details 设置表格渲染格式，会验证输入格式是否有效，无效格式将使用默认值
     * @param tableFormat 表格格式标识符，支持github/markdown/pipe
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setTableFormat(String tableFormat) {
        this.tableFormat = validateTableFormat(tableFormat);
        return this;
    }

      // ==================== 元数据相关配置 ====================

    /**
     * @brief 获取元数据支持配置
     * @details 查询是否在Markdown文档开头包含元数据信息
     * @return boolean true表示包含元数据，false表示跳过元数据
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * @brief 设置元数据支持配置
     * @details 控制是否在生成的Markdown文档中包含标题、作者等元数据信息
     * @param includeMetadata 是否包含元数据
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    // ==================== 列表样式配置 ====================

    /**
     * @brief 获取列表样式配置
     * @details 查询当前使用的列表项标记符号样式
     * @return String 列表样式标识符，支持dash/asterisk/plus三种样式
     */
    public String getListStyle() {
        return listStyle;
    }

    /**
     * @brief 设置列表样式配置
     * @details 设置无序列表项的标记符号，会验证输入样式是否有效
     * @param listStyle 列表样式标识符，支持dash/asterisk/plus
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setListStyle(String listStyle) {
        this.listStyle = validateListStyle(listStyle);
        return this;
    }

    // ==================== 标题样式配置 ====================

    /**
     * @brief 获取标题样式配置
     * @details 查询当前使用的标题标记样式
     * @return String 标题样式标识符，支持atx/setext两种样式
     */
    public String getHeadingStyle() {
        return headingStyle;
    }

    /**
     * @brief 设置标题样式配置
     * @details 设置标题的标记方式，会验证输入样式是否有效
     * @param headingStyle 标题样式标识符，支持atx/setext
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setHeadingStyle(String headingStyle) {
        this.headingStyle = validateHeadingStyle(headingStyle);
        return this;
    }

    // ==================== HTML转义配置 ====================

    /**
     * @brief 获取HTML转义配置
     * @details 查询是否对HTML特殊字符进行转义处理
     * @return boolean true表示转义HTML字符，false表示保持原样
     */
    public boolean isEscapeHtml() {
        return escapeHtml;
    }

    /**
     * @brief 设置HTML转义配置
     * @details 控制是否将HTML特殊字符(如<、>、&)转换为对应的HTML实体
     * @param escapeHtml 是否转义HTML字符
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setEscapeHtml(boolean escapeHtml) {
        this.escapeHtml = escapeHtml;
        return this;
    }

    // ==================== 代码块配置 ====================

    /**
     * @brief 获取代码块包装配置
     * @details 查询是否使用```包装代码块内容
     * @return boolean true表示包装代码块，false表示使用缩进方式
     */
    public boolean isWrapCodeBlocks() {
        return wrapCodeBlocks;
    }

    /**
     * @brief 设置代码块包装配置
     * @details 控制代码块的生成方式，使用```包装或4空格缩进
     * @param wrapCodeBlocks 是否使用```包装代码块
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setWrapCodeBlocks(boolean wrapCodeBlocks) {
        this.wrapCodeBlocks = wrapCodeBlocks;
        return this;
    }

    // ==================== 列表深度配置 ====================

    /**
     * @brief 获取最大列表深度配置
     * @details 查询嵌套列表的最大允许深度层级
     * @return int 最大列表深度，默认值为10
     */
    public int getMaxListDepth() {
        return maxListDepth;
    }

    /**
     * @brief 设置最大列表深度配置
     * @details 设置嵌套列表的最大允许深度，防止无限嵌套，最小值为1
     * @param maxListDepth 最大列表深度
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setMaxListDepth(int maxListDepth) {
        this.maxListDepth = Math.max(1, maxListDepth);
        return this;
    }

    // ==================== Map键排序配置 ====================

    /**
     * @brief 获取Map键排序配置
     * @details 查询是否对Map对象的键进行字母排序
     * @return boolean true表示排序键，false表示保持原始顺序
     */
    public boolean isSortMapKeys() {
        return sortMapKeys;
    }

    /**
     * @brief 设置Map键排序配置
     * @details 控制在生成表格或列表时是否对Map对象的键进行字母排序
     * @param sortMapKeys 是否排序Map键
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setSortMapKeys(boolean sortMapKeys) {
        this.sortMapKeys = sortMapKeys;
        return this;
    }

    // ==================== 日期格式配置 ====================

    /**
     * @brief 获取日期格式配置
     * @details 查询日期对象的格式化模式字符串
     * @return String 日期格式模式，遵循SimpleDateFormat规范
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @brief 设置日期格式配置
     * @details 设置日期对象的格式化模式，null值将使用默认格式
     * @param dateFormat 日期格式模式，遵循SimpleDateFormat规范
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat != null ? dateFormat : "yyyy-MM-dd HH:mm:ss";
        return this;
    }

    // ==================== 自定义选项配置 ====================

    /**
     * @brief 获取所有自定义选项
     * @details 返回所有自定义配置选项的副本，避免直接修改内部状态
     * @return Map<String,Object> 自定义选项映射的副本
     */
    public Map<String, Object> getCustomOptions() {
        return new HashMap<>(customOptions);
    }

    /**
     * @brief 设置自定义选项
     * @details 添加或更新自定义配置选项，支持扩展配置功能
     * @param key   选项键名，不能为null
     * @param value 选项值，可以为null
     * @return MarkdownConfig 配置实例，支持链式调用
     */
    public MarkdownConfig setCustomOption(String key, Object value) {
        this.customOptions.put(key, value);
        return this;
    }

    /**
     * @brief 获取指定自定义选项
     * @details 根据键名获取自定义配置选项的值，支持类型转换
     * @param key 选项键名
     * @param <T> 期望的返回类型
     * @return T 选项值，类型转换失败时返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomOption(String key) {
        return (T) customOptions.get(key);
    }

    // ==================== 验证方法 ====================

    /**
     * @brief 验证表格格式
     * @details 检查输入的表格格式是否在支持的格式列表中，不支持则使用默认值
     * @param format 待验证的表格格式字符串
     * @return String 验证后的表格格式，无效时返回"github"
     */
    private String validateTableFormat(String format) {
        Set<String> validFormats = Set.of("github", "markdown", "pipe");
        return validFormats.contains(format) ? format : "github";
    }

    /**
     * @brief 验证列表样式
     * @details 检查输入的列表样式是否在支持的样式列表中，不支持则使用默认值
     * @param style 待验证的列表样式字符串
     * @return String 验证后的列表样式，无效时返回"dash"
     */
    private String validateListStyle(String style) {
        Set<String> validStyles = Set.of("dash", "asterisk", "plus");
        return validStyles.contains(style) ? style : "dash";
    }

    /**
     * @brief 验证标题样式
     * @details 检查输入的标题样式是否在支持的样式列表中，不支持则使用默认值
     * @param style 待验证的标题样式字符串
     * @return String 验证后的标题样式，无效时返回"atx"
     */
    private String validateHeadingStyle(String style) {
        Set<String> validStyles = Set.of("atx", "setext");
        return validStyles.contains(style) ? style : "atx";
    }

    // ==================== Builder模式 ====================

    /**
     * @brief 创建Builder实例
     * @details 创建新的配置构建器，用于流式构建配置对象
     * @return Builder 配置构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @class Builder
     * @brief MarkdownConfig构建器类
     * @details 实现Builder模式，提供流式API创建配置实例，支持链式调用
     *          所有方法都会返回Builder实例以便连续调用
     */
    public static class Builder {
        private final MarkdownConfig config = new MarkdownConfig();

        /**
         * @brief 私有构造函数
         * @details 防止外部直接实例化Builder，只能通过builder()方法创建
         */
        private Builder() {}

        /**
         * @brief 设置表格支持配置
         * @details 流式设置表格支持选项，支持链式调用
         * @param includeTables 是否支持表格渲染
         * @return Builder 构建器实例
         */
        public Builder includeTables(boolean includeTables) {
            config.setIncludeTables(includeTables);
            return this;
        }

        /**
         * @brief 设置元数据支持配置
         * @details 流式设置元数据包含选项，支持链式调用
         * @param includeMetadata 是否包含元数据
         * @return Builder 构建器实例
         */
        public Builder includeMetadata(boolean includeMetadata) {
            config.setIncludeMetadata(includeMetadata);
            return this;
        }

        /**
         * @brief 设置表格格式配置
         * @details 流式设置表格格式样式，支持链式调用
         * @param tableFormat 表格格式标识符
         * @return Builder 构建器实例
         */
        public Builder tableFormat(String tableFormat) {
            config.setTableFormat(tableFormat);
            return this;
        }

        /**
         * @brief 设置列表样式配置
         * @details 流式设置列表标记样式，支持链式调用
         * @param listStyle 列表样式标识符
         * @return Builder 构建器实例
         */
        public Builder listStyle(String listStyle) {
            config.setListStyle(listStyle);
            return this;
        }

        /**
         * @brief 设置标题样式配置
         * @details 流式设置标题标记样式，支持链式调用
         * @param headingStyle 标题样式标识符
         * @return Builder 构建器实例
         */
        public Builder headingStyle(String headingStyle) {
            config.setHeadingStyle(headingStyle);
            return this;
        }

        /**
         * @brief 设置HTML转义配置
         * @details 流式设置HTML字符转义选项，支持链式调用
         * @param escapeHtml 是否转义HTML字符
         * @return Builder 构建器实例
         */
        public Builder escapeHtml(boolean escapeHtml) {
            config.setEscapeHtml(escapeHtml);
            return this;
        }

        /**
         * @brief 设置代码块包装配置
         * @details 流式设置代码块包装方式，支持链式调用
         * @param wrapCodeBlocks 是否使用```包装代码块
         * @return Builder 构建器实例
         */
        public Builder wrapCodeBlocks(boolean wrapCodeBlocks) {
            config.setWrapCodeBlocks(wrapCodeBlocks);
            return this;
        }

        /**
         * @brief 设置最大列表深度配置
         * @details 流式设置嵌套列表最大深度，支持链式调用
         * @param maxListDepth 最大列表深度
         * @return Builder 构建器实例
         */
        public Builder maxListDepth(int maxListDepth) {
            config.setMaxListDepth(maxListDepth);
            return this;
        }

        /**
         * @brief 设置Map键排序配置
         * @details 流式设置Map键排序选项，支持链式调用
         * @param sortMapKeys 是否排序Map键
         * @return Builder 构建器实例
         */
        public Builder sortMapKeys(boolean sortMapKeys) {
            config.setSortMapKeys(sortMapKeys);
            return this;
        }

        /**
         * @brief 设置日期格式配置
         * @details 流式设置日期格式模式，支持链式调用
         * @param dateFormat 日期格式字符串
         * @return Builder 构建器实例
         */
        public Builder dateFormat(String dateFormat) {
            config.setDateFormat(dateFormat);
            return this;
        }

        /**
         * @brief 设置自定义选项配置
         * @details 流式设置自定义配置选项，支持链式调用
         * @param key   选项键名
         * @param value 选项值
         * @return Builder 构建器实例
         */
        public Builder customOption(String key, Object value) {
            config.setCustomOption(key, value);
            return this;
        }

        /**
         * @brief 构建配置实例
         * @details 创建新的MarkdownConfig实例，包含所有已设置的配置
         * @return MarkdownConfig 构建完成的配置实例
         */
        public MarkdownConfig build() {
            return new MarkdownConfig(config);
        }
    }

      /**
     * @brief 判断配置对象是否相等
     * @details 比较所有配置选项和自定义选项是否完全一致
     * @param o 待比较的对象
     * @return boolean true表示配置完全相等，false表示不相等
     */
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

    /**
     * @brief 计算配置对象的哈希码
     * @details 基于所有配置选项计算哈希码，用于HashMap等数据结构
     * @return int 配置对象的哈希码值
     */
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

    /**
     * @brief 生成配置对象的字符串表示
     * @details 以JSON格式展示所有配置选项的名称和值，便于调试和日志记录
     * @return String 配置对象的字符串表示
     */
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