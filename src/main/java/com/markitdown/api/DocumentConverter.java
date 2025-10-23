package com.markitdown.api;

import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;

import java.nio.file.Path;

/**
 * @interface DocumentConverter
 * @brief 文档转换器接口，定义了将各种文件格式转换为Markdown的规范
 * @details 所有具体的文档转换器实现都必须实现此接口，支持不同文件格式的转换
 *          提供了转换支持检查、优先级管理和转换器标识等核心功能
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public interface DocumentConverter {

    /**
     * @brief 将文档文件转换为Markdown格式
     * @details 核心转换方法，将指定路径的文档文件转换为Markdown内容
     *          支持通过转换选项控制转换行为和输出格式
     * @param filePath 要转换的文档文件路径，不能为null
     * @param options  转换选项配置，可以为null（使用默认配置）
     * @return ConversionResult 包含Markdown内容和元数据的转换结果
     * @throws ConversionException 当转换失败时抛出异常
     */
    ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException;

    /**
     * @brief 检查是否支持指定的MIME类型
     * @details 用于判断转换器是否能够处理特定类型的文件
     *          支持根据MIME类型自动选择合适的转换器
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示支持该MIME类型，false表示不支持
     */
    boolean supports(String mimeType);

    /**
     * @brief 获取转换器优先级
     * @details 当多个转换器支持同一MIME类型时，优先级高的转换器会被优先选择
     *          优先级值越大表示优先级越高，默认优先级为0
     * @return int 转换器优先级值，默认为0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * @brief 获取转换器名称
     * @details 返回转换器的唯一标识名称，用于日志记录和调试
     * @return String 转换器名称，不能为null或空字符串
     */
    String getName();
}