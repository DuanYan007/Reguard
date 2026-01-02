from pathlib import Path
import re
# tested

def convert(source: Path) -> str:
    """将 HTML 转换为 Markdown"""
    with open(source, 'r', encoding='utf-8') as f:
        html_content = f.read()
    return _html_to_markdown(html_content)


def _html_to_markdown(html: str) -> str:
    """HTML 转 Markdown 核心逻辑"""

    # 预处理：移除不可见内容
    html = _preprocess(html)

    # 提取 body 内容（如果有）
    body_match = re.search(r'<body[^>]*>(.*?)</body>', html, re.DOTALL | re.IGNORECASE)
    if body_match:
        html = body_match.group(1)

    # 转换各类标签（注意顺序很重要）
    html = _convert_code_blocks(html)
    html = _convert_tables(html)
    html = _convert_links_and_images(html)
    html = _convert_lists(html)
    html = _convert_blockquote(html)
    html = _convert_headings(html)
    html = _convert_bold_italic(html)
    html = _convert_hr_br(html)

    # 清理和优化
    html = _cleanup(html)

    return html.strip()


def _preprocess(html: str) -> str:
    """预处理 HTML"""
    # 移除脚本、样式
    html = re.sub(r'<script[^>]*>.*?</script>', '', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<style[^>]*>.*?</style>', '', html, flags=re.DOTALL | re.IGNORECASE)

    # 移除注释、head、meta、link、base
    html = re.sub(r'<!--.*?-->', '', html, flags=re.DOTALL)
    html = re.sub(r'<head[^>]*>.*?</head>', '', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<meta[^>]*>', '', html, flags=re.IGNORECASE)
    html = re.sub(r'<link[^>]*>', '', html, flags=re.IGNORECASE)
    html = re.sub(r'<base[^>]*>', '', html, flags=re.IGNORECASE)

    # 移除 nav、footer、aside 等
    html = re.sub(r'<nav[^>]*>.*?</nav>', '', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<footer[^>]*>.*?</footer>', '', html, flags=re.DOTALL | re.IGNORECASE)

    return html


def _convert_code_blocks(html: str) -> str:
    """转换代码块"""
    # 先处理多行代码块 <pre><code>...</code></pre>
    def replace_pre_code(match):
        code = match.group(0)
        # 移除标签
        code = re.sub(r'</?code[^>]*>', '', code)
        code = re.sub(r'</?pre[^>]*>', '', code)
        # 解码 HTML 实体并保留缩进
        code = _unescape_html(code)
        return f'\n```\n{code}\n```\n'

    html = re.sub(r'<pre[^>]*>.*?</pre>', replace_pre_code, html, flags=re.DOTALL)

    # 行内代码 <code>...</code>
    html = re.sub(r'<code[^>]*>(.*?)</code>', r'`\1`', html, flags=re.DOTALL)

    # <kbd> 标签
    html = re.sub(r'<kbd[^>]*>(.*?)</kbd>', r'`\1`', html, flags=re.DOTALL)

    return html


def _convert_headings(html: str) -> str:
    """转换标题"""
    def replace_heading(match):
        # 提取标题级别
        tag = match.group(0)
        for i in range(1, 7):
            if f'<h{i}' in tag.lower():
                content = match.group(1)
                # 清理内容：解码HTML实体并去除多余空白
                content = _unescape_and_strip(content)
                return f'\n\n{"#" * i} {content}\n\n'
        return match.group(0)

    for i in range(6, 0, -1):
        html = re.sub(f'<h{i}[^>]*>(.*?)</h{i}>', replace_heading, html, flags=re.DOTALL | re.IGNORECASE)
    return html


def _convert_bold_italic(html: str) -> str:
    """转换粗体和斜体"""
    # 粗体
    html = re.sub(r'<strong[^>]*>(.*?)</strong>', r'**\1**', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<b[^>]*>(.*?)</b>', r'**\1**', html, flags=re.DOTALL | re.IGNORECASE)

    # 斜体
    html = re.sub(r'<em[^>]*>(.*?)</em>', r'*\1*', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<i[^>]*>(.*?)</i>', r'*\1*', html, flags=re.DOTALL | re.IGNORECASE)

    # 删除线
    html = re.sub(r'<s[^>]*>(.*?)</s>', r'~~\1~~', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<del[^>]*>(.*?)</del>', r'~~\1~~', html, flags=re.DOTALL | re.IGNORECASE)
    html = re.sub(r'<strike[^>]*>(.*?)</strike>', r'~~\1~~', html, flags=re.DOTALL | re.IGNORECASE)

    return html


def _convert_links_and_images(html: str) -> str:
    """转换链接和图片"""
    # 图片 <img>
    html = re.sub(
        r'<img[^>]*src=["\']([^"\']*)["\'][^>]*alt=["\']([^"\']*)["\'][^>]*>',
        r'![\2](\1)',
        html,
        flags=re.IGNORECASE
    )
    html = re.sub(
        r'<img[^>]*alt=["\']([^"\']*)["\'][^>]*src=["\']([^"\']*)["\'][^>]*>',
        r'![\1](\2)',
        html,
        flags=re.IGNORECASE
    )
    html = re.sub(
        r'<img[^>]*src=["\']([^"\']*)["\'][^>]*>',
        r'![](\1)',
        html,
        flags=re.IGNORECASE
    )

    # 链接 <a>
    html = re.sub(
        r'<a[^>]*href=["\']([^"\']*)["\'][^>]*>(.*?)</a>',
        r'[\2](\1)',
        html,
        flags=re.DOTALL | re.IGNORECASE
    )

    return html


def _convert_lists(html: str) -> str:
    """转换列表"""
    # 有序列表
    def replace_ol(match):
        content = match.group(1)
        items = re.findall(r'<li[^>]*>(.*?)</li>', content, flags=re.DOTALL | re.IGNORECASE)
        result = []
        for item in items:
            item = _unescape_and_strip(item)
            if item:
                result.append(f'1. {item}')
        return '\n'.join(result)

    html = re.sub(r'<ol[^>]*>(.*?)</ol>', replace_ol, html, flags=re.DOTALL | re.IGNORECASE)

    # 无序列表
    def replace_ul(match):
        content = match.group(1)
        items = re.findall(r'<li[^>]*>(.*?)</li>', content, flags=re.DOTALL | re.IGNORECASE)
        result = []
        for item in items:
            item = _unescape_and_strip(item)
            if item:
                result.append(f'- {item}')
        return '\n'.join(result)

    html = re.sub(r'<ul[^>]*>(.*?)</ul>', replace_ul, html, flags=re.DOTALL | re.IGNORECASE)

    return html


def _convert_blockquote(html: str) -> str:
    """转换引用"""
    def replace_blockquote(match):
        content = match.group(1)
        # 移除内部的 <p> 标签
        content = re.sub(r'</?p[^>]*>', '\n', content)
        lines = content.split('\n')
        quoted = []
        for line in lines:
            line = line.strip()
            if line:
                quoted.append(f'> {line}')
        return '\n'.join(quoted) + '\n'

    html = re.sub(r'<blockquote[^>]*>(.*?)</blockquote>', replace_blockquote, html, flags=re.DOTALL | re.IGNORECASE)
    return html


def _convert_tables(html: str) -> str:
    """转换表格"""
    def replace_table(match):
        table_html = match.group(0)

        # 提取所有行
        rows = re.findall(r'<tr[^>]*>(.*?)</tr>', table_html, flags=re.DOTALL | re.IGNORECASE)

        if not rows:
            return '\n'

        # 处理每一行
        md_rows = []
        for row in rows:
            cells = re.findall(r'<t[dh][^>]*>(.*?)</t[dh]>', row, flags=re.DOTALL | re.IGNORECASE)
            if cells:
                cell_texts = [_unescape_and_strip(cell) for cell in cells]
                md_rows.append(cell_texts)

        if not md_rows:
            return '\n'

        # 转换为 Markdown 表格
        max_cols = max(len(row) for row in md_rows)
        md_rows = [row + [''] * (max_cols - len(row)) for row in md_rows]

        # 表头
        header = '| ' + ' | '.join(md_rows[0]) + ' |'
        separator = '|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|'

        # 数据行
        data_rows = []
        for row in md_rows[1:]:
            data_rows.append('| ' + ' | '.join(cell or ' ' for cell in row) + ' |')

        return '\n'.join(['', header, separator] + data_rows) + '\n'

    html = re.sub(r'<table[^>]*>.*?</table>', replace_table, html, flags=re.DOTALL | re.IGNORECASE)
    return html


def _convert_hr_br(html: str) -> str:
    """转换水平线和换行"""
    # 水平线
    html = re.sub(r'<hr[^>]*/?>', '\n\n---\n\n', html, flags=re.IGNORECASE)

    # 换行
    html = re.sub(r'<br\s*/?>', '\n', html, flags=re.IGNORECASE)

    return html


def _cleanup(html: str) -> str:
    """清理和优化"""
    # 处理段落和 div
    html = re.sub(r'<p[^>]*>', '\n\n', html, flags=re.IGNORECASE)
    html = re.sub(r'</p>', '\n\n', html, flags=re.IGNORECASE)
    html = re.sub(r'<div[^>]*>', '\n', html, flags=re.IGNORECASE)
    html = re.sub(r'</div>', '\n', html, flags=re.IGNORECASE)

    # 移除其他容器标签但保留内容
    html = re.sub(r'</?(?:header|main|section|article|aside|aside)[^>]*>', '\n', html, flags=re.IGNORECASE)
    html = re.sub(r'</?(?:details|summary)[^>]*>', '\n', html, flags=re.IGNORECASE)

    # HTML 实体解码
    html = _unescape_html(html)

    # 移除多余空行
    html = re.sub(r'\n{4,}', '\n\n', html)

    # 清理每行首尾空格
    lines = []
    for line in html.split('\n'):
        line = line.strip()
        if line:
            lines.append(line)
        elif lines and lines[-1] != '':
            lines.append('')

    return '\n'.join(lines)


def _unescape_and_strip(text: str) -> str:
    """解码 HTML 实体并清理"""
    text = _unescape_html(text)
    text = re.sub(r'\s+', ' ', text)
    return text.strip()


def _unescape_html(text: str) -> str:
    """HTML 实体解码"""
    # 常见命名实体
    entities = {
        '&nbsp;': ' ', '&lt;': '<', '&gt;': '>', '&amp;': '&',
        '&quot;': '"', '&apos;': "'", '&copy;': '©', '&reg;': '®',
        '&trade;': '™', '&euro;': '€', '&pound;': '£', '&yen;': '¥',
        '&cent;': '¢', '&hellip;': '...', '&mdash;': '—', '&ndash;': '–',
        '&plusmn;': '±', '&times;': '×', '&divide;': '÷',
        '&le;': '≤', '&ge;': '≥', '&ne;': '≠', '&pm;': '±',
    }

    # 替换命名实体
    for entity, char in entities.items():
        text = text.replace(entity, char)

    # 替换数字实体 &#123; 和 &#x1F600;
    def replace_numeric(match):
        num = match.group(1)
        try:
            if num.startswith('x'):
                return chr(int(num[1:], 16))
            else:
                return chr(int(num))
        except (ValueError, OverflowError):
            return match.group(0)

    text = re.sub(r'&#(\d+);', replace_numeric, text)
    text = re.sub(r'&#x([0-9a-fA-F]+);', replace_numeric, text)

    return text

