package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;

import java.text.NumberFormat;

/**
 * Renderer for numeric values to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class NumberRenderer implements ObjectRenderer<Number> {

    @Override
    public String render(Object object, MarkdownContext context) {
        Number number = (Number) object;
        if (number == null) {
            return "";
        }

        // Format number based on type
        if (number instanceof Integer || number instanceof Long) {
            return NumberFormat.getIntegerInstance().format(number);
        } else if (number instanceof Float || number instanceof Double) {
            return NumberFormat.getNumberInstance().format(number);
        } else {
            return number.toString();
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Number;
    }

    @Override
    public int getPriority() {
        return 80; // High priority for numbers
    }

    @Override
    public String getName() {
        return "NumberRenderer";
    }
}