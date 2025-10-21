import com.markitdown.core.markdown.MarkdownEngineFactory;
import com.markitdown.config.ConversionOptions;

public class TestEscapeSequence {
    public static void main(String[] args) {
        var engine = MarkdownEngineFactory.getDefaultEngine();
        var options = ConversionOptions.builder()
                .includeMetadata(false)
                .includeTables(true)
                .build();

        String complexInput = "Complex text with: # heading, *bold*, _italic_, `inline code`, [link](url), ![image](url), - list item, + list item, > blockquote, --- horizontal rule, `\\\\escape sequence`, number 123, boolean true, and newlines\\n\\n\\n";
        String result = engine.convert(complexInput, options);

        System.out.println("=== Complex Input ===");
        System.out.println(complexInput);
        System.out.println("\n=== Output ===");
        System.out.println(result);
        System.out.println("\n=== Analysis ===");
        System.out.println("Length: " + result.length());
        System.out.println("Contains 'escape sequence': " + result.contains("escape sequence"));
        System.out.println("Contains '\\\\escape sequence': " + result.contains("\\\\escape sequence"));
        System.out.println("Contains '\\\\\\\\escape sequence': " + result.contains("\\\\\\escape sequence"));

        // Find the actual escaped part
        int escapeIndex = result.indexOf("escape sequence");
        if (escapeIndex > 0) {
            String before = result.substring(Math.max(0, escapeIndex - 10), escapeIndex);
            String after = result.substring(escapeIndex, Math.min(result.length(), escapeIndex + 50));
            System.out.println("\nContext around 'escape sequence':");
            System.out.println("Before: '" + before + "'");
            System.out.println("Match: '" + after + "'");
        }
    }
}