package com.markitdown.cli;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.converter.*;
import com.markitdown.core.ConverterRegistry;
import com.markitdown.core.MarkItDownEngine;
import com.markitdown.exception.ConversionException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * @class MarkItDownCommand
 * @brief MarkItDown Java命令行接口类
 * @details 基于Picocli框架实现的命令行工具，提供文档转换功能
 *          支持多种输入格式、丰富的配置选项和批量处理
 *          提供详细的帮助信息和错误处理机制
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
@Command(
        name = "markitdown",
        mixinStandardHelpOptions = true,
        version = "MarkItDown-java 1.0.0",
        description = "将各种文档格式转换为Markdown格式",
        footerHeading = "示例:%n",
        footer = {
                "  markitdown document.pdf                    # 将PDF转换为Markdown",
                "  markitdown document.docx -o output.md      # 将Word文档转换为output.md",
                "  markitdown presentation.pptx --no-tables   # 转换PowerPoint不包含表格",
                "  markitdown spreadsheet.xlsx --ocr          # 转换Excel并对图片使用OCR",
                "  markitdown *.pdf                          # 转换目录下所有PDF文件"
        }
)
public class MarkItDownCommand implements Callable<Integer> {

    @Option(
            names = {"-o", "--output"},
            description = "Output file or directory (default: same as input with .md extension)"
    )
    private String output;

    @Option(
            names = {"--include-images"},
            description = "Include images in the output (default: true)"
    )
    private Boolean includeImages = true;

    @Option(
            names = {"--no-images"},
            description = "Exclude images from the output"
    )
    private boolean noImages;

    @Option(
            names = {"--include-tables"},
            description = "Include tables in the output (default: true)"
    )
    private Boolean includeTables = true;

    @Option(
            names = {"--no-tables"},
            description = "Exclude tables from the output"
    )
    private boolean noTables;

    @Option(
            names = {"--include-metadata"},
            description = "Include metadata in the output (default: true)"
    )
    private Boolean includeMetadata = true;

    @Option(
            names = {"--no-metadata"},
            description = "Exclude metadata from the output"
    )
    private boolean noMetadata;

    @Option(
            names = {"--ocr"},
            description = "Use OCR for text extraction from images"
    )
    private boolean useOcr;

    @Option(
            names = {"--language"},
            description = "Language for OCR (default: auto)",
            defaultValue = "auto"
    )
    private String language;

    @Option(
            names = {"--table-format"},
            description = "Table format: github, markdown, pipe (default: github)",
            defaultValue = "github"
    )
    private String tableFormat;

    @Option(
            names = {"--image-format"},
            description = "Image format: markdown, html, base64 (default: markdown)",
            defaultValue = "markdown"
    )
    private String imageFormat;

    @Option(
            names = {"--max-file-size"},
            description = "Maximum file size in bytes (default: 50MB)",
            defaultValue = "52428800"
    )
    private long maxFileSize;

    @Option(
            names = {"--temp-dir"},
            description = "Temporary directory for file operations"
    )
    private String tempDir;

