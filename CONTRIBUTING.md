# Contributing to MarkItDown Java

Thank you for your interest in contributing to MarkItDown Java! This document provides guidelines and information for contributors.

## 🤝 How to Contribute

### Reporting Issues

1. **Search existing issues** before creating a new one
2. **Use clear, descriptive titles** for issues
3. **Provide detailed information** including:
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, Java version, etc.)
   - Sample files (if applicable)

### Submitting Pull Requests

1. **Fork the repository** and create a feature branch
2. **Follow coding standards** and add tests
3. **Update documentation** as needed
4. **Ensure all tests pass**
5. **Submit a pull request** with a clear description

## 🛠️ Development Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher
- Git

### Local Development

```bash
# Clone the repository
git clone https://github.com/yourusername/markitdown-java.git
cd markitdown-java

# Install dependencies
mvn clean install

# Run tests
mvn test

# Build the project
mvn clean package
```

### IDE Setup

#### IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Import as Maven project
3. Configure code style to use project settings
4. Enable annotation processing for Picocli

#### Eclipse

1. Import as existing Maven project
2. Configure build path
3. Install the Checkstyle plugin (optional)

## 📝 Coding Standards

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 100 characters
- Use meaningful variable and method names

### Code Quality

- Write unit tests for new functionality
- Maintain test coverage above 80%
- Use meaningful commit messages
- Document public APIs with JavaDoc

### Example Code

```java
/**
 * Converts PDF documents to Markdown format.
 *
 * @author John Doe
 * @since 1.0.0
 */
public class PdfConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    @Override
    public ConversionResult convert(Path file, ConversionOptions options) {
        logger.info("Converting PDF file: {}", file);
        // Implementation...
    }
}
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PdfConverterTest

# Run tests with coverage
mvn clean test jacoco:report
```

### Writing Tests

- Use JUnit 5 for unit tests
- Use Mockito for mocking
- Test both positive and negative scenarios
- Include integration tests for complex scenarios

### Test Structure

```java
@ExtendWith(MockitoExtension.class)
class PdfConverterTest {

    @Mock
    private ConversionOptions options;

    @InjectMocks
    private PdfConverter converter;

    @Test
    void shouldConvertPdfToMarkdown() {
        // Given
        Path pdfFile = Paths.get("test.pdf");

        // When
        ConversionResult result = converter.convert(pdfFile, options);

        // Then
        assertThat(result.getTextContent()).contains("# Converted Document");
    }
}
```

## 📋 Supported File Formats

When adding support for new file formats:

1. **Create converter class** implementing `DocumentConverter`
2. **Add format detection** in `FileTypeDetector`
3. **Write comprehensive tests**
4. **Update documentation**
5. **Add sample files** to test resources

### Example: Adding New Format Support

```java
@Component
public class CustomFormatConverter implements DocumentConverter {

    @Override
    public boolean supports(String mimeType) {
        return "application/custom-format".equals(mimeType);
    }

    @Override
    public ConversionResult convert(Path file, ConversionOptions options) {
        // Conversion logic here
    }
}
```

## 📖 Documentation

- Update README.md for new features
- Add JavaDoc for public APIs
- Update the GitHub Wiki with detailed guides
- Include examples in documentation

## 🔍 Code Review Process

1. **Self-review** your changes
2. **Ensure tests pass**
3. **Update documentation**
4. **Submit pull request**
5. **Address review feedback**
6. **Merge after approval**

## 🏷️ Release Process

Releases are managed by project maintainers:

1. Update version in `pom.xml`
2. Update CHANGELOG.md
3. Create release tag
4. Deploy to Maven Central
5. Update GitHub releases

## 🐛 Bug Reports

When reporting bugs:

1. **Use the bug report template**
2. **Provide minimal reproduction case**
3. **Include environment information**
4. **Attach relevant files** (redacted if necessary)

## 💡 Feature Requests

For feature requests:

1. **Check existing issues** and discussions
2. **Provide clear use case** and rationale
3. **Consider implementation complexity**
4. **Be willing to contribute** if possible

## 📞 Getting Help

- Create an issue for bugs or feature requests
- Start a discussion for questions
- Check the [Wiki](https://github.com/yourusername/markitdown-java/wiki)
- Review existing issues and discussions

## 🏆 Recognition

Contributors are recognized in:

- README.md contributors section
- Release notes
- GitHub contributors page
- Project documentation

Thank you for contributing to MarkItDown Java! 🎉