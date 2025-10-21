import com.markitdown.core.markdown.MarkdownEngine;
import com.markitdown.core.markdown.MarkdownEngineFactory;
import com.markitdown.config.ConversionOptions;
import java.util.*;

/**
 * Markdown Engine Demo - Shows various object to Markdown conversions
 */
public class MarkdownEngineDemo {

    public static void main(String[] args) {
        MarkdownEngine engine = MarkdownEngineFactory.getDefaultEngine();
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .includeTables(true)
                .build();

        System.out.println("=== Markdown Engine Demo ===\n");

        // 1. String Rendering
        demoStringRendering(engine, options);

        // 2. Primitive Types
        demoPrimitiveTypes(engine, options);

        // 3. Collection Rendering
        demoCollectionRendering(engine, options);

        // 4. Map to Table
        demoMapTableRendering(engine, options);

        // 5. Map to Definition List
        demoMapDefinitionRendering(engine, options);

        // 6. Complex Objects
        demoComplexObjects(engine, options);

        // 7. MarkdownBuilder
        demoMarkdownBuilder(engine);

        // 8. With Metadata
        demoWithMetadata(engine, options);
    }

    private static void demoStringRendering(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("1. String Rendering");
        System.out.println("==================");

        String text = "This text contains **bold**, *italic*, `inline code`, and [links](https://example.com).";
        String result = engine.convert(text, options);

        System.out.println("Input: " + text);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoPrimitiveTypes(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("2. Primitive Types");
        System.out.println("==================");

        Object[] primitives = {
            "Number:", 42,
            "Double:", 3.14159,
            "Boolean (true):", true,
            "Boolean (false):", false
        };

        String result = engine.convert(primitives, options);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoCollectionRendering(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("3. Collection Rendering");
        System.out.println("==================");

        List<String> items = Arrays.asList(
            "First item with **bold** text",
            "Second item with *italic* text",
            "Third item with `inline code`"
        );

        String result = engine.convert(items, options);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoMapTableRendering(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("4. Map to Table Rendering");
        System.out.println("==================");

        Map<String, Object> personInfo = new LinkedHashMap<>();
        personInfo.put("Name", "John Doe");
        personInfo.put("Age", 30);
        personInfo.put("City", "New York");
        personInfo.put("Active", true);
        personInfo.put("Score", 95.5);

        String result = engine.convert(personInfo, options);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoMapDefinitionRendering(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("5. Map to Definition List Rendering");
        System.out.println("==================");

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("theme", "dark");
        config.put("language", "zh-CN");
        config.put("features", Arrays.asList("tables", "charts"));

        String result = engine.convert(config, options);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoComplexObjects(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("6. Complex Objects");
        System.out.println("==================");

        List<Map<String, Object>> products = new ArrayList<>();

        Map<String, Object> phone = new LinkedHashMap<>();
        phone.put("name", "iPhone Pro");
        phone.put("specs", Arrays.asList("6.1 inch", "256GB", "Triple camera"));
        phone.put("price", 999.99);

        Map<String, Object> laptop = new LinkedHashMap<>();
        laptop.put("name", "MacBook Pro");
        laptop.put("specs", Arrays.asList("15.6 inch", "512GB", "Retina display"));
        laptop.put("price", 1999.00);

        products.add(phone);
        products.add(laptop);

        String result = engine.convert(products, options);
        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoMarkdownBuilder(MarkdownEngine engine) {
        System.out.println("7. MarkdownBuilder");
        System.out.println("==================");

        String result = engine.createBuilder()
                .heading("Product Comparison Report", 1)
                .paragraph("This report compares smartphone and laptop specifications.")
                .horizontalRule()
                .heading("Key Specifications", 2)
                .bold("Display Size").text("6.1 inch vs 15.6 inch")
                .bold("Storage").text("256GB vs 512GB")
                .bold("Camera").text("Triple camera vs Retina display")
                .horizontalRule()
                .blockquote("Laptop is better for professional work, smartphone offers better portability.")
                .link("Full Report", "https://example.com/reports/comparison")
                .build();

        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }

    private static void demoWithMetadata(MarkdownEngine engine, ConversionOptions options) {
        System.out.println("8. With Metadata");
        System.out.println("==================");

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", "Markdown Engine Demo");
        metadata.put("author", "Claude");
        metadata.put("created", new Date());
        metadata.put("version", "1.0.0");
        metadata.put("tags", Arrays.asList("demo", "markdown", "java"));

        String content = "# Sample Content\n\nThis is a demonstration of the markdown engine's metadata capabilities.";
        String result = engine.convertWithMetadata(content, metadata, options);

        System.out.println("Output:");
        System.out.println(result);
        System.out.println();
    }
}