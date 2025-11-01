package com.markitdown;

import com.markitdown.cli.MarkItDownCommand;
import com.markitdown.converter.*;
import com.markitdown.core.ConverterRegistry;
import com.markitdown.core.MarkItDownEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class MarkItDownApplication
 * @brief MarkItDown Java应用程序主入口类
 * @details 作为整个应用程序的启动入口，负责初始化和配置转换引擎
 *          注册所有可用的文档转换器，提供完整的文档转换功能
 *          委托给CLI命令行工具处理用户交互
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class MarkItDownApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownApplication.class);

    /**
     * @brief 应用程序主入口点
     * @details 程序启动的入口方法，直接委托给CLI命令处理器
     * @param args 命令行参数数组
     */
    public static void main(String[] args) {
        // 委托给CLI命令处理器
        MarkItDownCommand.main(args);
    }

    /**
     * @brief 创建并配置带有默认转换器的引擎实例
     * @details 创建转换器注册表并注册所有可用的文档转换器
     *          包括PDF、DOCX、PPTX、XLSX、HTML、图片、音频和文本转换器
     * @return MarkItDownEngine 配置完成的引擎实例
     */
    private static MarkItDownEngine createEngine() {
        ConverterRegistry registry = new ConverterRegistry();

        // 注册所有可用的转换器
        registry.registerConverter(new PdfConverter());
        registry.registerConverter(new DocxConverter());
        registry.registerConverter(new PptxConverter());
        registry.registerConverter(new XlsxConverter());
        registry.registerConverter(new HtmlConverter());
        registry.registerConverter(new ImageConverter());
        registry.registerConverter(new AudioConverter());
        registry.registerConverter(new TextConverter());

        return new MarkItDownEngine(registry);
    }
}