    @Option(
            names = {"--verbose", "-v"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    @Option(
            names = {"--quiet", "-q"},
            description = "Suppress all output except errors"
    )
    private boolean quiet;

    @Parameters(
            arity = "1..*",
            description = "Input files to convert"
    )
    private String[] inputFiles;

    private MarkItDownEngine engine;

    @Override
    public Integer call() throws Exception {
        try {
            // Initialize engine
            engine = createEngine();

            // Configure options
            ConversionOptions options = createConversionOptions();

            // Process files
            int successCount = 0;
            int errorCount = 0;

            for (String inputFile : inputFiles) {
                try {
                    if (inputFile.contains("*") || inputFile.contains("?")) {
                        // Handle wildcards
                        processWildcard(inputFile, options);
                    } else {
                        // Handle single file
                        processFile(inputFile, options);
                    }
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    if (!quiet) {
                        System.err.println("Error processing " + inputFile + ": " + e.getMessage());
                    }
                    if (verbose) {
                        e.printStackTrace();
                    }
                }
            }

            // Print summary
            if (!quiet && (inputFiles.length > 1 || successCount > 0)) {
                System.out.printf("Conversion completed: %d successful, %d failed%n", successCount, errorCount);
            }

            return errorCount > 0 ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 2;
        }
    }

    /**
     * Creates and configures the MarkItDown engine.
     *
     * @return configured engine
     */
    private MarkItDownEngine createEngine() {
        ConverterRegistry registry = new ConverterRegistry();
        // Register all converters
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

    /**
     * Creates conversion options from command-line arguments.
     *
     * @return configured conversion options
     */
    // ToDo: 这里的Option设置有点人机
    private ConversionOptions createConversionOptions() {
        ConversionOptions.Builder builder = ConversionOptions.builder();

        // Process boolean options with precedence
        boolean includeImages = this.includeImages != null ? this.includeImages : !noImages;
        boolean includeTables = this.includeTables != null ? this.includeTables : !noTables;
        boolean includeMetadata = this.includeMetadata != null ? this.includeMetadata : !noMetadata;

        builder.includeImages(includeImages)
               .includeTables(includeTables)
               .includeMetadata(includeMetadata)
               .useOcr(useOcr)
               .language(language)
               .tableFormat(tableFormat)
               .imageFormat(imageFormat)
               .maxFileSize(maxFileSize);

        if (tempDir != null) {
            builder.tempDirectory(Paths.get(tempDir));
        }

        return builder.build();
    }

    /**
     * Processes a single file.
     *
     * @param inputFile the input file path
     * @param options   conversion options
     * @throws ConversionException if conversion fails
     */
    private void processFile(String inputFile, ConversionOptions options) throws ConversionException {
        Path inputPath = Paths.get(inputFile);
        File inputFileObj = inputPath.toFile();

        if (!inputFileObj.exists()) {
            throw new ConversionException("Input file does not exist: " + inputFile);
        }

        if (!inputFileObj.isFile()) {
            throw new ConversionException("Input path is not a file: " + inputFile);
        }

        // Check if file type is supported
        if (!engine.isSupported(inputPath)) {
            throw new ConversionException("Unsupported file type: " + inputFile);
        }

        // Convert the file
        ConversionResult result = engine.convert(inputPath, options);

        // Determine output path
        Path outputPath = determineOutputPath(inputPath);

        // Write result to file
        writeResult(result, outputPath);

        if (!quiet) {
            System.out.printf("Converted: %s -> %s%n", inputFile, outputPath);
        }

        if (verbose && result.hasWarnings()) {
            System.out.println("Warnings:");
            for (String warning : result.getWarnings()) {
                System.out.println("  - " + warning);
            }
        }
    }

    /**
     * Processes wildcard patterns.
     *
     * @param pattern the wildcard pattern
     * @param options conversion options
     * @throws IOException if file operations fail
     */
    private void processWildcard(String pattern, ConversionOptions options) throws IOException {
        Path parentPath = Paths.get(pattern).getParent();
        if (parentPath == null) {
            parentPath = Paths.get(".");
        }

        String fileName = Paths.get(pattern).getFileName().toString();
        String globPattern = fileName.replace("*", ".*").replace("?", ".");

        Files.list(parentPath)
                .filter(path -> path.getFileName().toString().matches(globPattern))
                .filter(path -> path.toFile().isFile())
                .filter(engine::isSupported)
                .forEach(path -> {
                    try {
                        processFile(path.toString(), options);
                    } catch (ConversionException e) {
                        if (!quiet) {
                            System.err.println("Error processing " + path + ": " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Determines the output path based on input path and options.
     *
     * @param inputPath the input file path
     * @return the output file path
     */
    private Path determineOutputPath(Path inputPath) {
        if (output != null) {
            Path outputPath = Paths.get(output);

            // If output is a directory, use input filename with .md extension
            if (Files.isDirectory(outputPath) || output.endsWith("/") || output.endsWith("\\")) {
                String fileName = inputPath.getFileName().toString();
                String nameWithoutExt = getFileNameWithoutExtension(fileName);
                return outputPath.resolve(nameWithoutExt + ".md");
            }

            return outputPath;
        }

        // Default: same directory as input with .md extension
        String fileName = inputPath.getFileName().toString();
        String nameWithoutExt = getFileNameWithoutExtension(fileName);
        Path parentPath = inputPath.getParent();

        if (parentPath != null) {
            return parentPath.resolve(nameWithoutExt + ".md");
        } else {
            // File is in root directory, use current directory
            return Paths.get(nameWithoutExt + ".md");
        }
    }

    /**
     * Writes the conversion result to the output file.
     *
     * @param result     the conversion result
     * @param outputPath the output file path
     * @throws ConversionException if writing fails
     */
    private void writeResult(ConversionResult result, Path outputPath) throws ConversionException {
        try {
            // Create parent directories if they don't exist
            Path parentPath = outputPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            // Write the markdown content
            // 这里的写对于批量处理不是很明确
            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(result.getMarkdown());
            }

        } catch (IOException e) {
            throw new ConversionException("Failed to write output file: " + e.getMessage());
        }
    }

    /**
     * Gets the file name without extension.
     *
     * @param fileName the file name
     * @return the file name without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MarkItDownCommand()).execute(args);
        System.exit(exitCode);
    }
}