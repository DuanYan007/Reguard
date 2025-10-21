package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive MarkdownEngine test class
 * Tests all functionality modules including basic features, table rendering,
 * collection processing, code/media, advanced features, and exception handling.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("MarkdownEngine Comprehensive Tests")
class MarkdownEngineTest {

    private MarkdownEngine engine;
    private ConversionOptions defaultOptions;
    private ConversionOptions noMetadataOptions;
    private ConversionOptions noTablesOptions;

    @BeforeEach
    void setUp() {
        engine = MarkdownEngineFactory.getDefaultEngine();
        defaultOptions = ConversionOptions.builder()
                .includeMetadata(true)
                .includeTables(true)
                .tableFormat("github")
                .build();

        noMetadataOptions = ConversionOptions.builder()
                .includeMetadata(false)
                .includeTables(true)
                .tableFormat("github")
                .build();

        noTablesOptions = ConversionOptions.builder()
                .includeMetadata(true)
                .includeTables(false)
                .tableFormat("github")
                .build();
    }

    // ========== Basic Functionality Tests ==========

    @Test
    @DisplayName("Test 1: Basic String Rendering - Character Escaping")
    void testBasicStringRenderingCharacterEscaping() {
        String input = "Hello, World! This text contains **bold**, *italic*, `code`, [link](url), >quote, and ---rule.";
        String result = engine.convert(input, defaultOptions);

        assertTrue(result.contains("Hello, World! This text contains"), "Plain text should be preserved");
        assertTrue(result.contains("\\*\\*bold\\*\\*"), "Bold markers should be escaped");
        assertTrue(result.contains("\\*italic\\*"), "Italic markers should be escaped");
        assertTrue(result.contains("\\`code\\`"), "Code markers should be escaped");
        assertTrue(result.contains("\\[link\\]\\(url\\)"), "Link markers should be escaped");
        assertTrue(result.contains("\\>quote"), "Quote markers should be escaped");
        assertTrue(result.contains("\\---rule"), "Rule markers should be escaped");
    }

    @Test
    @DisplayName("Test 2: Markdown Syntax Validation")
    void testMarkdownSyntaxValidation() {
        assertTrue(engine.isValidMarkdown("# Heading\nThis is a [link](https://example.com)"), "Valid markdown should be valid");
        assertTrue(engine.isValidMarkdown("- list item\n1. numbered item"), "Ordered lists should be valid");

        assertFalse(engine.isValidMarkdown("This has an [incomplete link"), "Incomplete links should be invalid");
        assertFalse(engine.isValidMarkdown("This has a [] link"), "Empty links should be invalid");
        assertFalse(engine.isValidMarkdown(null), "Null should be invalid");
        assertFalse(engine.isValidMarkdown(""), "Empty string should be invalid");
    }

    @Test
    @DisplayName("Test 3: Engine Information")
    void testEngineInformation() {
        EngineInfo info = engine.getEngineInfo();

        assertEquals("MarkItDown Markdown Engine", info.getName(), "Engine name should be correct");
        assertEquals("1.0.0", info.getVersion(), "Engine version should be correct");

        Set<String> features = info.getSupportedFeatures();
        assertTrue(features.containsAll(Set.of("headings", "paragraphs", "lists", "tables", "code", "links", "images", "metadata", "extensible")));

        Set<String> languages = info.getSupportedLanguages();
        assertTrue(languages.containsAll(Set.of("java", "javascript", "python", "sql", "json", "xml", "html")));
    }

    @Test
    @DisplayName("Test 4: MarkdownBuilder Basic Functionality")
    void testMarkdownBuilderBasicFunctionality() {
        MarkdownBuilder builder = engine.createBuilder();

        String result = builder
                .heading("Main Title", 1)
                .paragraph("Introduction with **bold** and *italic* text.")
                .horizontalRule()
                .unorderedList("Item 1", "Item 2", "Item 3")
                .codeBlock("System.out.println(\"Hello\");", "java")
                .link("GitHub", "https://github.com")
                .build();

        assertTrue(result.contains("# Main Title"), "Should contain level 1 heading");
        assertTrue(result.contains("Introduction with **bold** and *italic* text."), "Should contain paragraph");
        assertTrue(result.contains("---"), "Should contain horizontal rule");
        assertTrue(result.contains("- Item 1"), "Should contain unordered list items");
        assertTrue(result.contains("```java\\nSystem.out.println(\"Hello\");\\n```"), "Should contain code block");
        assertTrue(result.contains("[GitHub](https://github.com)"), "Should contain link");
    }

