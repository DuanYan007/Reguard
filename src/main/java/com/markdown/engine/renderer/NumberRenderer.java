package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

/**
 * Renderer for Number objects to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class NumberRenderer implements ObjectRenderer<Number> {

    @Override
    public String render(Object object, RenderContext context) {
        Number number = (Number) object;
        if (number == null) {
            return "0";
        }

        // Format number based on its type
        if (object instanceof Integer || object instanceof Long) {
            return String.format("%,d", number.longValue());
        } else if (object instanceof Float || object instanceof Double) {
            return String.format("%,.2f", number.doubleValue());
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
        return 60; // High priority for numbers
    }

    @Override
    public String getName() {
        return "NumberRenderer";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Number> getTargetClass() {
        return Number.class;
    }

    @Override
    public String getDescription() {
        return "Renders Number objects to Markdown with proper formatting";
    }
}