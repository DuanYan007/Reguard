package com.markitdown.core;

import com.markitdown.api.DocumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * @class ConverterRegistry
 * @brief 文档转换器注册表，用于管理和查找各种文档转换器
 * @details 采用线程安全设计，支持转换器的动态注册、注销和查找
 *          基于MIME类型和转换器名称进行匹配，支持优先级排序
 *          使用缓存机制提高查找效率，可扩展为Spring Bean
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConverterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConverterRegistry.class);

    // ==================== 实例变量 ====================

    /**
     * @brief MIME类型到转换器列表的映射
     * @details 缓存MIME类型与支持该类型的转换器之间的关系，提高查找效率
     */
    private final Map<String, List<DocumentConverter>> mimeTypeToConverters;

    /**
     * @brief 转换器名称到转换器实例的映射
     * @details 存储所有已注册的转换器，支持按名称查找
     */
    private final Map<String, DocumentConverter> nameToConverter;

    /**
     * @brief 构造函数 - 创建新的转换器注册表
     * @details 初始化线程安全的并发映射容器，准备接收转换器注册
     */
    public ConverterRegistry() {
        this.mimeTypeToConverters = new ConcurrentHashMap<>();
        this.nameToConverter = new ConcurrentHashMap<>();
    }

      // ==================== 转换器管理方法 ====================

    /**
     * @brief 注册文档转换器
     * @details 将转换器添加到注册表中，支持按名称查找和MIME类型匹配
     *          使用同步方法确保线程安全，防止重复注册
     * @param converter 要注册的转换器实例，不能为null
     * @throws IllegalArgumentException 当转换器为null或已注册时抛出异常
     */
    public synchronized void registerConverter(DocumentConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");

        String name = converter.getName();
        if (nameToConverter.containsKey(name)) {
            throw new IllegalArgumentException("Converter with name '" + name + "' is already registered");
        }

        nameToConverter.put(name, converter);
        logger.info("Registered converter: {}", name);

        // MIME类型缓存将在getConverter方法中处理
        // 转换器不直接暴露支持的MIME类型，需要动态检查
    }

    /**
     * @brief 注销文档转换器
     * @details 从注册表中移除指定名称的转换器，并清理相关的MIME类型缓存
     *          使用同步方法确保线程安全，清理缓存以保持数据一致性
     * @param converterName 要注销的转换器名称，不能为null
     * @return boolean true表示成功找到并移除转换器，false表示未找到
     */
    public synchronized boolean unregisterConverter(String converterName) {
        DocumentConverter removed = nameToConverter.remove(converterName);
        if (removed != null) {
            logger.info("Unregistered converter: {}", converterName);
            // 清理MIME类型缓存以强制重新评估
            mimeTypeToConverters.clear();
            return true;
        }
        return false;
    }

    // ==================== 转换器查找方法 ====================

    /**
     * @brief 获取支持指定MIME类型的转换器
     * @details 根据MIME类型查找最适合的转换器，支持缓存和优先级排序
     *          首先检查缓存，若未命中则遍历所有注册的转换器进行匹配
     * @param MIME类型 要查找转换器的MIME类型，不能为null
     * @return Optional<DocumentConverter> 包含最高优先级转换器的Optional，未找到时返回空Optional
     */
    public Optional<DocumentConverter> getConverter(String mimeType) {
        Objects.requireNonNull(mimeType, "MIME type cannot be null");

        // 首先检查缓存
        List<DocumentConverter> converters = mimeTypeToConverters.get(mimeType);
        if (converters != null && !converters.isEmpty()) {
            return Optional.of(converters.get(0)); // 返回最高优先级的转换器
        }

        // 查找支持此MIME类型的转换器
        List<DocumentConverter> matchingConverters = nameToConverter.values().stream()
                .filter(converter -> converter.supports(mimeType))
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())) // 优先级高的在前
                .collect(Collectors.toList());

        if (matchingConverters.isEmpty()) {
            logger.debug("No converter found for MIME type: {}", mimeType);
            return Optional.empty();
        }

        // 缓存结果
        mimeTypeToConverters.put(mimeType, matchingConverters);
        logger.debug("Found {} converter(s) for MIME type: {}", matchingConverters.size(), mimeType);

        return Optional.of(matchingConverters.get(0));
    }

    /**
     * @brief 获取所有支持指定MIME类型的转换器
     * @details 返回支持指定MIME类型的所有转换器列表，按优先级降序排列
     *          不使用缓存，每次都重新查找以确保结果的准确性
     * @param mimeType 要查找的MIME类型，不能为null
     * @return List<DocumentConverter> 支持该MIME类型的转换器列表，按优先级排序
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
     * @brief 根据名称获取转换器
     * @details 通过转换器的唯一名称查找对应的转换器实例
     * @param name 转换器名称，不能为null
     * @return Optional<DocumentConverter> 包含转换器的Optional，未找到时返回空Optional
     */
    public Optional<DocumentConverter> getConverterByName(String name) {
        Objects.requireNonNull(name, "Converter name cannot be null");
        return Optional.ofNullable(nameToConverter.get(name));
    }

    /**
     * @brief 获取所有已注册的转换器
     * @details 返回注册表中所有转换器的不可修改集合
     * @return Collection<DocumentConverter> 所有已注册转换器的集合
     */
    public Collection<DocumentConverter> getAllConverters() {
        return Collections.unmodifiableCollection(nameToConverter.values());
    }

    // ==================== MIME类型支持方法 ====================

    /**
     * @brief 获取所有支持的MIME类型
     * @details 遍历所有注册的转换器，推断并汇总所有支持的MIME类型
     *          由于转换器不直接暴露支持的MIME类型，需要基于类名进行推断
     * @return Set<String> 所有支持的MIME类型集合
     */
    public Set<String> getSupportedMimeTypes() {
        Set<String> mimeTypes = new HashSet<>();

        for (DocumentConverter converter : nameToConverter.values()) {
            // 由于转换器不直接暴露支持的MIME类型，需要检查常见MIME类型
            // 这是当前设计的一个限制
            mimeTypes.addAll(getCommonMimeTypesForConverter(converter));
        }

        return Collections.unmodifiableSet(mimeTypes);
    }

    /**
     * @brief 检查MIME类型是否被支持
     * @details 快速检查指定MIME类型是否有任何注册的转换器支持
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示有转换器支持，false表示不支持
     */
    public boolean isSupported(String mimeType) {
        return getConverter(mimeType).isPresent();
    }

    // ==================== 管理操作方法 ====================

    /**
     * @brief 清空所有已注册的转换器
     * @details 移除注册表中的所有转换器并清理缓存，使用同步方法确保线程安全
     */
    public synchronized void clear() {
        logger.info("Clearing all converters from registry");
        nameToConverter.clear();
        mimeTypeToConverters.clear();
    }

    /**
     * @brief 获取已注册转换器的数量
     * @details 返回当前注册表中转换器的总数
     * @return int 转换器数量
     */
    public int getConverterCount() {
        return nameToConverter.size();
    }

    /**
     * @brief 获取所有已注册转换器的信息
     * @details 生成包含转换器名称、优先级和类名的信息映射
     * @return Map<String, String> 转换器名称到信息的映射，键为名称，值为格式化的信息字符串
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

    // ==================== 私有辅助方法 ====================

    /**
     * @brief 推断转换器可能支持的常见MIME类型
     * @details 基于转换器类名推断其可能支持的MIME类型
     *          这是一个临时解决方案，直到转换器能够直接暴露支持的MIME类型
     * @param converter 要检查的转换器，不能为null
     * @return Set<String> 推断出的MIME类型集合
     */
    private Set<String> getCommonMimeTypesForConverter(DocumentConverter converter) {
        Set<String> mimeTypes = new HashSet<>();
        String className = converter.getClass().getSimpleName().toLowerCase();

        // 基于类名进行推断
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

    /**
     * @brief 生成注册表的字符串表示
     * @details 以JSON格式展示注册表的关键信息，包括转换器数量和支持的MIME类型数量
     * @return String 注册表的字符串表示
     */
    @Override
    public String toString() {
        return "ConverterRegistry{" +
                "converterCount=" + nameToConverter.size() +
                ", supportedMimeTypes=" + getSupportedMimeTypes().size() +
                '}';
    }
}