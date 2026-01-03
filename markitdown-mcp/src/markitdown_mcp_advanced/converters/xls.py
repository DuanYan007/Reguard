"""
支持 .xls 格式（Excel 97-2003）

依赖：需要安装 xlrd
pip install xlrd==1.2.0
"""
# tested
from pathlib import Path

def convert(source: Path) -> str:
    """将 Excel (.xls) 转换为 Markdown"""

    # 懒加载：尝试导入依赖
    try:
        import xlrd
        # from openpyxl import Workbook
    except ImportError:
        return """# 需要安装依赖

当前使用标准库模式，仅支持 .xlsx 格式。

要支持 .xls 格式，请安装依赖：
```bash
pip install xlrd==1.2.0 openpyxl
```

**或者**手动将文件另存为 .xlsx 格式（推荐）：
1. 在 Excel 中打开文件
2. 选择"文件" → "另存为"
3. 格式选择"Excel 工作簿 (.xlsx)"
4. 保存后重新转换

---

**说明**：
- .xlsx 是现代格式（基于 XML），使用标准库即可解析
- .xls 是旧格式（二进制），需要第三方库支持
- 为保持项目轻量级，.xls 支持为可选项
"""

    try:
        # 执行转换
        return _convert_xls_to_markdown(source)
    except Exception as e:
        return f"# 转换错误\n\n{str(e)}"


def _convert_xls_to_markdown(source: Path) -> str:
    """使用 xlrd 读取 .xls，直接转换为 Markdown"""
    import xlrd

    # 读取 .xls
    xls_workbook = xlrd.open_workbook(source, formatting_info=False, on_demand=True)

    # 收集所有工作表数据
    all_sheets = []

    for sheet_idx in range(xls_workbook.nsheets):
        xls_sheet = xls_workbook.sheet_by_index(sheet_idx)

        # 读取工作表数据
        sheet_data = []
        for row_idx in range(xls_sheet.nrows):
            row_data = []
            for col_idx in range(xls_sheet.ncols):
                cell_value = xls_sheet.cell_value(row_idx, col_idx)
                processed_value = _process_cell_value(cell_value, xls_sheet, row_idx, col_idx)
                row_data.append(processed_value)
            sheet_data.append(row_data)

        all_sheets.append({
            'name': xls_sheet.name,
            'data': sheet_data
        })

    # 释放 .xls 资源
    xls_workbook.release_resources()

    # 格式化为 Markdown（复用 xlsx.py 的格式化逻辑）
    return _format_sheets(all_sheets)


def _format_sheets(sheets: list) -> str:
    markdown_parts = []

    for sheet in sheets:
        sheet_name = sheet['name']
        data = sheet['data']

        # 添加工作表标题
        markdown_parts.append(f"\n## {sheet_name}\n")

        if not data:
            markdown_parts.append("*（空工作表）*\n")
            continue

        # 转换为 Markdown 表格
        max_cols = max(len(row) for row in data)

        # 填充空单元格
        padded_data = []
        for row in data:
            padded_row = row + [''] * (max_cols - len(row))
            padded_data.append(padded_row)

        # 生成表格
        if padded_data:
            header = '| ' + ' | '.join(str(padded_data[0][i]) if i < len(padded_data[0]) else '' for i in range(max_cols)) + ' |'
            separator = '|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|'
            markdown_parts.append(header)
            markdown_parts.append(separator)

            for row in padded_data[1:]:
                row_text = '| ' + ' | '.join(str(cell or ' ') for cell in row) + ' |'
                markdown_parts.append(row_text)

        markdown_parts.append('')

    return '\n'.join(markdown_parts).strip()



def _process_cell_value(value, xls_sheet, row_idx: int, col_idx: int) -> any:
    """处理单元格值，转换数据类型"""
    import xlrd

    # 获取单元格类型
    cell_type = xls_sheet.cell_type(row_idx, col_idx)

    # XLRD 单元格类型常量
    # XL_CELL_EMPTY = 0
    # XL_CELL_TEXT = 1
    # XL_CELL_NUMBER = 2
    # XL_CELL_DATE = 3
    # XL_CELL_BOOLEAN = 4
    # XL_CELL_ERROR = 5

    if cell_type == xlrd.XL_CELL_EMPTY:
        return ""

    elif cell_type == xlrd.XL_CELL_TEXT:
        return str(value).strip()

    elif cell_type == xlrd.XL_CELL_NUMBER:
        # 判断是否为整数
        if isinstance(value, float) and value.is_integer():
            return int(value)
        return value

    elif cell_type == xlrd.XL_CELL_DATE:
        # 转换 Excel 日期为 datetime 对象
        try:
            date_tuple = xlrd.xldate_as_tuple(value, xls_sheet.book.datemode)
            if date_tuple[:3] == (0, 0, 0):
                # 仅时间
                return "{:02d}:{:02d}:{:02d}".format(*date_tuple[3:6])
            elif date_tuple[3:] == (0, 0, 0):
                # 仅日期
                return "{:04d}-{:02d}-{:02d}".format(*date_tuple[:3])
            else:
                # 日期时间
                return "{:04d}-{:02d}-{:02d} {:02d}:{:02d}:{:02d}".format(*date_tuple)
        except Exception:
            return str(value)

    elif cell_type == xlrd.XL_CELL_BOOLEAN:
        return "TRUE" if value else "FALSE"

    elif cell_type == xlrd.XL_CELL_ERROR:
        return f"#ERROR({value})"

    else:
        return str(value)
