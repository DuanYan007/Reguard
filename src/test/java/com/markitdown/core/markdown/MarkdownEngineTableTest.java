package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表格渲染测试 - 测试Map到Markdown表格的转换功能
 * 包括：简单Map表格渲染、复杂Map定义列表渲染、表格格式变化
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("MarkdownEngine Table Rendering Tests")
class MarkdownEngineTableTest {

    private MarkdownEngine engine;
    private ConversionOptions options;

    @BeforeEach
    void setUp() {
        engine = MarkdownEngineFactory.createEngine();
        options = ConversionOptions.builder()
                .includeMetadata(false) // 测试时不包含元数据
                .includeTables(true)
                .build();
    }

    /**
     * 测试8: 简单Map表格渲染（表格启用时）
     * 功能: 测试基础类型Map到GitHub表格的转换
     * 测试重点: 表头、行格式、字符转义
     */
    @Test
    @DisplayName("Test 8: Simple Map Table Rendering (Tables Enabled)")
    void testSimpleMapTableRenderingWithTablesEnabled() {
        Map<String, Object> simpleData = new LinkedHashMap<>();
        simpleData.put("Name", "John Doe");
        simpleData.put("Age", 30);
        simpleData.put("Country", "United States");
        simpleData.put("Active", true);
        simpleData.put("Salary", 50000.50);

        String result = engine.convert(simpleData, options);

        // 验证表格结构
        assertTrue(result.contains("| Key | Value |"), "应该包含表头");
        assertTrue(result.contains("|------|-------|"), "应该包含分隔符");

        // 验证表格内容（不考虑顺序）
        assertTrue(result.contains("| Name | John Doe |"), "应该包含姓名");
        assertTrue(result.contains("| Age | 30 |"), "应该包含年龄");
        assertTrue(result.contains("| Country | United States |"), "应该包含国家");
        assertTrue(result.contains("| Active | true |"), "应该包含布尔值");
        assertTrue(result.contains("| Salary | 50000.5 |"), "应该包含数字");
    }

    /**
     * 测试9: 复杂Map作为定义列表渲染
     * 功能: 测试包含嵌套结构的Map到定义列表的转换
     * 测试重点: 智能判断、嵌套内容处理
     */
    @Test
    @DisplayName("Test 9: Complex Map as Definition List Rendering")
    void testComplexMapAsDefinitionListRendering() {
        Map<String, Object> complexData = new LinkedHashMap<>();

        // 创建嵌套数据
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "New York");
        address.put("zip", "10001");

        List<String> hobbies = Arrays.asList("Reading", "Coding", "Travel");

        complexData.put("name", "Alice Johnson");
        complexData.put("age", 28);
        complexData.put("address", address); // 嵌套Map - 应该触发定义列表
        complexData.put("hobbies", hobbies); // 集合 - 应该触发定义列表

        String result = engine.convert(complexData, options);

