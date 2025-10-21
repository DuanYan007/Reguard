package com.markitdown.core.markdown;

/**
 * Interface for rendering specific object types to Markdown.
 * Implementations handle the conversion of particular object types to Markdown format.
 *
 * @param <T> the object type this renderer handles
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ObjectRenderer<T> {

    /**
     * Renders an object to Markdown format.
     *

     * @param context the markdown rendering context
     * @return Markdown formatted string
     */
    String render(Object object, MarkdownContext context);

    /**
     * Checks if this renderer supports the given object type.
     *
     * @param object the object to check
     * @return true if this renderer can handle the object
     */
    boolean supports(Object object);

    /**
     * Gets the priority of this renderer. Higher values indicate higher priority.
     *
     * @return priority value (default is 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Gets the name of this renderer.
     *
     * @return renderer name
     */
    String getName();
}