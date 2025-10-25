package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class XlsxConverter
 * @brief Excel电子表格转换器，用于将XLSX文件转换为Markdown格式
 * @details 使用Apache POI库解析Excel工作簿，提取工作表数据和结构信息
 *          支持多工作表处理、自动表头检测、数据类型转换等功能
 *          将表格数据转换为标准Markdown表格格式
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class XlsxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(XlsxConverter.class);

    private MarkdownBuilder mb;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("正在转换XLSX文件: {}", filePath);
        // ToDo: MarkdownConfig 并未和 Conversion Options 共享元素，待解决
        mb = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            // 提取元数据
            Map<String, Object> metadata = extractMetadata(workbook, options);

            // 将工作簿转换为Markdown格式
            String markdownContent = convertToMarkdown(workbook, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "处理XLSX文件失败: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType) ||
               "application/vnd.ms-excel".equals(mimeType) ||
               "application/vnd.ms-excel.sheet.macroEnabled.12".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "XlsxConverter";
    }

    /**
     * 从Excel工作簿中提取元数据信息
     *
     * @param workbook Excel工作簿对象
     * @param options  转换选项配置
     * @return 包含元数据信息的映射表
     */
    private Map<String, Object> extractMetadata(Workbook workbook, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 工作簿统计信息
            metadata.put("工作表数量", workbook.getNumberOfSheets());
            metadata.put("当前工作表索引(0为起始索引)", workbook.getActiveSheetIndex());
            metadata.put("转换时刻", LocalDateTime.now());

            // 计算总单元格数量（近似值）
            // Todo: 活跃单元格数量有必要统计么?
            int totalCells = 0;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                totalCells += estimateSheetSize(sheet);
            }
            metadata.put("统计单元格数量", totalCells);
        }

        return metadata;
    }

    /**
     * 估算工作表中的单元格数量
     *
     * @param sheet 要分析的工作表对象
     * @return 估算的单元格数量
     */
    private int estimateSheetSize(Sheet sheet) {
        int cellCount = 0;
        for (Row row : sheet) {
            cellCount += row.getPhysicalNumberOfCells();
        }
        return cellCount;
    }

    /**
     * 将Excel工作簿转换为Markdown格式内容
     *
     * @param workbook Excel工作簿对象
     * @param metadata 文档元数据信息
     * @param options  转换选项配置
     * @return Markdown格式的内容字符串
     */
    private String convertToMarkdown(Workbook workbook, Map<String, Object> metadata, ConversionOptions options) {

        // 如果有标题则添加标题
        if (options.isIncludeMetadata() && metadata.containsKey("文件名")) {
            String title = (String) metadata.get("文件名");
            if (title != null && !title.trim().isEmpty()) {
                mb.append(mb.h1(mb.escapeMarkdown(title.trim())));
            }
        }
        // 如果启用则添加元数据部分
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            mb.append(mb.h2("工作簿信息"));
            List<StringBuilder> meta = new ArrayList<>();
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    String tile = formatMetadataKey(entry.getKey());
                    // Todo: 关于元数据中的对象 ?
                    String value = entry.getValue().toString();
                    meta.add(new StringBuilder().append("**").append(tile).append(":** ").append(value));
                }
            }
            // Todo: 这里可能有问题，需要排查
            mb.append(mb.unorderedList(meta.toArray(new StringBuilder[meta.size()])));
            mb.newline();
        }

        // 处理所有工作表
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, i + 1, options);
        }

        return mb.flush();
    }

    /**
     * 处理单个工作表并将其转换为Markdown格式
     *
     * @param sheet    要处理的工作表对象
     * @param sheetNum 工作表编号（从1开始）
     * @param options  转换选项配置
     */
    private void processSheet(Sheet sheet, int sheetNum, ConversionOptions options) {
        String sheetName = sheet.getSheetName();
        mb.append(mb.h2("工作表 " + sheetNum + ": " + sheetName));
        if (!options.isIncludeTables()) {
            mb.append(mb.italic("表格功能在转换选项中被禁用"));
            mb.newline(2);
            return;
        }

        // 查找数据范围
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();

        if (firstRow < 0 || lastRow < 0 || lastRow < firstRow) {
            mb.append(mb.italic("空工作表"));
            mb.newline(2);
            mb.horizontalRule();
            return;
        }

        // 判断第一行是否可能是表头
        boolean hasHeader = detectHeaderRow(sheet, firstRow);

        // 处理数据
        if (hasHeader) {
            processTableWithHeader(sheet, firstRow, lastRow, options);
        } else {
            processTableWithoutHeader(sheet, firstRow, lastRow, options);
        }

        mb.horizontalRule();
    }

    /**
     * 检测第一行是否可能是表头行
     *
     * @param sheet    要分析的工作表对象
     * @param firstRow 第一行的索引
     * @return 如果第一行可能是表头则返回true
     */
    private boolean detectHeaderRow(Sheet sheet, int firstRow) {
        Row firstRowData = sheet.getRow(firstRow);
        if (firstRowData == null) {
            return false;
        }

        int nonEmptyCells = 0;
        int stringCells = 0;
        int totalCells = firstRowData.getPhysicalNumberOfCells();

        for (Cell cell : firstRowData) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                nonEmptyCells++;
                if (cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        stringCells++;
                    }
                }
            }
        }

        // 如果大多数单元格非空且许多是字符串，则认为是表头
        return totalCells > 0 && (double) nonEmptyCells / totalCells > 0.7 && (double) stringCells / totalCells > 0.5;
    }

    /**
     * 处理带表头行的表格
     *
     * @param sheet    包含表格的工作表对象
     * @param firstRow 第一行的索引
     * @param lastRow  最后一行的索引
     * @param options  转换选项配置
     */
    private void processTableWithHeader(Sheet sheet, int firstRow, int lastRow
                                       , ConversionOptions options) {
        List<String> headers= new ArrayList<>();
        Row headRow = sheet.getRow(firstRow);
        for(Cell cell : headRow) {
            headers.add(getCellValueAsString(cell).trim());
        }
        List<List<String>> data = new ArrayList<>();
        for(int i = firstRow + 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            for(Cell cell : row) {
                data.get(i - firstRow).add(getCellValueAsString(cell).trim());
            }
        }
        String[][] table = data.stream().map(list -> list.toArray(new String[0])).toArray(String[][]::new);
        mb.append(mb.table(headers.toArray(new String[0]), table));

    }

    /**
     * 处理不带表头行的表格
     *
     * @param sheet    包含表格的工作表对象
     * @param firstRow 第一行的索引
     * @param lastRow  最后一行的索引
     * @param options  转换选项配置
     */
    private void processTableWithoutHeader(Sheet sheet, int firstRow, int lastRow
                                         , ConversionOptions options) {
        List<String> headers= new ArrayList<>();
        Row headRow = sheet.getRow(firstRow);
        for(Cell cell : headRow) {
            headers.add("default");
        }
        List<List<String>> data = new ArrayList<>();
        for(int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            data.add(new ArrayList<>());
            for(Cell cell : row) {
                data.get(i - firstRow).add(getCellValueAsString(cell).trim());
            }
        }
        String[][] table = data.stream().map(list -> list.toArray(new String[0])).toArray(String[][]::new);
        mb.append(mb.table(headers.toArray(new String[0]), table));
    }

    /**
     * 将单元格值转换为字符串表示
     *
     * @param cell 要转换的单元格对象
     * @return 单元格值的字符串表示
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理大数和小数
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.format("%d", (long) numValue);
                    } else {
                        return String.format("%s", numValue);
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // 尝试计算公式
                    CellValue evaluatedValue = cell.getSheet().getWorkbook().getCreationHelper()
                            .createFormulaEvaluator().evaluate(cell);
                    if (evaluatedValue != null) {
                        switch (evaluatedValue.getCellType()) {
                            case STRING:
                                return evaluatedValue.getStringValue();
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    return cell.getDateCellValue().toString();
                                } else {
                                    double numValue = evaluatedValue.getNumberValue();
                                    if (numValue == (long) numValue) {
                                        return String.format("%d", (long) numValue);
                                    } else {
                                        return String.format("%s", numValue);
                                    }
                                }
                            case BOOLEAN:
                                return Boolean.toString(evaluatedValue.getBooleanValue());
                            default:
                                return "";
                        }
                    }
                } catch (Exception e) {
                    // 如果计算失败，返回公式本身
                    return cell.getCellFormula();
                }
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 格式化元数据键名以供显示
     *
     * @param key 元数据键名
     * @return 格式化后的键名
     */
    private String formatMetadataKey(String key) {
        // 将驼峰命名转换为标题格式
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                ;
    }
}