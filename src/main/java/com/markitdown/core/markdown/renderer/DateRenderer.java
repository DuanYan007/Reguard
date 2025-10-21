package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Renderer for Date objects to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class DateRenderer implements ObjectRenderer<Date> {

    @Override
    public String render(Object object, MarkdownContext context) {
        Date date = (Date) object;
        if (date == null) {
            return "";
        }

        // Try to format with ISO format for modern dates
        try {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return localDateTime.format(formatter);
        } catch (Exception e) {
            // Fallback to default format
            return date.toString();
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Date;
    }

    @Override
    public int getPriority() {
        return 75; // High priority for dates
    }

    @Override
    public String getName() {
        return "DateRenderer";
    }
}