from pathlib import Path
import zipfile
import xml.etree.ElementTree as ET
from typing import List
# tested

def convert(source: Path) -> str:
    """将 Excel (.xlsx) 转换为 Markdown"""
    try:
        with zipfile.ZipFile(source, 'r') as zip_ref:
            # 读取工作簿数据
            workbook_data = _parse_workbook_xml(zip_ref)
            shared_strings = _parse_shared_strings(zip_ref)
            sheets = _parse_worksheets(zip_ref, workbook_data, shared_strings)

            if not sheets:
                return "# 空文档\n\n此 Excel 文件没有内容"

            # 生成 Markdown
            return _format_sheets(sheets)

    except Exception as e:
        return f"# 转换错误\n\n{str(e)}"


def _parse_workbook_xml(zip_ref: zipfile.ZipFile) -> dict:
    """解析工作簿 XML，获取工作表信息"""
    try:
        with zip_ref.open('xl/workbook.xml') as f:
            tree = ET.parse(f)
            root = tree.getroot()

            # 命名空间
            ns = {'main': 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'}

            sheets = {}
            sheets_elem = root.find('.//main:sheets', ns)

            if sheets_elem is not None:
                for idx, sheet in enumerate(sheets_elem.findall('main:sheet', ns)):
                    sheet_id = sheet.get('sheetId')
                    name = sheet.get('name')
                    rid = sheet.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id')
                    sheets[rid] = {
                        'id': sheet_id,
                        'name': name,
                        'index': idx
                    }

            return sheets

    except Exception:
        return {}


def _parse_shared_strings(zip_ref: zipfile.ZipFile) -> List[str]:
    """解析共享字符串表"""
    try:
        with zip_ref.open('xl/sharedStrings.xml') as f:
            tree = ET.parse(f)
            root = tree.getroot()

            ns = {'main': 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'}

            strings = []
            for si in root.findall('.//main:si', ns):
                text = ""
                t_elem = si.find('main:t', ns)
                if t_elem is not None:
                    text = t_elem.text or ""

                # 处理富文本（多个 <t> 标签）
                for t in si.findall('.//main:t', ns):
                    if t != t_elem:
                        text += t.text or ""

                strings.append(text)

            return strings

    except Exception:
        return []


def _parse_worksheets(zip_ref: zipfile.ZipFile, workbook_data: dict, shared_strings: List[str]) -> List[dict]:
    """解析所有工作表"""
    sheets = []

    try:
        # 读取关系文件，获取工作表文件路径
        with zip_ref.open('xl/_rels/workbook.xml.rels') as f:
            tree = ET.parse(f)
            root = tree.getroot()

            ns = {'rel': 'http://schemas.openxmlformats.org/package/2006/relationships'}

            for rel in root.findall('.//rel:Relationship', ns):
                rid = rel.get('Id')
                target = rel.get('Target')

                if rid in workbook_data and target:
                    sheet_info = workbook_data[rid]
                    # 解析工作表数据
                    worksheet_path = f"xl/{target}"
                    sheet_data = _parse_worksheet(zip_ref, worksheet_path, shared_strings)

                    sheets.append({
                        'name': sheet_info['name'],
                        'index': sheet_info['index'],
                        'data': sheet_data
                    })

    except Exception:
        pass

    # 按索引排序
    sheets.sort(key=lambda x: x['index'])
    return sheets


def _parse_worksheet(zip_ref: zipfile.ZipFile, worksheet_path: str, shared_strings: List[str]) -> List[List[str]]:
    """解析单个工作表"""
    try:
        with zip_ref.open(worksheet_path) as f:
            tree = ET.parse(f)
            root = tree.getroot()

            ns = {'main': 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'}

            rows = []
            for row_elem in root.findall('.//main:row', ns):
                row_data = []
                cells = row_elem.findall('main:c', ns)

                # 处理每个单元格
                for cell in cells:
                    ref = cell.get('r')  # 单元格引用，如 "A1"
                    cell_type = cell.get('t')  # 类型：s=共享字符串, n=数字, 等

                    value_elem = cell.find('main:v', ns)
                    if value_elem is not None and value_elem.text:
                        value = value_elem.text

                        # 根据类型处理值
                        if cell_type == 's':  # 共享字符串
                            try:
                                idx = int(value)
                                if 0 <= idx < len(shared_strings):
                                    value = shared_strings[idx]
                            except (ValueError, IndexError):
                                pass
                        elif cell_type == 'n':  # 数字
                            try:
                                # 尝试转换为整数或浮点数
                                if '.' in value:
                                    num = float(value)
                                    # 如果是整数，去掉小数点
                                    if num.is_integer():
                                        value = str(int(num))
                                    else:
                                        value = str(num)
                                else:
                                    value = str(int(float(value)))
                            except ValueError:
                                pass

                        row_data.append(value)

                if row_data:
                    rows.append(row_data)

            return rows

    except Exception:
        return []


def _format_sheets(sheets: List[dict]) -> str:
    """将工作表数据格式化为 Markdown"""
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
        if len(data) > 0:
            # 计算最大列数
            max_cols = max(len(row) for row in data)

            # 填充空单元格
            padded_data = []
            for row in data:
                padded_row = row + [''] * (max_cols - len(row))
                padded_data.append(padded_row)

            # 生成表格
            # 表头
            if padded_data:
                header = '| ' + ' | '.join(padded_data[0]) + ' |'
                separator = '|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|'
                markdown_parts.append(header)
                markdown_parts.append(separator)

                # 数据行
                for row in padded_data[1:]:
                    row_text = '| ' + ' | '.join(cell or ' ' for cell in row) + ' |'
                    markdown_parts.append(row_text)

            markdown_parts.append('')

    return '\n'.join(markdown_parts).strip()
