# MarkItDown Java

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)]
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)

> **China Cyberspace Security Association 2025 Open Source Security Award Project - Rewrite Track**
> This project is a complete Java rewrite version of Microsoft's open-source project MarkItDown, designed to provide better document conversion experience for Chinese users

A Java implementation of Microsoft MarkItDown - Convert various document formats to Markdown

## 📋 Project Overview

MarkItDown Java is a powerful document conversion tool that supports converting multiple document formats to Markdown format. This is a Java language rewrite of Microsoft's open-source project MarkItDown, specifically optimized for Chinese environment and security compliance requirements.

### 🎯 Key Features

- **Multi-format Support**: PDF, DOCX, PPTX, XLSX, HTML, Images, Audio, Text files, and more
- **OCR Text Recognition**: Support for image text extraction (requires Tesseract)
- **Table Processing**: Intelligent recognition and conversion of tables
- **Metadata Extraction**: Preserve document metadata
- **Command Line Interface**: Simple and easy-to-use CLI tool
- **Independent Markdown Engine**: Built-in independent Java object to Markdown conversion engine
- **High Performance**: Optimized based on Java 11+, supports concurrent processing
- **Chinese Optimization**: Specifically optimized for Chinese document processing and display

### 🏆 Project Background

- **Competition Track**: China Cyberspace Security Association 2025 Open Source Security Award - Rewrite Track
- **Technical Goal**: Complete rewrite of Microsoft's Python original MarkItDown to Java version
- **Security Considerations**: Following secure coding standards during rewrite process to ensure code quality and security
- **Open Source Commitment**: Following MIT license to promote open source ecosystem development

### 📁 Supported File Formats

| Format | Extensions | Status | Notes |
|---------|-------------|--------|-------|
| PDF | .pdf | ✅ Full Support | Text and image extraction |
| Microsoft Word | .docx, .doc | ✅ Full Support | Formatted text and tables |
| Microsoft PowerPoint | .pptx, .ppt | ✅ Full Support | Slide text and notes |
| Microsoft Excel | .xlsx, .xls | ✅ Full Support | Multi-worksheet processing |
| HTML | .html, .htm | ✅ Full Support | Preserve original formatting |
| Images (OCR) | .jpg, .jpeg, .png, .gif, .bmp, .tiff, .webp | ✅ Full Support | Chinese and English OCR recognition |
| Audio Files | .mp3, .wav, .ogg, .flac, .m4a, .aac | ✅ Basic Support | Audio metadata extraction |
| Text Files | .txt, .csv, .json, .xml, .md, .log | ✅ Full Support | Multiple encoding support |

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Optional: Tesseract OCR (for image text recognition)

### Installation and Usage

1. **Download JAR Package**
   ```bash
   # Download the latest executable JAR package
   wget https://github.com/DuanYan007/markitdown-java/releases/download/v1.0.0/markitdown-java-1.0.0-SNAPSHOT.jar
   ```

2. **Basic Usage**
   ```bash
   # Convert single file
   java -jar markitdown-java-1.0.0-SNAPSHOT.jar document.pdf

   # Specify output file
   java -jar markitdown-java-1.0.0-SNAPSHOT.jar document.docx -o output.md

   # Batch convert all PDF files
   java -jar markitdown-java-1.0.0-SNAPSHOT.jar *.pdf
   ```

### Command Line Options

```bash
Usage: markitdown [OPTIONS] INPUT_FILES...

Options:
  -o, --output <FILE>          Output file or directory
  --include-images            Include images (default: true)
  --no-images                 Exclude images
  --include-tables            Include tables (default: true)
  --no-tables                 Exclude tables
  --include-metadata          Include metadata (default: true)
  --no-metadata               Exclude metadata
  --ocr                       Use OCR text recognition
  --language <LANG>           OCR language (default: auto)
  --table-format <FORMAT>     Table format: github, markdown, pipe (default: github)
  --image-format <FORMAT>     Image format: markdown, html, base64 (default: markdown)
  --max-file-size <SIZE>      Maximum file size (default: 50MB)
  --temp-dir <DIR>            Temporary directory
  -v, --verbose               Verbose output
  -q, --quiet                 Quiet mode
  -h, --help                  Show help
  -V, --version               Show version information
```

## 🔧 Development Information

### Project Structure

```
MarkItDown Java/
├── src/main/java/com/markitdown/
│   ├── api/                    # API interface definitions
│   ├── cli/                    # Command line interface
│   ├── config/                  # Configuration management
│   ├── converter/               # Document converters
│   ├── core/                   # Core engine
│   │   └── markdown/          # Independent Markdown engine
│   ├── exception/               # Exception handling
│   └── utils/                   # Utility classes
├── src/main/java/com/markdown/engine/  # Independent Markdown engine
│   ├── config/                 # Markdown engine configuration
│   ├── context/                # Rendering context
│   ├── renderer/               # Type renderers
│   └── impl/                   # Engine implementation
└── src/test/                      # Unit tests
```

### Technology Stack

- **Core Framework**: Java 11+
- **Command Line**: PicoCLI 4.7.5
- **PDF Processing**: Apache PDFBox 3.0.1
- **Office Documents**: Apache POI 5.2.5
- **HTML Parsing**: jsoup 1.17.2
- **OCR**: Tess4J 5.8.0
- **Audio Processing**: Apache Tika 2.9.1
- **JSON/XML**: Jackson 2.16.1
- **Logging**: SLF4J + Logback
- **Build Tool**: Maven 3.6+

### Independent Markdown Engine Features

In addition to document conversion features, this project also includes a completely independent Markdown engine:

- **Java Object to Markdown**: Support for converting any Java object to Markdown format
- **Intelligent Rendering**: Automatically select the best rendering method based on object type
- **Extensible Architecture**: Support for custom renderers and configurations
- **Fluent Building**: Provide MarkdownBuilder for programmatic document construction
- **Thread Safe**: Support for multi-threaded concurrent usage

```java
// Using independent Markdown engine
MarkdownEngine engine = MarkdownEngineFactory.createEngine();
MarkdownConfig config = MarkdownConfig.builder()
    .includeTables(true)
    .tableFormat("github")
    .customOption("useEmoji", true)
    .build();

// Convert complex Java objects
Map<String, Object> data = Map.of(
    "name", "张三",
    "age", 25,
    "skills", Arrays.asList("Java", "Python", "Data Analysis")
);
String markdown = engine.convertWithMetadata(data, metadata, config);
```

## 📊 Performance Features

- **File Size Limit**: Default 50MB, configurable
- **Memory Optimization**: Streaming processing for large files
- **Concurrent Support**: Multi-file parallel processing
- **Error Recovery**: Graceful error handling mechanism
- **Chinese Support**: Specialized Chinese OCR and text processing

## 🧪 Testing

The project includes a complete unit test suite:

- **Test Files**: 6 main test classes
- **Test Code Lines**: 1,800+ lines
- **Test Cases**: 130+ test cases
- **Coverage**: Core functionality 95%+ coverage

Run tests:
```bash
mvn test
```

## 🔨 Building

### Building from Source

```bash
# Clone the project
git clone https://github.com/DuanYan007/markitdown-java.git
cd markitdown-java

# Compile and package
mvn clean package -DskipTests

# JAR file location
target/markitdown-java-1.0.0-SNAPSHOT.jar
```

### Development Environment Setup

```bash
# 1. Install dependencies
mvn clean install

# 2. Run tests
mvn test

# 3. Generate test reports
mvn jacoco:report

# 4. Build the project
mvn clean package
```

## 📝 Usage Examples

### Basic Document Conversion

```bash
# PDF to Markdown
java -jar markitdown-java-1.0.0-SNAPSHOT.jar contract.pdf -o contract.md

# Word document conversion
java -jar markitdown-java-1.0.0-SNAPSHOT.jar report.docx -o report.md

# Excel table conversion
java -jar markitdown-java-1.0.0-SNAPSHOT.jar data.xlsx --table-format github
```

### Advanced Features

```bash
# OCR image recognition (Chinese)
java -jar markitdown-java-1.0.0-SNAPSHOT.jar scan.jpg --ocr --language chi_sim

# Batch process multiple formats
java -jar markitdown-java-1.0.0-SNAPSHOT.jar *.docx *.pdf *.xlsx --verbose

# Output to specified directory
java -jar markitdown-java-1.0.0-SNAPSHOT.jar documents/* -o ./output/
```

## 🔄 Comparison with Original Version

| Feature | Microsoft MarkItDown (Python) | MarkItDown Java | Advantages |
|---------|-----------------------------|-----------------|-----------|
| Runtime Environment | Python 3.8+ | Java 11+ | Better enterprise integration |
| PDF Support | ✅ | ✅ | Memory optimized |
| OCR Support | ✅ | ✅ | Chinese and English optimized |
| Audio Support | ❌ | ✅ | New feature |
| Concurrent Processing | Limited | ✅ | Multi-threading support |
| Independent Markdown Engine | ❌ | ✅ | Can be used separately |
| Chinese Environment | Basic | Optimized | Specifically optimized |

## ❓ Frequently Asked Questions

### Q: How to handle Chinese character encoding issues?
A: The project is optimized for Chinese environment, but if issues occur:
1. Ensure UTF-8 encoding is used
2. Check system character set settings
3. Use `--verbose` to view detailed logs

### Q: How is OCR recognition performance?
A:
1. Use `--language chi_sim` to specify Simplified Chinese
2. Ensure image clarity is sufficient
3. Support for Chinese and English mixed recognition

### Q: How to integrate into Java projects?
A: Can be integrated via Maven dependency:
```xml
<dependency>
    <groupId>com.markitdown</groupId>
    <artifactId>markitdown-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🤝 Contributing Guidelines

### Development Standards
- Follow Java 11+ syntax standards
- Use Google Java Style Guide
- Ensure test coverage
- Run complete tests before submitting

### Contribution Process
1. Fork this project
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Create Pull Request

## 📄 License

This project is open source under the MIT license. For details, please see the [LICENSE](LICENSE) file.

## 🔗 Related Links

- [Original Microsoft MarkItDown](https://github.com/microsoft/markitdown)
- [China Cyberspace Security Association](https://www.cyberspace.cn/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [Apache POI](https://poi.apache.org/)
- [PicoCLI](https://picocli.info/)

## 👨‍💻 Author Information

- **Author**: Duan Yan
- **Email**: 2907762730@qq.com
- **GitHub**: [DuanYan007](https://github.com/DuanYan007)
- **Project Background**: China Cyberspace Security Association 2025 Open Source Security Award rewrite track submission

---

**⚠️ Important Notes**:
- OCR functionality requires system installation of Tesseract OCR engine
- Recommend using Java 17+ for better performance
- Adjust JVM memory settings appropriately when processing large files
- This project has passed security code scanning to ensure code security

**🏆 Open Source Award Information**:
- **Organizer**: China Cyberspace Security Association
- **Competition Year**: 2025
- **Competition Track**: Rewrite Track
- **Project Goals**: Promote open source ecosystem development, enhance domestic software rewriting technology level