package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import java.util.*;

/**
 * Example usage of the MarkdownEngine.
 * Demonstrates various rendering capabilities.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineExample {

    public static void main(String[] args) {
        // Get the markdown engine
        MarkdownEngine engine = MarkdownEngineFactory.getDefaultEngine();

        // Create conversion options
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .includeTables(true)
                .tableFormat("github")
                .build();

        System.out.println("=== Markdown Engine Demo ===");
        System.out.println();

        // Example 1: Simple text
        System.out.println("1. Simple Text:");
        String textResult = engine.convert("Hello, **World**!", options);
        System.out.println(textResult);
        System.out.println();

        // Example 2: Map as table
        System.out.println("2. Map as Table:");
        Map<String, Object> simpleMap = new LinkedHashMap<>();
        simpleMap.put("Name", "John Doe");
        simpleMap.put("Age", 30);
        simpleMap.put("Country", "United States");
        String mapResult = engine.convertWithMetadata(simpleMap,
                Map.of("title", "Person Information"), options);
        System.out.println(mapResult);
        System.out.println();

        // Example 3: Collection as list
        System.out.println("3. Collection as List:");
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        String listResult = engine.convert(items, options);
        System.out.println(listResult);
        System.out.println();

        // Example 4: Complex nested data
        System.out.println("4. Complex Data:");
        Map<String, Object> complexData = new LinkedHashMap<>();
        List<Map<String, Object>> people = new ArrayList<>();

        Map<String, Object> person1 = new LinkedHashMap<>();
        person1.put("name", "Alice");
        person1.put("age", 25);
        person1.put("skills", Arrays.asList("Java", "Python", "SQL"));

        Map<String, Object> person2 = new LinkedHashMap<>();
        person2.put("name", "Bob");
        person2.put("age", 30);
        person2.put("skills", Arrays.asList("JavaScript", "React", "Node.js"));

        people.add(person1);
        people.add(person2);

        complexData.put("team", people);
        complexData.put("project", "Markdown Engine");
        complexData.put("version", "1.0.0");

        String complexResult = engine.convertWithMetadata(complexData,
                Map.of("title", "Project Information", "created", new Date()), options);
        System.out.println(complexResult);
        System.out.println();

        // Example 5: Using MarkdownBuilder
        System.out.println("5. Using MarkdownBuilder:");
        MarkdownBuilder builder = engine.createBuilder();
        String builderResult = builder
                .heading("Custom Document", 1)
                .paragraph("This is a **custom** document created with the *MarkdownBuilder*.")
                .horizontalRule()
                .heading("Features", 2)
                .unorderedList("Easy to use", "Fluent API", "Extensible")
                .horizontalRule()
                .codeBlock("public class Hello {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}", "java")
                .link("GitHub Repository", "https://github.com/example/markdown")
                .build();
        System.out.println(builderResult);
        System.out.println();

        // Example 6: Engine information
        System.out.println("6. Engine Information:");
        EngineInfo info = engine.getEngineInfo();
        System.out.println("Engine: " + info.getName());
        System.out.println("Version: " + info.getVersion());
        System.out.println("Features: " + String.join(", ", info.getSupportedFeatures()));
        System.out.println("Languages: " + String.join(", ", info.getSupportedLanguages()));
        System.out.println();

        // Example 7: Markdown validation
        System.out.println("7. Markdown Validation:");
        String validMarkdown = "# Heading\nThis is a [link](https://example.com)";
        String invalidMarkdown = "# Heading\nThis is a [broken link(https://example.com";

        System.out.println("Valid markdown: " + engine.isValidMarkdown(validMarkdown));
        System.out.println("Invalid markdown: " + engine.isValidMarkdown(invalidMarkdown));
    }
}