    // ========== Table Rendering Tests ==========

    @Test
    @DisplayName("Test 5: Simple Map Table Rendering")
    void testSimpleMapTableRendering() {
        Map<String, Object> tableData = new LinkedHashMap<>();
        tableData.put("Name", "John Doe");
        tableData.put("Age", 30);
        tableData.put("Country", "United States");
        tableData.put("Active", true);
        tableData.put("Salary", 50000.50);

        String result = engine.convert(tableData, defaultOptions);

        assertTrue(result.contains("| Name | Age | Country | Active | Salary |"), "Should contain table header");
        assertTrue(result.contains("|------|-----|---------|--------|"), "Should contain separator");
        assertTrue(result.contains("| John Doe | 30 | United States | true | 50000.5 |"), "Should contain table row");
    }

    @Test
    @DisplayName("Test 6: Complex Map as Definition List")
    void testComplexMapAsDefinitionList() {
        Map<String, Object> complexData = new LinkedHashMap<>();

        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "New York");
        address.put("zip", "10001");

        List<String> hobbies = Arrays.asList("Reading", "Coding", "Travel");

        complexData.put("person", "Alice Johnson");
        complexData.put("age", 25);
        complexData.put("address", address);
        complexData.put("hobbies", hobbies);

        String result = engine.convert(complexData, defaultOptions);

