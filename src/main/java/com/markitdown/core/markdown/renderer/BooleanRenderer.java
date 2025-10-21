package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;

/**
 * Renderer for Boolean values to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class BooleanRenderer implements ObjectRenderer<Boolean> {

    @Override
    public String render(Object object, MarkdownContext context) {
        Boolean value = (Boolean) object;
        if (value == null) {
            return "";
        }

        return value ? "[YES]" : "[NO]";
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Boolean;
    }

    @Override
    public int getPriority() {
        return 90; // Very high priority for booleans
    }

    @Override
    public String getName() {
        return "BooleanRenderer";
    }
}