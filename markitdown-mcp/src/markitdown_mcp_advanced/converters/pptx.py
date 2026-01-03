from pathlib import Path
import zipfile
import xml.etree.ElementTree as ET
from typing import List
# tested

def convert(source: Path) -> str:
    """将 PowerPoint (.pptx) 转换为 Markdown"""
    try:
        with zipfile.ZipFile(source, 'r') as zip_ref:
            # 解析所有幻灯片
            slides = _parse_slides(zip_ref)

            if not slides:
                return "# 空演示文稿\n\n此 PowerPoint 文件没有内容"

            # 生成 Markdown
            return _format_slides(slides)

    except Exception as e:
        return f"# 转换错误\n\n{str(e)}"


def _parse_slides(zip_ref: zipfile.ZipFile) -> List[dict]:
    """解析所有幻灯片"""
    slides = []

    # 查找所有幻灯片文件（ppt/slides/slide1.xml, slide2.xml, ...）
    # 使用正则表达式匹配 slide数字
    import re
    slide_files = [
        name for name in zip_ref.namelist()
        if re.match(r'ppt/slides/slide\d+\.xml$', name)
    ]

    # 按数字排序
    slide_files.sort(key=lambda x: int(re.search(r'slide(\d+)\.xml$', x).group(1)))

    for slide_file in slide_files:
        with zip_ref.open(slide_file) as f:
            tree = ET.parse(f)
            root = tree.getroot()

            # 提取幻灯片内容
            slide_content = _extract_slide_content(root, zip_ref)
            slides.append(slide_content)

    return slides


def _extract_slide_content(slide_elem: ET.Element, zip_ref: zipfile.ZipFile) -> dict:
    """从幻灯片 XML 中提取内容"""
    # 命名空间
    ns = {
        'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
        'p': 'http://schemas.openxmlformats.org/presentationml/2006/main',
    }

    content = {
        'title': '',
        'text': [],
        'images': [],
        'tables': [],
    }

    # 查找所有形状（shapes）
    shapes = slide_elem.findall('.//p:sp', ns)

    for shape in shapes:
        # 提取文本
        text_boxes = shape.findall('.//a:t', ns)
        for text_box in text_boxes:
            if text_box.text:
                content['text'].append(text_box.text)

        # 检查是否是标题（通常是第一个文本框）
        if not content['title'] and text_boxes and text_boxes[0].text:
            # 简单判断：文本较短且独立
            text = text_boxes[0].text.strip()
            if len(text) < 100 and text:
                content['title'] = text

    # 提取图片
    pictures = slide_elem.findall('.//p:pic', ns)
    for pic in pictures:
        blip = pic.find('.//a:blip', ns)
        if blip is not None:
            embed = blip.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed')
            if embed:
                # 图片信息（实际图片需要从关系文件中获取）
                content['images'].append('[Image]')

    # 提取表格
    tables = slide_elem.findall('.//a:tbl', ns)
    for table in tables:
        table_data = _extract_table(table, ns)
        if table_data:
            content['tables'].append(table_data)

    return content


def _extract_table(table_elem: ET.Element, ns: dict) -> List[List[str]]:
    """从表格 XML 中提取数据"""
    rows = table_elem.findall('.//a:tr', ns)

    table_data = []
    for row in rows:
        cells = row.findall('.//a:tc', ns)
        row_data = []
        for cell in cells:
            # 获取单元格文本
            text_elem = cell.find('.//a:t', ns)
            text = text_elem.text if text_elem is not None else ''
            row_data.append(text.strip())
        table_data.append(row_data)

    return table_data


def _format_slides(slides: List[dict]) -> str:
    """将幻灯片内容格式化为 Markdown"""
    markdown_parts = []

    for idx, slide in enumerate(slides, 1):
        # 幻灯片分隔符
        if idx > 1:
            markdown_parts.append('\n---\n')

        # 标题
        if slide['title']:
            markdown_parts.append(f"## {slide['title']}\n")
        else:
            markdown_parts.append(f"## Slide {idx}\n")

        # 文本内容
        if slide['text']:
            # 过滤掉已作为标题的文本
            texts = [t for t in slide['text'] if t != slide['title']]
            for text in texts:
                text = text.strip()
                if text:
                    markdown_parts.append(f"{text}\n")

        # 表格
        for table in slide['tables']:
            markdown_parts.append(_format_table(table))

        # 图片占位符
        for img in slide['images']:
            markdown_parts.append(f"\n{img}\n")

    return '\n'.join(markdown_parts).strip()


def _format_table(table_data: List[List[str]]) -> str:
    """将表格数据格式化为 Markdown"""
    if not table_data:
        return ''

    max_cols = max(len(row) for row in table_data)

    # 填充空单元格
    padded_table = []
    for row in table_data:
        padded_row = row + [''] * (max_cols - len(row))
        padded_table.append(padded_row)

    # 生成 Markdown 表格
    lines = []

    # 表头
    if padded_table:
        header = '| ' + ' | '.join(padded_table[0]) + ' |'
        separator = '|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|'
        lines.append(header)
        lines.append(separator)

    # 数据行
    for row in padded_table[1:]:
        row_text = '| ' + ' | '.join(cell or ' ' for cell in row) + ' |'
        lines.append(row_text)

    return '\n'.join(lines) + '\n'
