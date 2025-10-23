package com.markdown.engine.context;

import com.markdown.engine.config.MarkdownConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @class RenderContext
 * @brief 渲染上下文对象
 * @details 在Markdown生成过程中提供配置和状态管理，维护渲染选项、元数据和临时状态
 *          支持线程安全操作和链式调用
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class RenderContext {

    private final MarkdownConfig config;
    private final Map<String, Object> metadata;
    private final Map<String, Object> state;
    private final StringBuilder output;

    /**
     * @brief 构造函数 - 创建新的渲染上下文
     * @details 使用指定配置和元数据创建渲染上下文实例，初始化线程安全的容器
     *          过滤掉null键值对，确保数据完整性
     * @param config   渲染配置，不能为null，null时使用默认配置
     * @param metadata 文档元数据，可以为空或null，支持null键值过滤
     */
    public RenderContext(MarkdownConfig config, Map<String, Object> metadata) {
        this.config = config != null ? config : MarkdownConfig.builder().build();
        this.metadata = new ConcurrentHashMap<>();
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.state = new ConcurrentHashMap<>();
        this.output = new StringBuilder();
    }

    /**
     * @brief 简化构造函数 - 创建带空元数据的渲染上下文
     * @details 使用指定配置创建渲染上下文，元数据初始化为空映射
     * @param config 渲染配置，不能为null，null时使用默认配置
     */
    public RenderContext(MarkdownConfig config) {
        this(config, null);
    }

    /**
     * @brief 获取渲染配置
     * @details 返回当前渲染上下文使用的配置对象，包含所有渲染设置
     * @return MarkdownConfig 渲染配置实例
     */
    public MarkdownConfig getConfig() {
        return config;
    }

    /**
     * @brief 获取文档元数据
     * @details 返回当前文档的元数据映射，包含标题、作者、创建时间等信息
     * @return Map<String,Object> 文档元数据映射，线程安全
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * @brief 获取输出缓冲区
     * @details 返回用于构建Markdown内容的字符串构建器，支持直接操作和链式调用
     * @return StringBuilder 输出缓冲区，线程安全
     */
    public StringBuilder getOutput() {
        return output;
    }

    // ==================== 状态管理方法 ====================

    /**
     * @brief 设置状态值
     * @details 在上下文中存储临时状态数据，用于渲染过程中的信息传递
     * @param key   状态键名，不能为null
     * @param value 状态值，可以为null
     */
    public void setState(String key, Object value) {
        state.put(key, value);
    }

    /**
     * @brief 获取状态值
     * @details 根据键名获取上下文中存储的状态数据
     * @param key 状态键名
     * @return Object 状态值，不存在时返回null
     */
    public Object getState(String key) {
        return state.get(key);
    }

    /**
     * @brief 获取类型化状态值
     * @details 获取指定类型的状态值，如果类型不匹配则返回null
     * @param key  状态键名
     * @param type 期望的类型
     * @param <T>  类型参数
     * @return T 类型化的状态值，类型不匹配或不存在时返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getState(String key, Class<T> type) {
        Object value = state.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    // ==================== 配置便捷方法 ====================

    /**
     * @brief 获取元数据包含配置
     * @details 便捷方法，查询是否在Markdown中包含元数据
     * @return boolean true表示包含元数据，false表示跳过
     */
    public boolean shouldIncludeMetadata() {
        return config.isIncludeMetadata();
    }

    /**
     * @brief 获取表格支持配置
     * @details 便捷方法，查询是否支持表格渲染
     * @return boolean true表示支持表格，false表示跳过表格
     */
    public boolean shouldIncludeTables() {
        return config.isIncludeTables();
    }

    /**
     * @brief 获取表格格式配置
     * @details 便捷方法，获取当前表格格式样式
     * @return String 表格格式标识符
     */
    public String getTableFormat() {
        return config.getTableFormat();
    }

    /**
     * @brief 获取列表样式配置
     * @details 便捷方法，获取当前列表标记样式
     * @return String 列表样式标识符
     */
    public String getListStyle() {
        return config.getListStyle();
    }

    /**
     * @brief 获取标题样式配置
     * @details 便捷方法，获取当前标题标记样式
     * @return String 标题样式标识符
     */
    public String getHeadingStyle() {
        return config.getHeadingStyle();
    }

    /**
     * @brief 获取HTML转义配置
     * @details 便捷方法，查询是否转义HTML字符
     * @return boolean true表示转义HTML，false表示保持原样
     */
    public boolean shouldEscapeHtml() {
        return config.isEscapeHtml();
    }

    /**
     * @brief 获取代码块包装配置
     * @details 便捷方法，查询是否使用```包装代码块
     * @return boolean true表示包装代码块，false表示使用缩进
     */
    public boolean shouldWrapCodeBlocks() {
        return config.isWrapCodeBlocks();
    }

    /**
     * @brief 获取最大列表深度配置
     * @details 便捷方法，获取嵌套列表的最大允许深度
     * @return int 最大列表深度
     */
    public int getMaxListDepth() {
        return config.getMaxListDepth();
    }

    /**
     * @brief 获取Map键排序配置
     * @details 便捷方法，查询是否对Map键进行排序
     * @return boolean true表示排序Map键，false表示保持原序
     */
    public boolean shouldSortMapKeys() {
        return config.isSortMapKeys();
    }

    /**
     * @brief 获取日期格式配置
     * @details 便捷方法，获取日期格式化模式
     * @return String 日期格式字符串
     */
    public String getDateFormat() {
        return config.getDateFormat();
    }

    // ==================== 内容操作方法 ====================

    /**
     * @brief 重置上下文以便重用
     * @details 清空输出缓冲区和状态数据，保留元数据和配置
     *          用于批量处理多个文档时的上下文重用
     */
    public void reset() {
        output.setLength(0);
        state.clear();
    }

    /**
     * @brief 获取当前内容
     * @details 将输出缓冲区转换为字符串，返回当前已生成的所有Markdown内容
     * @return String 当前的Markdown内容，包含所有已生成的文本
     */
    public String getContent() {
        return output.toString();
    }

    /**
     * @brief 追加文本到输出缓冲区
     * @details 将指定文本追加到输出缓冲区末尾，支持链式调用
     * @param text 要追加的文本，可以为null（null值会被忽略）
     * @return RenderContext 上下文实例，支持链式调用
     */
    public RenderContext append(String text) {
        output.append(text);
        return this;
    }

    /**
     * @brief 追加换行符到输出缓冲区
     * @details 追加系统特定的换行符，支持跨平台兼容性
     * @return RenderContext 上下文实例，支持链式调用
     */
    public RenderContext newline() {
        output.append(System.lineSeparator());
        return this;
    }

    /**
     * @brief 追加多个换行符到输出缓冲区
     * @details 根据指定数量追加多个系统特定的换行符，用于控制文档间距
     * @param count 要追加的换行符数量，必须大于等于0
     * @return RenderContext 上下文实例，支持链式调用
     */
    public RenderContext newline(int count) {
        for (int i = 0; i < count; i++) {
            output.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * @brief 追加格式化字符串到输出缓冲区
     * @details 使用String.format格式化字符串并追加到输出缓冲区，支持链式调用
     * @param format 格式化字符串，遵循String.format规范
     * @param args   格式化参数，可以为空数组
     * @return RenderContext 上下文实例，支持链式调用
     */
    public RenderContext appendf(String format, Object... args) {
        output.append(String.format(format, args));
        return this;
    }

    // ==================== 列表深度管理方法 ====================

    /**
     * @brief 增加当前列表深度
     * @details 用于嵌套列表渲染，增加列表嵌套层级并更新状态
     * @return int 新的列表深度值
     */
    public int incrementListDepth() {
        Integer currentDepth = getState("listDepth", Integer.class);
        if (currentDepth == null) {
            currentDepth = 0;
        }
        int newDepth = currentDepth + 1;
        setState("listDepth", newDepth);
        return newDepth;
    }

    /**
     * @brief 减少当前列表深度
     * @details 用于嵌套列表渲染，减少列表嵌套层级并更新状态，防止负数
     * @return int 新的列表深度值
     */
    public int decrementListDepth() {
        Integer currentDepth = getState("listDepth", Integer.class);
        if (currentDepth == null || currentDepth <= 0) {
            currentDepth = 1;
        }
        int newDepth = currentDepth - 1;
        setState("listDepth", newDepth);
        return newDepth;
    }

    /**
     * @brief 获取当前列表深度
     * @details 获取当前的嵌套列表深度层级，用于渲染时的缩进计算
     * @return int 当前列表深度，未设置时返回0
     */
    public int getCurrentListDepth() {
        Integer depth = getState("listDepth", Integer.class);
        return depth != null ? depth : 0;
    }

    /**
     * @brief 生成上下文对象的字符串表示
     * @details 以JSON格式展示上下文的关键信息，包括配置、元数据、状态和输出长度
     * @return String 上下文对象的字符串表示
     */
    @Override
    public String toString() {
        return "RenderContext{" +
               "config=" + config +
               ", metadata=" + metadata +
               ", state=" + state +
               ", outputLength=" + output.length() +
               '}';
    }
}