        assertTrue(result.contains("person: Alice Johnson"), "Should contain simple key value");
        assertTrue(result.contains("age: 25"), "Should contain numeric value");
        assertTrue(result.contains("address: [3 items]"), "Nested map should show as item count");
        assertTrue(result.contains("hobbies: [3 items]"), "Collection should show as item count");
    }

    @ParameterizedTest
    @DisplayName("Test 7: Different Table Format Support")
    @ValueSource(strings = {"github", "markdown", "pipe"})
    void testDifferentTableFormatSupport(String tableFormat) {
        ConversionOptions formatOptions = ConversionOptions.builder()
                .tableFormat(tableFormat)
                .includeTables(true)
                .build();

        Map<String, Object> testData = Map.of(
                "Column1", "Value1",
                "Column2", "Value2"
        );

        String result = engine.convert(testData, formatOptions);

        assertTrue(result.contains("| Column1 | Column2 |"), "Should contain table header");

        if ("github".equals(tableFormat) || "markdown".equals(tableFormat)) {
            assertTrue(result.contains("|---------|----------|"), "GitHub/Markdown format should use --- separator");
        } else if ("pipe".equals(tableFormat)) {
            assertTrue(result.contains("|:--------|:---------|"), "Pipe format should use : separator");
        }
    }

    @Test
    @DisplayName("Test 8: Table Disabled Behavior")
    void testTableDisabledBehavior() {
        Map<String, Object> simpleData = Map.of(
                "Product", "Laptop",
                "Price", 999.99
        );

        String result = engine.convert(simpleData, noTablesOptions);

        assertTrue(result.contains("Product: Laptop"), "Should render as definition list when tables disabled");
        assertTrue(result.contains("Price: 999.99"), "Should contain value definition");
        assertFalse(result.contains("|"), "Should not contain table format");
    }

    // ========== Collection and List Tests ==========

    @Test
    @DisplayName("Test 9: Basic Collection Rendering")
    void testBasicCollectionRendering() {
        Collection<String> items = Arrays.asList(
                "First item with **bold**",
                "Second item with `code`",
                "Third item with [link](url)",
                "Fourth item"
        );

        String result = engine.convert(items, defaultOptions);

        assertTrue(result.contains("- First item with **bold**"), "Should contain first item");
        assertTrue(result.contains("- Second item with `code`"), "Should contain second item");
        assertTrue(result.contains("- Third item with [link](url)"), "Should contain third item");
        assertTrue(result.contains("- Fourth item"), "Should contain fourth item");
    }

    @Test
    @DisplayName("Test 10: Nested Collection Rendering")
    void testNestedCollectionRendering() {
        List<Map<String, Object>> nestedData = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "Project");
        item1.put("technologies", Arrays.asList("Java", "Spring", "Docker"));

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "Documentation");
        item2.put("format", "Markdown");

        nestedData.add(item1);
        nestedData.add(item2);

        String result = engine.convert(nestedData, defaultOptions);

        assertTrue(result.contains("technologies=["), "Inner collection should be processed");
        assertTrue(result.contains("format: Markdown"), "Inner map value should be rendered");
    }

    @Test
    @DisplayName("Test 11: Array Rendering")
    void testArrayRendering() {
        Object[] arrayData = {"Item 1", "Item 2", "Item 3", null, "Item 5"};

        String result = engine.convert(arrayData, defaultOptions);

        assertTrue(result.contains("- Item 1"), "Should contain first item");
        assertTrue(result.contains("- Item 2"), "Should contain second item");
        assertTrue(result.contains("- Item 3"), "Should contain third item");
        assertTrue(result.contains("- Item 5"), "Should contain fifth item");
    }

    @Test
    @DisplayName("Test 12: Empty and Null Collection Handling")
    void testEmptyAndNullCollectionHandling() {
        String emptyResult = engine.convert(Collections.emptyList(), defaultOptions);
        assertEquals("", emptyResult, "Empty collection should return empty string");

        Collection<String> collectionWithNulls = Arrays.asList("Item 1", null, "Item 3");
        String resultWithNulls = engine.convert(collectionWithNulls, defaultOptions);

        assertTrue(resultWithNulls.contains("- Item 1"), "Non-null elements should be processed");
        assertTrue(resultWithNulls.contains("- Item 3"), "Non-null elements should be processed");
    }

    // ========== Type-Specific Rendering Tests ==========

    @Test
    @DisplayName("Test 13: Number Type Rendering")
    void testNumberTypeRendering() {
        Object[] numberData = {42, 3.14159, 2.718f, 1234567890123456789L};

        String result = engine.convert(numberData, defaultOptions);

        assertTrue(result.contains("- 42"), "Integer should be rendered");
        assertTrue(result.contains("- 3.14159"), "Float should be rendered");
        assertTrue(result.contains("- 2.718"), "Single precision float should be rendered");
        assertTrue(result.contains("- 1234567890123456789"), "Long integer should be rendered");
    }

    @Test
    @DisplayName("Test 14: Boolean Type Rendering")
    void testBooleanTypeRendering() {
        Object[] booleanData = {true, false, true};

        String result = engine.convert(booleanData, defaultOptions);

        assertTrue(result.contains("- true"), "Should render as checkmark");
        assertTrue(result.contains("- false"), "Should render as X mark");
    }

    @Test
    @DisplayName("Test 15: Date Type Rendering")
    void testDateTypeRendering() {
        Date testDate = new Date();
        Object[] dateData = {testDate};

        String result = engine.convert(dateData, defaultOptions);

        assertNotNull(result, "Date should be processed");
        assertTrue(result.length() > 0, "Date conversion should produce content");
    }

    // ========== Metadata Processing Tests ==========

    @Test
    @DisplayName("Test 16: Metadata Inclusion and Exclusion")
    void testMetadataInclusionAndExclusion() {
        Map<String, Object> metadata = Map.of(
                "title", "Test Document",
                "author", "John Doe",
                "created", new Date(),
                "tags", Arrays.asList("tag1", "tag2", "tag3")
        );

        String resultWithMeta = engine.convertWithMetadata("Content", metadata, defaultOptions);
        String resultWithoutMeta = engine.convertWithMetadata("Content", metadata, noMetadataOptions);

        assertTrue(resultWithMeta.contains("## Document Information"), "Should contain metadata title");
        assertTrue(resultWithMeta.contains("**title:** Test Document"), "Should contain document title");
        assertTrue(resultWithMeta.contains("**tags:** [tag1, tag2, tag3]"), "Should contain tag list");

        assertEquals("Content", resultWithoutMeta, "Without metadata should only return content");
        assertFalse(resultWithoutMeta.contains("## Document Information"), "Should not contain metadata title");
    }

    // ========== Advanced Features Tests ==========

    @Test
    @DisplayName("Test 17: MarkdownBuilder Advanced Features")
    void testMarkdownBuilderAdvancedFeatures() {
        MarkdownBuilder builder = engine.createBuilder();

        String result = builder
                .heading("Level 1", 1)
                .heading("Level 2", 2)
                .heading("Level 3", 3)
                .heading("Level 4", 4)
                .heading("Level 5", 5)
                .heading("Level 6", 6)
                .blockquote("This is a blockquote\nwith multiple lines.")
                .image("Alt text", "https://example.com/image.jpg", "Image title")
                .lineBreak()
                .raw("<em>Raw HTML content</em>")
                .build();

        assertTrue(result.contains("# Level 1"), "Should contain level 1 heading");
        assertTrue(result.contains("## Level 2"), "Should contain level 2 heading");
        assertTrue(result.contains("### Level 3"), "Should contain level 3 heading");
        assertTrue(result.contains("#### Level 4"), "Should contain level 4 heading");
        assertTrue(result.contains("##### Level 5"), "Should contain level 5 heading");
        assertTrue(result.contains("###### Level 6"), "Should contain level 6 heading");
        assertTrue(result.contains("> This is a blockquote"), "Should contain blockquote");
        assertTrue(result.contains("![Alt text](https://example.com/image.jpg \"Image title\")"), "Should contain image");
        assertTrue(result.contains("<em>Raw HTML content</em>"), "Should contain raw HTML");
    }

    @Test
    @DisplayName("Test 18: Custom Renderer Registration")
    void testCustomRendererRegistration() {
        ObjectRenderer<String> customRenderer = new ObjectRenderer<String>() {
            @Override
            public String render(Object text, MarkdownContext context) {
                return "[CUSTOM:" + text + "]";
            }

            @Override
            public boolean supports(Object object) {
                return object instanceof String;
            }

            @Override
            public int getPriority() {
                return 200;
            }

            @Override
            public String getName() {
                return "CustomRenderer";
            }
        };

        engine.registerRenderer(String.class, customRenderer);

        String result = engine.convert("test content", defaultOptions);
        assertTrue(result.contains("[CUSTOM:test content]"), "Should use custom renderer");
    }

    // ========== Exception and Edge Case Tests ==========

    @Test
    @DisplayName("Test 19: Exception Safety Handling")
    void testExceptionSafetyHandling() {
        String nullResult = engine.convert(null, defaultOptions);
        assertEquals("", nullResult, "Null input should return empty string");

        String emptyResult = engine.convert("", defaultOptions);
        assertEquals("", emptyResult, "Empty string input should return empty string");
    }

    @Test
    @DisplayName("Test 20: Performance and Memory Test")
    void testPerformanceAndMemoryTest() {
        List<String> largeDataset = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeDataset.add("Item " + i + " with **formatting** and *content*");
        }

        long startTime = System.currentTimeMillis();
        String result = engine.convert(largeDataset, defaultOptions);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Should complete within 5 seconds");

        assertTrue(result.contains("Item 0"), "Should contain first item");
        assertTrue(result.contains("Item 999"), "Should contain last item");
    }

    @Test
    @DisplayName("Test 21: Edge Case Handling")
    void testEdgeCaseHandling() {
        String specialChars = "\t\n\r\f\u0000\u0001\u00A0\uFEFF";
        String result = engine.convert(specialChars, defaultOptions);

        assertNotNull(result, "Special characters input should produce result");
        assertTrue(result.length() > 0, "Special characters input should produce content");
    }

    @Test
    @DisplayName("Test 22: Engine Factory Functionality")
    void testEngineFactoryFunctionality() {
        MarkdownEngine engine1 = MarkdownEngineFactory.createEngine();
        MarkdownEngine engine2 = MarkdownEngineFactory.createEngine();

        assertNotSame(engine1, engine2, "Factory should create different instances");

        MarkdownEngine default1 = MarkdownEngineFactory.getDefaultEngine();
        MarkdownEngine default2 = MarkdownEngineFactory.getDefaultEngine();

        assertSame(default1, default2, "Default engine should be same instance");
    }
}