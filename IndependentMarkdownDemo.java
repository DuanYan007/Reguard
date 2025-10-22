import com.markdown.engine.MarkdownEngine;
import com.markdown.engine.MarkdownEngineFactory;
import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;

import java.util.*;

/**
 * 独立Markdown引擎演示 - 展示完全独立的Java对象到Markdown转换功能
 * 这个引擎完全独立于现有的MarkItDown项目，不依赖任何文档转换功能
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class IndependentMarkdownDemo {

    public static void main(String[] args) {
        System.out.println("=== 独立Markdown引擎演示 ===\n");

        // 1. 使用默认引擎
        demoDefaultEngine();

        // 2. 配置化引擎演示
        demoConfigurableEngine();

        // 3. 不同类型的引擎演示
        demoSpecializedEngines();

        // 4. 复杂对象渲染演示
        demoComplexObjectRendering();

        // 5. MarkdownBuilder流式构建演示
        demoMarkdownBuilder();

        // 6. 自定义渲染器演示
        demoCustomRenderers();

        // 7. 元数据处理演示
        demoMetadataHandling();
    }

    /**
     * 演示默认引擎的基本功能
     */
    private static void demoDefaultEngine() {
        System.out.println("1. 默认引擎基本功能演示");
        System.out.println("=====================");

        MarkdownEngine engine = MarkdownEngineFactory.getDefaultEngine();

        // 字符串转换
        String text = "这是一个包含 **粗体**、*斜体*、`代码` 的文本";
        String result = engine.convert(text);
        System.out.println("字符串转换结果:");
        System.out.println(result);

        // 数字转换
        Number number = 1234567.89;
        System.out.println("数字转换: " + engine.convert(number));

        // 布尔值转换
        Boolean bool = true;
        System.out.println("布尔值转换: " + engine.convert(bool));
        System.out.println();
    }

    /**
     * 演示配置化引擎
     */
    private static void demoConfigurableEngine() {
        System.out.println("2. 配置化引擎演示");
        System.out.println("================");

        // 创建自定义配置
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(true)
                .tableFormat("github")
                .includeMetadata(true)
                .sortMapKeys(true)
                .escapeHtml(true)
                .listStyle("asterisk")
                .dateFormat("yyyy年MM月dd日")
                .customOption("useEmoji", true)
                .build();

        MarkdownEngine engine = MarkdownEngineFactory.createEngine(config);

        // 测试Map转换为表格
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("姓名", "张三");
        data.put("年龄", 25);
        data.put("活跃", true);
        data.put("分数", 95.5);
        data.put("注册时间", new Date());

        System.out.println("Map转表格演示:");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "用户信息表");
        metadata.put("author", "系统生成");

        String result = engine.convertWithMetadata(data, metadata, config);
        System.out.println(result);
    }

    /**
     * 演示专业化引擎
     */
    private static void demoSpecializedEngines() {
        System.out.println("3. 专业化引擎演示");
        System.out.println("=================");

        // 表格优化引擎
        MarkdownEngine tableEngine = MarkdownEngineFactory.createTableOptimizedEngine();
        Map<String, Object> tableData = Map.of(
                "产品", "iPhone 15",
                "价格", "¥8999",
                "库存", 50,
                "热销", true
        );
        System.out.println("表格优化引擎:");
        System.out.println(tableEngine.convert(tableData));

        // 简单文本引擎
        MarkdownEngine simpleEngine = MarkdownEngineFactory.createSimpleTextEngine();
        System.out.println("简单文本引擎:");
        System.out.println(simpleEngine.convert("这是一个简单的文本，没有特殊格式"));

        // 富格式引擎
        MarkdownEngine richEngine = MarkdownEngineFactory.createRichFormattingEngine();
        System.out.println("富格式引擎 (布尔值带emoji):");
        System.out.println(richEngine.convert(false));
        System.out.println();
    }

    /**
     * 演示复杂对象渲染
     */
    private static void demoComplexObjectRendering() {
        System.out.println("4. 复杂对象渲染演示");
        System.out.println("===================");

        MarkdownEngine engine = MarkdownEngineFactory.getDefaultEngine();

        // 集合渲染
        List<String> items = Arrays.asList(
                "第一项包含 **粗体**",
                "第二项包含 *斜体*",
                "第三项包含 `代码`"
        );
        System.out.println("集合渲染为无序列表:");
        System.out.println(engine.convert(items));

        // 复杂Map渲染
        Map<String, Object> complexData = new LinkedHashMap<>();
        complexData.put("用户信息", Map.of(
                "姓名", "李四",
                "年龄", 30,
                "标签", Arrays.asList("VIP", "技术达人", "早期用户")
        ));
        complexData.put("订单列表", Arrays.asList(
                Map.of("编号", "ORDER001", "金额", 299.99),
                Map.of("编号", "ORDER002", "金额", 599.50)
        ));
        complexData.put("统计", Map.of(
                "总订单", 2,
                "总金额", 899.49,
                "平均金额", 449.745
        ));

        System.out.println("复杂Map渲染为定义列表:");
        System.out.println(engine.convert(complexData));
    }

    /**
     * 演示MarkdownBuilder流式构建
     */
    private static void demoMarkdownBuilder() {
        System.out.println("5. MarkdownBuilder流式构建演示");
        System.out.println("==========================");

        MarkdownBuilder builder = MarkdownEngineFactory.createEngine().createBuilder();

        String document = builder
                .heading("产品对比报告", 1)
                .paragraph("本报告对比了智能手机和笔记本电脑的规格参数。")
                .horizontalRule()
                .heading("主要规格对比", 2)
                .bold("显示屏").text("6.1英寸 vs 15.6英寸").newline()
                .bold("存储").text("256GB vs 512GB").newline()
                .bold("摄像头").text("三摄 vs 高清摄像头").newline()
                .horizontalRule()
                .heading("使用建议", 2)
                .unorderedList("智能手机：适合移动办公、拍照、社交",
                               "笔记本电脑：适合专业工作、学习、娱乐")
                .heading("购买链接", 2)
                .link("查看详细规格", "https://example.com/specs")
                .blockquote("笔记本电脑更适合专业工作，智能手机提供更好的便携性。")
                .build();

        System.out.println("流式构建的文档:");
        System.out.println(document);
    }

    /**
     * 演示自定义渲染器
     */
    private static void demoCustomRenderers() {
        System.out.println("6. 自定义渲染器演示");
        System.out.println("===================");

        MarkdownEngine engine = MarkdownEngineFactory.createEngine();

        // 注册自定义渲染器
        engine.registerRenderer(Person.class, new PersonRenderer());

        Person person = new Person("王五", 35, Arrays.asList("阅读", "编程", "旅行"));

        System.out.println("自定义Person渲染器:");
        System.out.println(engine.convert(person));
    }

    /**
     * 演示元数据处理
     */
    private static void demoMetadataHandling() {
        System.out.println("7. 元数据处理演示");
        System.out.println("==================");

        MarkdownEngine engine = MarkdownEngineFactory.createEngine();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("文档标题", "测试文档");
        metadata.put("作者", "系统管理员");
        metadata.put("创建时间", new Date());
        metadata.put("版本", "1.0.0");
        metadata.put("标签", Arrays.asList("测试", "演示", "Markdown"));

        String content = "这是带有元数据的内容演示。";

        System.out.println("带元数据的文档:");
        System.out.println(engine.convertWithMetadata(content, metadata));
    }

    /**
     * 自定义Person类用于演示
     */
    static class Person {
        private String name;
        private int age;
        private List<String> hobbies;

        public Person(String name, int age, List<String> hobbies) {
            this.name = name;
            this.age = age;
            this.hobbies = hobbies;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
        public List<String> getHobbies() { return hobbies; }
    }

    /**
     * 自定义Person渲染器
     */
    static class PersonRenderer implements com.markdown.engine.ObjectRenderer<Person> {
        @Override
        public String render(Object object, com.markdown.engine.context.RenderContext context) {
            Person person = (Person) object;
            if (person == null) {
                return "";
            }

            MarkdownBuilder builder = new MarkdownBuilder(context.getConfig());
            return builder
                    .heading("个人信息", 2)
                    .bold("姓名:").text(" " + person.getName()).newline()
                    .bold("年龄:").text(" " + person.getAge()).newline()
                    .bold("爱好:").text(" " + String.join(", ", person.getHobbies()))
                    .build();
        }

        @Override
        public boolean supports(Object object) {
            return object instanceof Person;
        }

        @Override
        public int getPriority() {
            return 100; // 高优先级
        }

        @Override
        public String getName() {
            return "PersonRenderer";
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<Person> getTargetClass() {
            return Person.class;
        }
    }
}