        // 验证定义列表格式
        assertTrue(result.contains("name: Alice Johnson"), "应该包含姓名定义");
        assertTrue(result.contains("age: 28"), "应该包含年龄定义");
        assertTrue(result.contains("address: [3 items]"), "嵌套Map应该显示为[项目数]");
        assertTrue(result.contains("hobbies: [3 items]"), "集合应该显示为[项目数]");
    }

    /**
     * 测试10: 不同表格格式支持
     * 功能: 测试GitHub、Markdown、管道符三种表格格式
     * 测试重点: 格式选择正确性
     */
    @ParameterizedTest
    @DisplayName("Test 10: Different Table Format Support")
    @MethodSource("tableFormatProvider")
    void testDifferentTableFormatSupport(String tableFormat) {
        ConversionOptions formatOptions = ConversionOptions.builder()
                .tableFormat(tableFormat)
                .includeTables(true)
                .build();

        Map<String, Object> testData = Map.of(
                "Column1", "Value1",
                "Column2", "Value2"
        );

        String result = engine.convert(testData, formatOptions);

        // 验证包含表头
        assertTrue(result.contains("| Column1 | Column2 |"), "应该包含表头");

        // 验证不同的分隔符格式
        if ("github".equals(tableFormat) || "markdown".equals(tableFormat)) {
            assertTrue(result.contains("|------|--------|"), "GitHub/Markdown格式应该使用---分隔符");
        } else if ("pipe".equals(tableFormat)) {
            assertTrue(result.contains("|:-----|:-------|"), "管道符格式应该使用:分隔符");
        }
    }

    // 测试数据提供者
    static List<String> tableFormatProvider() {
        return Arrays.asList("github", "markdown", "pipe");
    }

    /**
     * 测试11: 表格功能禁用时的Map渲染
     * 功能: 当禁用表格时，Map应该渲染为定义列表而非表格
     * 测试重点: 选项优先级、回退行为
     */
    @Test
    @DisplayName("Test 11: Map Rendering with Tables Disabled")
    void testMapRenderingWithTablesDisabled() {
        ConversionOptions noTablesOptions = ConversionOptions.builder()
                .includeTables(false)
                .build();

        Map<String, Object> simpleData = Map.of(
                "Product", "Laptop",
                "Price", 999.99,
                "In Stock", true
        );

        String result = engine.convert(simpleData, noTablesOptions);

        // 即使是简单Map，表格禁用时应该渲染为定义列表
        assertTrue(result.contains("Product: Laptop"), "应该包含产品定义");
        assertTrue(result.contains("Price: 999.99"), "应该包含价格定义");
        assertTrue(result.contains("In Stock: true"), "应该包含库存定义");

        // 验证不包含表格格式
        assertFalse(result.contains("|"), "不应该包含表格分隔符");
        assertFalse(result.contains("---"), "不应该包含表格分隔线");
    }

    /**
     * 测试12: 特殊字符在表格中的转义
     * 功能: 测试包含Markdown特殊字符的表格数据
     * 测试重点: 表格内容安全、转义正确性
     */
    @Test
    @DisplayName("Test 12: Special Character Escaping in Tables")
    void testSpecialCharacterEscapingInTables() {
        Map<String, Object> problematicData = Map.of(
                "Content", "This contains **bold** and *italic* text",
                "Code", "`console.log('hello')`",
                "Link", "[GitHub](https://github.com)",
                "Description", "Use # for headings"
        );

        String result = engine.convert(problematicData, options);

        // 验证表格结构存在
        assertTrue(result.contains("| Content | Code |"), "应该包含表头");

        // 验证特殊字符被正确转义
        assertTrue(result.contains("\\*\\*bold\\*\\*"), "粗体应该被转义");
        assertTrue(result.contains("\\*italic\\*"), "斜体应该被转义");
        assertTrue(result.contains("\\`console.log\\('hello\\')\\`"), "代码应该被转义");
        assertTrue(result.contains("\\[GitHub\\]\\(https://github.com\\)"), "链接应该被转义");
        assertTrue(result.contains("Use \\# for headings"), "#应该被转义");
    }

    /**
     * 测试13: 数值在表格中的格式化
     * 功能: 测试整数、浮点数、大数的表格显示
     * 测试重点: 数值格式化、精度保持
     */
    @Test
    @DisplayName("Test 13: Number Formatting in Tables")
    void testNumberFormattingInTables() {
        Map<String, Object> numberData = Map.of(
                "Integer", 42,
                "Double", 3.14159265359,
                "Float", 2.718f,
                "Long", 9223372036854775807L,
                "Scientific", 1.23E8
        );

        String result = engine.convert(numberData, options);

        // 验证各种数值类型都被正确显示
        assertTrue(result.contains("| Integer | 42 |"), "整数应该原样显示");
        assertTrue(result.contains("| Double | 3.14159265359 |"), "浮点数应该原样显示");
        assertTrue(result.contains("| Float | 2.718 |"), "单精度浮点应该原样显示");
        assertTrue(result.contains("| Long | 9223372036854775807 |"), "长整数应该原样显示");
        // 科学计数法可能被格式化为标准格式
        assertTrue(result.contains("| Scientific |"), "科学计数法应该被处理");
    }

    /**
     * 测试14: 布尔值在表格中的表示
     * 功能: 测试true/false值的表格显示
     * 测试重点: 布尔值正确表示、大小写敏感
     */
    @Test
    @DisplayName("Test 14: Boolean Value Representation in Tables")
    void testBooleanValueRepresentationInTables() {
        Map<String, Object> booleanData = Map.of(
                "Active", true,
                "Completed", false,
                "Valid", true,
                "Failed", false
        );

        String result = engine.convert(booleanData, options);

        // 验证布尔值正确显示
        assertTrue(result.contains("| Active | true |"), "true应该显示为true");
        assertTrue(result.contains("| Completed | false |"), "false应该显示为false");
        assertTrue(result.contains("| Valid | true |"), "第二个true应该显示为true");
        assertTrue(result.contains("| Failed | false |"), "第二个false应该显示为false");
    }
}