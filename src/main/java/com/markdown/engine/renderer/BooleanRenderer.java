package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

/**
 * Renderer for Boolean objects to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class BooleanRenderer implements ObjectRenderer<Boolean> {

    @Override
    public String render(Object object, RenderContext context) {
        Boolean value = (Boolean) object;
        if (value == null) {
            return "❓ Unknown";
        }

        // Use emoji or text based on configuration
        Boolean useEmoji = context.getCustomOption("useEmoji", Boolean.class);
        if (Boolean.TRUE.equals(useEmoji)) {
            return value ? "✅ Yes" : "❌ No";
        } else {
            return value ? "[YES]" : "[NO]";
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Boolean;
    }

    @Override
    public int getPriority() {
        return 70; // High priority for booleans
    }

    @Override
    public String getName() {
        return "BooleanRenderer";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Boolean> getTargetClass() {
        return Boolean.class;
    }

    @Override
    public String getDescription() {
        return "Renders Boolean objects to Markdown with optional emoji support";
    }
}