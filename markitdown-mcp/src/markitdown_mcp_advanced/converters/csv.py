from pathlib import Path
# tested

def convert(source: Path) -> str:
    with open(source, "r", encoding="utf-8") as f:
        content = f.read()
    if not content:
        return "| 空内容 |\n|---------|"

    try:
        if not isinstance(content, str):
            return "| 输入错误：内容必须是字符串 |\n|-----------------------|"

        lines = content.strip().split('\n')
        if not lines:
            return "| 空CSV数据 |\n|----------|"

        non_empty_lines = [line.strip() for line in lines if line.strip()]
        if not non_empty_lines:
            return "| CSV数据为空 |\n|------------|"

        ans = ""
        for i, line in enumerate(non_empty_lines):
            try:
                cols = [col.strip() for col in line.split(',')]
                if not cols:
                    cols = [""]

                row_content = "|" + "|".join(cols) + "|"
                ans += "\n" + row_content

                if i == 0:
                    separator = "|" + "|".join(["---" for _ in cols]) + "|"
                    ans += "\n" + separator
            except Exception as e:
                ans += f"\n| 行 {i + 1} 处理错误: {str(e)} |"
                if i == 0:
                    ans += "\n|---------------|"

        return ans.strip()
    except Exception as e:
        return f"| CSV转换错误: {str(e)} |\n|------------------------|"





