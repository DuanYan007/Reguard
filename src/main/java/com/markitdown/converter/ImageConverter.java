package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class ImageConverter
 * @brief 图片转换器，用于从图片文件中提取文本信息
 * @details 使用Tesseract OCR引擎进行光学字符识别，支持多种图片格式
 *          提取图片元数据、尺寸信息和OCR识别文本
 *          支持多语言识别和文本清理优化
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ImageConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);

    /**
     * @brief 支持的图片格式集合
     * @details 包含所有此转换器支持的图片文件扩展名
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif");

    /**
     * @brief Tesseract OCR引擎实例
     * @details 用于执行光学字符识别的核心引擎
     */
    private final ITesseract tesseract;

    /**
     * @brief 构造函数 - 创建使用默认Tesseract实例的转换器
     * @details 使用默认配置的Tesseract OCR引擎
     */
    public ImageConverter() {
        this.tesseract = new Tesseract();
    }

    /**
     * @brief 构造函数 - 创建使用自定义Tesseract实例的转换器
     * @details 允许注入自定义配置的Tesseract OCR引擎实例
     * @param tesseract Tesseract OCR引擎实例，不能为null
     */
    public ImageConverter(ITesseract tesseract) {
        this.tesseract = requireNonNull(tesseract, "Tesseract instance cannot be null");
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting image file: {}", filePath);

        try {
            // 加载图片
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image == null) {
                throw new ConversionException("Cannot load image file: " + filePath,
                        filePath.getFileName().toString(), getName());
            }

            // 提取元数据
            Map<String, Object> metadata = extractMetadata(filePath, image, options);

            // 如果启用则执行OCR识别
            String extractedText = "";
            if (options.isUseOcr()) {
                extractedText = performOcr(image, options);
            } else {
                extractedText = "*OCR is disabled in conversion options*";
            }

            // 转换为Markdown格式
            String markdownContent = convertToMarkdown(extractedText, metadata, options, filePath);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to read image file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType.startsWith("image/");
    }

    @Override
    public int getPriority() {
        return 80; // Lower priority as OCR is resource-intensive
    }

    @Override
    public String getName() {
        return "ImageConverter";
    }

    /**
     * @brief 从图片文件中提取元数据
     * @details 提取图片的尺寸信息、文件信息和颜色类型等元数据
     *          根据转换选项控制是否包含元数据信息
     * @param filePath 图片文件路径，不能为null
     * @param image    已加载的图片对象，不能为null
     * @param options  转换选项配置，用于控制是否包含元数据
     * @return Map<String, Object> 包含图片元数据的映射
     */
    private Map<String, Object> extractMetadata(Path filePath, BufferedImage image, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 图片尺寸信息
            metadata.put("宽度", image.getWidth());
            metadata.put("高度", image.getHeight());

            // 文件信息
            metadata.put("文件名", filePath.getFileName().toString());
            metadata.put("文件尺寸", filePath.toFile().length());

            // 图片格式
            String fileName = filePath.getFileName().toString();
            String format = getFileExtension(fileName).toLowerCase();
            metadata.put("格式", format);

            // 颜色信息
            metadata.put("颜色类型", getColorType(image));
            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * @brief 使用Tesseract对图片进行OCR识别
     * @details 使用Tesseract OCR引擎从图片中提取文本内容
     *          支持设置识别语言，并对识别结果进行清理优化
     * @param image   要进行OCR处理的图片对象，不能为null
     * @param options 转换选项配置，包含语言设置等参数
     * @return String OCR识别提取的文本内容
     * @throws ConversionException 当OCR处理失败时抛出
     */
    private String performOcr(BufferedImage image, ConversionOptions options) throws ConversionException {
        try {
            // 设置OCR识别语言
            String language = options.getLanguage();
            if (!"auto".equals(language) && !language.isEmpty()) {
                tesseract.setLanguage(language);
            }

            // 执行OCR识别
            String result = tesseract.doOCR(image);
            System.out.println(result);

            // 清理识别结果
            return cleanupOcrResult(result);

        } catch (TesseractException e) {
            String errorMessage = "OCR processing failed: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, "image", getName());
        }
    }

    /**
     * @brief 清理OCR识别结果中的常见问题
     * @details 修复OCR识别过程中常见的字符混淆和格式问题
     *          处理多余的空白字符和常见的识别错误
     * @param ocrText 原始OCR识别的文本内容
     * @return String 清理后的文本内容
     */
    private String cleanupOcrResult(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }

        // 移除多余的空白字符
        String cleaned = ocrText.replaceAll("\\s+", " ");

        // 修复常见的OCR识别错误
        cleaned = cleaned.replaceAll("\\|", "I"); // 常见的竖线和字母I混淆
        cleaned = cleaned.replaceAll("0", "O"); // 在某些情况下常见的数字0和字母O混淆

        // 移除首尾空白字符
        return cleaned.trim();
    }

    /**
     * @brief 将提取的文本转换为Markdown格式
     * @details 生成包含图片引用、元数据信息和提取文本的完整Markdown文档
     *          根据转换选项控制各个部分的显示内容
     * @param extractedText OCR提取的文本内容
     * @param metadata      图片元数据映射
     * @param options       转换选项配置，控制输出内容
     * @param filePath      原始图片文件路径
     * @return String 格式化的Markdown内容
     */
    private String convertToMarkdown(String extractedText, Map<String, Object> metadata,
                                   ConversionOptions options, Path filePath) {
        StringBuilder markdown = new StringBuilder();

        // 如果启用则添加图片引用
        if (options.isIncludeImages()) {
            String fileName = filePath.getFileName().toString();
            markdown.append("![Image](").append(fileName).append(")\n\n");
        }

        // 如果启用则添加元数据部分
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Image Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // 添加提取的文本内容
        markdown.append("## Extracted Text\n\n");

        if (extractedText.isEmpty()) {
            markdown.append("*No text could be extracted from this image*\n\n");
        } else {
            // 格式化提取的文本以提高可读性
            String formattedText = formatExtractedText(extractedText);
            markdown.append(formattedText).append("\n\n");
        }

        return markdown.toString();
    }

    /**
     * @brief 格式化提取的文本以提高可读性
     * @details 对OCR提取的文本进行段落分割和格式化处理
     *          识别可能的标题文本并应用相应的Markdown格式
     * @param text OCR提取的原始文本内容
     * @return String 格式化后的文本内容
     */
    private String formatExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 分割为段落并进行格式化
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder formatted = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                // 检测可能的标题（短行后跟较长文本的情况）
                if (trimmed.length() < 100 && trimmed.length() > 0 &&
                    Character.isUpperCase(trimmed.charAt(0))) {
                    formatted.append("### ").append(trimmed).append("\n\n");
                } else {
                    formatted.append(trimmed).append("\n\n");
                }
            }
        }

        return formatted.toString();
    }

    /**
     * @brief 获取图片的颜色类型
     * @details 根据BufferedImage的类型常量返回对应的颜色类型描述
     *          支持常见的RGB、ARGB、灰度图等颜色模式
     * @param image 要分析的图片对象，不能为null
     * @return String 图片颜色类型的描述字符串
     */
    private String getColorType(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB";
            case BufferedImage.TYPE_INT_BGR:
                return "BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "4BYTE_ABGR";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "Grayscale";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "Binary";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "USHORT_555_RGB";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "USHORT_565_RGB";
            default:
                return "Unknown (" + image.getType() + ")";
        }
    }

    /**
     * @brief 从文件名中获取文件扩展名
     * @details 提取文件名中最后一个点号后的部分作为扩展名
     *          忽略点号在文件名开头或结尾的情况
     * @param fileName 要提取扩展名的文件名，不能为null
     * @return String 文件扩展名（不包含点号），如果没有扩展名则返回空字符串
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * @brief 格式化元数据键名用于显示
     * @details 将驼峰命名的键名转换为更适合显示的格式
     *          在单词间添加空格并转换为小写形式
     * @param key 要格式化的元数据键名，不能为null
     * @return String 格式化后的键名
     */
    private String formatMetadataKey(String key) {
        // 将驼峰命名转换为标题格式
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

    /**
     * @brief 检查文件格式是否被此转换器支持
     * @details 通过比较文件扩展名与预定义的支持格式列表进行判断
     *          不区分大小写地进行匹配
     * @param fileExtension 要检查的文件扩展名
     * @return boolean 如果支持该格式返回true，否则返回false
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * @brief 获取所有支持的图片格式
     * @details 返回一个包含此转换器支持的所有图片文件扩展名的集合
     *          返回的是集合的副本以避免外部修改
     * @return Set<String> 支持的文件扩展名集合
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}