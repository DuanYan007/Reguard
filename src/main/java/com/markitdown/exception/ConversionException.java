package com.markitdown.exception;

/**
 * @class ConversionException
 * @brief 文档转换异常类，用于表示转换操作失败的情况
 * @details 继承自Exception，提供详细的转换失败信息
 *          包含失败文件名、转换器名称等上下文信息
 *          支持多种构造方式以适应不同的错误处理场景
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConversionException extends Exception {

    // ==================== 实例变量 ====================

    /**
     * @brief 转换失败的文件名
     * @details 记录转换操作失败的源文件名称
     */
    private final String fileName;

    /**
     * @brief 转换失败的转换器名称
     * @details 记录执行转换操作时失败的转换器名称
     */
    private final String converterName;

    /**
     * Creates a new ConversionException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConversionException(String message) {
        super(message);
        this.fileName = null;
        this.converterName = null;
    }

    /**
     * Creates a new ConversionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
        this.fileName = null;
        this.converterName = null;
    }

    /**
     * Creates a new ConversionException with the specified detail message, file name, and converter name.
     *
     * @param message       the detail message
     * @param fileName      the name of the file being converted
     * @param converterName the name of the converter that failed
     */
    public ConversionException(String message, String fileName, String converterName) {
        super(message);
        this.fileName = fileName;
        this.converterName = converterName;
    }

    /**
     * Creates a new ConversionException with the specified detail message, cause, file name, and converter name.
     *
     * @param message       the detail message
     * @param cause         the cause of this exception
     * @param fileName      the name of the file being converted
     * @param converterName the name of the converter that failed
     */
    public ConversionException(String message, Throwable cause, String fileName, String converterName) {
        super(message, cause);
        this.fileName = fileName;
        this.converterName = converterName;
    }

    /**
     * Gets the name of the file that failed to convert.
     *
     * @return the file name, or null if not available
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the name of the converter that failed.
     *
     * @return the converter name, or null if not available
     */
    public String getConverterName() {
        return converterName;
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        String message = getLocalizedMessage();
        StringBuilder sb = new StringBuilder(className);

        if (message != null) {
            sb.append(": ").append(message);
        }

        if (fileName != null || converterName != null) {
            sb.append(" [");
            if (fileName != null) {
                sb.append("file=").append(fileName);
            }
            if (fileName != null && converterName != null) {
                sb.append(", ");
            }
            if (converterName != null) {
                sb.append("converter=").append(converterName);
            }
            sb.append("]");
        }

        return sb.toString();
    }
}