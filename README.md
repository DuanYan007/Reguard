# MarkItDown Java 📄➡️📝

[![Build Status](https://github.com/yourusername/markitdown-java/workflows/CI/badge.svg)](https://github.com/yourusername/markitdown-java/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.markitdown/markitdown-java.svg)](https://search.maven.org/artifact/com.markitdown/markitdown-java)
[![codecov](https://codecov.io/gh/yourusername/markitdown-java/branch/main/graph/badge.svg)](https://codecov.io/gh/yourusername/markitdown-java)

**MarkItDown Java** is a powerful Java implementation of Microsoft's MarkItDown - a versatile document-to-Markdown converter designed for LLM applications and content processing pipelines.

## ✨ Features

- **📄 Multi-format Support**: Convert PDF, DOCX, PPTX, XLSX, HTML, images, and more to Markdown
- **🚀 High Performance**: Optimized for batch processing and large documents
- **🔧 Modular Design**: Plugin-based architecture for extensible converters
- **☕ Java Native**: Built for the Java ecosystem with enterprise-grade reliability
- **🎯 LLM Ready**: Perfect for preparing documents for Large Language Model processing
- **📦 Easy Integration**: Simple API for seamless integration into your applications

## 🚀 Quick Start

### Installation

#### Maven
```xml
<dependency>
    <groupId>com.markitdown</groupId>
    <artifactId>markitdown-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'com.markitdown:markitdown-java:1.0.0'
```

### Basic Usage

```java
import com.markitdown.MarkItDown;

// Simple conversion
MarkItDown markItDown = new MarkItDown();
String markdown = markItDown.convert("document.pdf").getTextContent();
System.out.println(markdown);

// Advanced usage with options
ConversionOptions options = new ConversionOptions()
    .setIncludeImages(true)
    .setTableFormat("github");

ConversionResult result = markItDown.convert("presentation.pptx", options);
System.out.println(result.getMarkdown());
System.out.println("Metadata: " + result.getMetadata());
```

### Command Line Usage

```bash
# Install via package manager or download the executable JAR
java -jar markitdown-java.jar document.docx -o output.md

# Convert multiple files
java -jar markitdown-java.jar *.pdf -d output/

# Advanced options
java -jar markitdown-java.jar document.pdf --include-images --table-format=github
```

## 📋 Supported Formats

| Format | Extension | Status | Notes |
|--------|-----------|--------|-------|
| **Text Files** | .txt, .md, .csv | ✅ Full Support | |
| **Microsoft Office** | .docx, .pptx, .xlsx | ✅ Full Support | Preserves formatting and structure |
| **PDF** | .pdf | ✅ Full Support | Text extraction and OCR capabilities |
| **HTML** | .html, .htm | ✅ Full Support | Clean conversion to Markdown |
| **Images** | .png, .jpg, .jpeg, .gif, .bmp | ✅ OCR Support | Requires Tesseract OCR |
| **JSON/XML** | .json, .xml | ✅ Full Support | Structured data conversion |
| **Archive** | .zip | ✅ Full Support | Batch processing of contained files |

## 🏗️ Architecture

```
MarkItDown Engine
├── Document Converter Interface
├── Format Detection
├── Converter Registry
├── Individual Converters
│   ├── PDF Converter (Apache PDFBox)
│   ├── Office Converters (Apache POI)
│   ├── HTML Converter (jsoup)
│   ├── Image OCR Converter (Tess4J)
│   └── Text Converters
└── Markdown Builder
```

## 🔧 Configuration

### Programmatic Configuration

```java
MarkItDownConfig config = MarkItDownConfig.builder()
    .enableOcr(true)
    .setOcrLanguage("eng")
    .setMaxFileSize(10_000_000) // 10MB
    .setTempDir("/tmp/markitdown")
    .build();

MarkItDown markItDown = new MarkItDown(config);
```

### Properties Configuration

```properties
# application.properties
markitdown.ocr.enabled=true
markitdown.ocr.language=eng
markitdown.max-file-size=10485760
markitdown.temp-dir=/tmp/markitdown
```

## 🧪 Advanced Examples

### Batch Processing

```java
MarkItDown markItDown = new MarkItDown();
Files.walk(Paths.get("documents"))
    .filter(path -> path.toString().endsWith(".pdf"))
    .forEach(path -> {
        try {
            String markdown = markItDown.convert(path).getTextContent();
            Files.write(Paths.get(path.toString().replace(".pdf", ".md")),
                       markdown.getBytes());
        } catch (IOException e) {
            logger.error("Failed to convert: " + path, e);
        }
    });
```

### Custom Converter

```java
public class CustomConverter implements DocumentConverter {
    @Override
    public boolean supports(String mimeType) {
        return "application/custom".equals(mimeType);
    }

    @Override
    public ConversionResult convert(Path file, ConversionOptions options) {
        // Your custom conversion logic
        return new ConversionResult("# Custom Content\nConverted content", metadata);
    }
}

// Register custom converter
MarkItDown markItDown = new MarkItDown();
markItDown.registerConverter(new CustomConverter());
```

## 📦 Dependencies

- **Apache PDFBox**: PDF processing
- **Apache POI**: Microsoft Office documents
- **jsoup**: HTML parsing
- **Tess4J**: OCR capabilities (optional)
- **Jackson**: JSON/XML processing
- **SLF4J + Logback**: Logging

## 🔒 Security

MarkItDown Java follows security best practices:
- No temporary file creation by default (in-memory processing)
- Configurable file size limits
- Input validation and sanitization
- Regular security updates for dependencies

## 🚀 Performance

- **Memory Efficient**: Streaming processing for large files
- **Fast Conversion**: Optimized parsers and algorithms
- **Concurrent Safe**: Thread-safe implementation
- **Low Latency**: Sub-second conversion for most documents

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

```bash
git clone https://github.com/yourusername/markitdown-java.git
cd markitdown-java
mvn clean install
```

### Running Tests

```bash
mvn test
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Microsoft** for the original [MarkItDown](https://github.com/microsoft/markitdown) project
- **Apache Software Foundation** for PDFBox and POI libraries
- All contributors and users of this project

## 📞 Support

- 📖 [Documentation](https://markitdown-java.github.io)
- 🐛 [Issue Tracker](https://github.com/yourusername/markitdown-java/issues)
- 💬 [Discussions](https://github.com/yourusername/markitdown-java/discussions)

---

⭐ If this project helps you, please give us a star!