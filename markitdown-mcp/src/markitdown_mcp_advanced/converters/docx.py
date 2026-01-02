import zipfile
import xml.etree.ElementTree as ET
import re
from pathlib import Path
# tested

# XML 命名空间
NS = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
}


def convert(source: Path) -> str:
    """将 Word 文档转换为 Markdown"""
    try:
        content = _parse_docx(source)
        return _process_content(content)
    except Exception as e:
        return f"# 转换错误\n\n{str(e)}"


def _parse_docx(file_path: Path):
    """解析 docx 文件内容"""
    with zipfile.ZipFile(file_path, 'r') as docx_zip:
        with docx_zip.open('word/document.xml') as xml_file:
            tree = ET.parse(xml_file)
            return tree.getroot()


def _process_content(root):
    """处理文档内容"""
    markdown_lines = []

    # 处理段落
    paragraphs = root.findall('.//w:p', NS)
    for paragraph in paragraphs:
        line = _process_paragraph(paragraph)
        if line is not None:
            markdown_lines.append(line)

    # 处理表格
    tables = root.findall('.//w:tbl', NS)
    for table in tables:
        table_md = _process_table(table)
        if table_md:
            markdown_lines.extend(table_md)

    return '\n'.join(markdown_lines)


def _process_paragraph(paragraph):
    """处理段落"""
    style = _get_paragraph_style(paragraph)
    text = _extract_paragraph_text(paragraph)

    if not text.strip():
        return ''

    return _apply_format(text, style)


def _get_paragraph_style(paragraph):
    """获取段落样式"""
    style = {
        'is_heading': False,
        'heading_level': 0,
        'is_list': False,
        'list_level': 0,
        'list_type': 'bullet',
    }

    # 标题样式
    p_style = paragraph.find('.//w:pStyle', NS)
    if p_style is not None:
        style_name = p_style.get(f'{{{NS["w"]}}}val', '')
        if 'Heading' in style_name:
            style['is_heading'] = True
            match = re.search(r'Heading(\d+)', style_name)
            if match:
                style['heading_level'] = int(match.group(1))

    # 列表样式
    num_pr = paragraph.find('.//w:numPr', NS)
    if num_pr is not None:
        style['is_list'] = True
        ilvl = num_pr.find('.//w:ilvl', NS)
        if ilvl is not None:
            style['list_level'] = int(ilvl.get(f'{{{NS["w"]}}}val', 0))

        num_id = num_pr.find('.//w:numId', NS)
        if num_id is not None:
            style['list_type'] = 'number'

    return style


def _extract_paragraph_text(paragraph):
    """提取段落文本"""
    text_parts = []
    for run in paragraph.findall('.//w:r', NS):
        text = _process_run(run)
        if text:
            text_parts.append(text)
    return ''.join(text_parts)


def _process_run(run):
    """处理文本块格式"""
    text = ''

    is_bold = run.find('.//w:b', NS) is not None
    is_italic = run.find('.//w:i', NS) is not None
    is_strike = run.find('.//w:strike', NS) is not None

    for t_elem in run.findall('.//w:t', NS):
        if t_elem.text:
            text += t_elem.text

    if not text:
        return ''

    if is_strike:
        text = f'~~{text}~~'
    if is_italic:
        text = f'*{text}*'
    if is_bold:
        text = f'**{text}**'

    return text


def _apply_format(text, style):
    """应用段落格式"""
    if style['is_heading'] and style['heading_level'] > 0:
        return f"{'#' * style['heading_level']} {text}"

    if style['is_list']:
        indent = '  ' * style['list_level']
        prefix = '1.' if style['list_type'] == 'number' else '-'
        return f"{indent}{prefix} {text}"

    return text


def _process_table(table):
    """处理表格"""
    rows = []
    for tr in table.findall('.//w:tr', NS):
        cells = []
        for tc in tr.findall('.//w:tc', NS):
            cell_text = _extract_cell_content(tc)
            cells.append(cell_text.strip())
        if cells:
            rows.append(cells)

    if not rows:
        return []

    return _convert_table_to_markdown(rows)


def _extract_cell_content(cell):
    """提取单元格内容"""
    cell_text = []
    for p in cell.findall('.//w:p', NS):
        text = _extract_paragraph_text(p)
        if text.strip():
            cell_text.append(text.strip())
    return ' '.join(cell_text)


def _convert_table_to_markdown(rows):
    """将表格转换为 Markdown"""
    max_cols = max(len(row) for row in rows)
    normalized_rows = [row + [''] * (max_cols - len(row)) for row in rows]

    markdown = []
    markdown.append('| ' + ' | '.join(normalized_rows[0]) + ' |')
    markdown.append('|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|')

    for row in normalized_rows[1:]:
        markdown.append('| ' + ' | '.join(cell or ' ' for cell in row) + ' |')

    markdown.append('')
    return markdown


