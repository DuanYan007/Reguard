import com.markitdown.core.markdown.MarkdownEngineFactory;
import com.markitdown.config.ConversionOptions;

public class TestLink {
    public static void main(String[] args) {
        var engine = MarkdownEngineFactory.getDefaultEngine();
        var options = ConversionOptions.builder()
                .includeMetadata(false)
                .includeTables(true)
                .build();

        var builder = engine.createBuilder();
        String result = builder
                .heading("Main Title", 1)
                .paragraph("This is introduction paragraph.")
                .horizontalRule()
                .heading("Section 1", 2)
                .bold("Important text")
                .italic("emphasized text")
                .inlineCode("code snippet")
                .link("Click here", "https://example.com")
                .build();

        System.out.println("Result:");
        System.out.println(result);
        System.out.println("Contains 'https://example.com': " + result.contains("https://example.com"));
    }
}