import com.markitdown.core.markdown.MarkdownEngineFactory;
import com.markitdown.config.ConversionOptions;

public class TestComplex {
    public static void main(String[] args) {
        var engine = MarkdownEngineFactory.getDefaultEngine();
        var options = ConversionOptions.builder()
                .includeMetadata(false)
                .includeTables(true)
                .build();

        String complexInput = "Complex text with: # heading, *bold*, _italic_, `inline code`, [link](url), ![image](url), - list item, + list item, > blockquote, --- horizontal rule, `\\\\escape sequence`, number 123, boolean true, and newlines\n\n\n";
        String result = engine.convert(complexInput, options);

        System.out.println("Output length: " + result.length());
        System.out.println("Contains 'escape sequence': " + result.contains("escape sequence"));
        System.out.println("Contains '\\\\escape sequence': " + result.contains("\\\\escape sequence"));
        System.out.println("Contains '\\\\\\\\escape sequence': " + result.contains("\\\\\\escape sequence"));
    }
}