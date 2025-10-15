package com.markitdown.exception;

/**
 * Exception thrown when a document conversion operation fails.
 *
 * @author MarkItDown Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConversionException extends Exception {

    private final String fileName;
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