package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

import java.text.SimpleDateFormat;
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
    public String render(Object object, RenderContext context) {
        Date date = (Date) object;
        if (date == null) {
            return "";
        }

        String dateFormat = context.getDateFormat();
        if (dateFormat == null || dateFormat.trim().isEmpty()) {
            dateFormat = "yyyy-MM-dd HH:mm:ss";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format(date);
        } catch (Exception e) {
            return date.toString(); // Fallback to default format
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Date;
    }

    @Override
    public int getPriority() {
        return 65; // High priority for dates
    }

    @Override
    public String getName() {
        return "DateRenderer";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Date> getTargetClass() {
        return Date.class;
    }

    @Override
    public String getDescription() {
        return "Renders Date objects to Markdown with customizable date format";
    }
}