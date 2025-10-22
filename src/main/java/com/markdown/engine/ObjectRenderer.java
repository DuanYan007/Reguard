package com.markdown.engine;

import com.markdown.engine.context.RenderContext;

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
     * @param object  the object to render
     * @param context the rendering context
     * @return Markdown formatted string
     */
    String render(Object object, RenderContext context);

    /**
     * Checks if this renderer supports the given object type.
     *
     * @param object the object to check
     * @return true if this renderer can handle the object
     */
    boolean supports(Object object);

    /**
     * Gets the priority of this renderer. Higher values indicate higher priority.
     * Priority is used when multiple renderers support the same object type.
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

    /**
     * Gets the target class this renderer is designed for.
     * This is used for type-specific registration and lookup.
     *
     * @return the target class
     */
    Class<T> getTargetClass();

    /**
     * Checks if this renderer can handle null values.
     * Most renderers should return false, but some may need to handle null specially.
     *
     * @return true if this renderer can handle null values
     */
    default boolean supportsNull() {
        return false;
    }

    /**
     * Gets a description of what this renderer does.
     *
     * @return renderer description
     */
    default String getDescription() {
        return "Renders " + getTargetClass().getSimpleName() + " objects to Markdown";
    }
}