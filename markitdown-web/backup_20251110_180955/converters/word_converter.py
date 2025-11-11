#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import zipfile
import xml.etree.ElementTree as ET
import re
import os
import subprocess
import platform


def word_converter(file_path):
    """将Word文档转换为Markdown格式字符串"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()

    if file_ext == '.docx':
        converter = DocxToMarkdownConverter()
        return converter.convert(file_path)
    elif file_ext == '.doc':
        converter = DocToMarkdownConverter()
        return converter.convert(file_path)
    else:
        raise ValueError("目前只支持 .doc 和 .docx 格式文件")


class DocToMarkdownConverter:
    """DOC文件转Markdown转换器"""

    def __init__(self):
        self.platform = platform.system().lower()

    def convert(self, file_path):
        """转换DOC文件"""
        try:
            text_content = self._extract_text_from_doc(file_path)

            if not text_content.strip():
                return "# 转换错误\n\n无法从DOC文件中提取文本内容"

            return self._text_to_markdown(text_content)

        except Exception as e:
            return f"# 转换错误\n\n在处理DOC文件 `{file_path}` 时发生错误: {str(e)}"

    def _extract_text_from_doc(self, file_path):
        """从DOC文件中提取文本"""
        if self.platform == 'windows':
            result = self._extract_with_com(file_path)
            if result:
                return result

        if self.platform == 'linux':
            result = self._extract_with_antiword(file_path)
            if result:
                return result

        result = self._extract_with_oletools(file_path)
        if result:
            return result

        return self._extract_simple_text(file_path)

    def _extract_with_com(self, file_path):
        """使用Windows COM接口读取DOC文件"""
        try:
            import win32com.client

            word = win32com.client.Dispatch("Word.Application")
            word.Visible = False

            doc = word.Documents.Open(os.path.abspath(file_path))
            text = doc.Content.Text
            doc.Close()
            word.Quit()

            # 检查是否是RTF格式并清理
            if text and text.startswith('{\\rtf1'):
                return self._clean_rtf_content(text)
            return text

        except ImportError:
            return None
        except Exception:
            return None

    def _extract_with_antiword(self, file_path):
        """使用antiword命令行工具读取DOC文件"""
        try:
            subprocess.run(['which', 'antiword'], check=True, capture_output=True)

            result = subprocess.run(
                ['antiword', file_path],
                capture_output=True,
                text=True,
                encoding='utf-8'
            )

            if result.returncode == 0:
                content = result.stdout
                # 检查是否是RTF格式并清理
                if content.startswith('{\\rtf1'):
                    return self._clean_rtf_content(content)
                return content

            return None

        except (subprocess.CalledProcessError, FileNotFoundError):
            return None

    def _extract_with_oletools(self, file_path):
        """使用oletools提取DOC文件文本"""
        try:
            try:
                from oletools import oleobj
                from oletools.olefile import OleFileIO
            except ImportError:
                return None

            ole = OleFileIO(file_path)

            if ole.exists('WordDocument'):
                data = ole.get_type('WordDocument')
                ole.close()
                return self._simple_ole_text_extraction(file_path)

            ole.close()
            return None

        except Exception:
            return None

    def _clean_rtf_content(self, rtf_text):
        """清理RTF格式内容，提取纯文本"""
        try:
            if not rtf_text or not rtf_text.startswith('{\\rtf1'):
                return rtf_text

            # 方法1: 尝试使用外部RTF库
            result = self._try_external_rtf_parser(rtf_text)
            if result:
                return result

            # 方法2: 使用智能解析方法
            result = self._parse_rtf_intelligently(rtf_text)
            if result and len(result.strip()) > 10:
                return result

            # 方法3: 使用简单清理方法
            result = self._simple_rtf_clean(rtf_text)
            if result and len(result.strip()) > 10:
                return result

            # 如果所有方法都失败，返回建议
            return ("注意：此DOC文件包含复杂的RTF格式，提取效果可能不理想。\n\n"
                   "建议操作：\n"
                   "1. 在Microsoft Word中打开此文件\n"
                   "2. 选择'文件' -> '另存为'\n"
                   "3. 选择文件格式为：\n"
                   "   - 纯文本(.txt) - 推荐用于纯文本内容\n"
                   "   - Word文档(.docx) - 推荐用于保留格式\n"
                   "4. 重新保存后上传转换\n\n"
                   f"原始预览（前200字符）：{rtf_text[:200] if rtf_text else '无内容'}...")

        except Exception as e:
            return f"RTF格式处理遇到问题: {str(e)}\n\n建议将文件转换为TXT或DOCX格式后重试。"

    def _try_external_rtf_parser(self, rtf_text):
        """尝试使用外部RTF解析库"""
        try:
            # 尝试使用striprtf库
            import striprtf
            text = striprtf.rtf_to_text(rtf_text)
            if text and len(text.strip()) > 5:
                return self._post_process_rtf_text(text)
        except ImportError:
            pass
        except Exception:
            pass

        try:
            # 尝试使用其他方法，这里可以添加更多RTF解析库
            pass
        except Exception:
            pass

        return None

    def _post_process_rtf_text(self, text):
        """后处理RTF解析结果"""
        if not text:
            return text

        # 清理多余的空行
        text = re.sub(r'\n\s*\n\s*\n', '\n\n', text)

        # 移除页码等数字
        lines = text.split('\n')
        cleaned_lines = []

        for line in lines:
            line = line.strip()
            # 跳过纯数字行（可能是页码）
            if line and not line.isdigit():
                # 跳过过短的垃圾行
                if len(line) > 2 or line in ('•', '-', '*'):
                    cleaned_lines.append(line)

        return '\n'.join(cleaned_lines)

    def _parse_rtf_intelligently(self, rtf_text):
        """智能解析RTF内容，更好地提取文本结构"""
        try:
            text = rtf_text
            result = []
            current_paragraph = ""
            in_skip_block = False

            i = 0
            while i < len(text):
                # 检测控制字符
                if text[i] == '\\':
                    # 查找控制命令的结束位置
                    j = i + 1
                    while j < len(text) and (text[j].isalpha() or text[j].isdigit()):
                        j += 1

                    if j > i + 1:
                        command = text[i+1:j]

                        # 处理重要的命令
                        if command == 'par':
                            # 段落结束
                            if current_paragraph.strip():
                                result.append(current_paragraph.strip())
                            current_paragraph = ""
                            i = j
                        elif command == 'line':
                            # 强制换行
                            current_paragraph += "\n"
                            i = j
                        elif command == 'tab':
                            current_paragraph += "\t"
                            i = j
                        elif command == 'bullet':
                            current_paragraph += "• "
                            i = j
                        elif command.startswith('u'):
                            # Unicode字符处理
                            try:
                                unicode_match = re.search(r'u(\d+)', command)
                                if unicode_match:
                                    code_point = int(unicode_match.group(1))
                                    char = self._safe_unicode_convert(code_point)
                                    if char:
                                        current_paragraph += char
                            except:
                                pass
                            i = j + 1  # 跳过?或空格
                        elif command in ['pard', 'plain']:
                            # 重置格式，开始新段落
                            if current_paragraph.strip():
                                result.append(current_paragraph.strip())
                            current_paragraph = ""
                            i = j
                        elif command in ['header', 'footer']:
                            # 跳过页眉页脚内容
                            in_skip_block = True
                            i = j
                        elif command in ['ql', 'qr', 'qc', 'qj']:
                            # 对齐命令，跳过
                            i = j
                        elif command in ['fs', 'f', 'cf', 'highlight']:
                            # 字体大小、字体、颜色等格式命令，跳过
                            # 处理可能的数字参数
                            while j < len(text) and (text[j].isdigit() or text[j].isspace()):
                                j += 1
                            i = j
                        else:
                            # 跳过其他格式命令
                            # 处理可能的数字参数
                            while j < len(text) and (text[j].isdigit() or text[j].isspace()):
                                j += 1
                            i = j
                    else:
                        i += 1

                elif text[i] == '{':
                    # 检查是否是需要跳过的块
                    lookahead = i + 1
                    while lookahead < len(text) and text[lookahead] != '\\':
                        lookahead += 1

                    if lookahead < len(text):
                        remaining = text[lookahead:lookahead+10]
                        if any(skip in remaining for skip in ['\\header', '\\footer', '\\fonttbl', '\\colortbl']):
                            # 跳过这个块
                            brace_count = 1
                            k = i + 1
                            while k < len(text) and brace_count > 0:
                                if text[k] == '{':
                                    brace_count += 1
                                elif text[k] == '}':
                                    brace_count -= 1
                                k += 1
                            i = k
                        else:
                            i += 1
                    else:
                        i += 1

                elif text[i] == '}':
                    # 块结束，可能结束跳过模式
                    if in_skip_block:
                        in_skip_block = False
                    i += 1

                elif text[i] == '\n' or text[i] == '\r':
                    # 保留原始换行
                    current_paragraph += text[i]
                    i += 1

                else:
                    # 普通字符
                    if not in_skip_block and ord(text[i]) >= 32:
                        current_paragraph += text[i]
                    i += 1

            # 添加最后一个段落
            if current_paragraph.strip():
                result.append(current_paragraph.strip())

            # 后处理结果
            cleaned_result = []
            for paragraph in result:
                # 清理多余空格
                paragraph = re.sub(r'\s+', ' ', paragraph)
                # 移除明显的垃圾内容
                if len(paragraph) > 1 or paragraph.strip() in ['•', '-', '*']:
                    cleaned_result.append(paragraph.strip())

            # 重新组织段落结构
            final_result = []
            for i, para in enumerate(cleaned_result):
                final_result.append(para)

                # 如果当前段很短且下一段也很长，可能需要换行
                if i < len(cleaned_result) - 1:
                    current_len = len(para)
                    next_len = len(cleaned_result[i + 1])

                    # 如果当前段很短（如标题）且下一段很长，添加空行
                    if current_len < 50 and next_len > 100:
                        final_result.append('')

            final_text = '\n'.join(final_result)

            # 最终清理
            final_text = re.sub(r'\n{3,}', '\n\n', final_text)
            final_text = final_text.strip()

            return final_text

        except Exception:
            return None

    def _simple_rtf_clean(self, rtf_text):
        """简单RTF清理方法（备用）"""
        try:
            # 移除大块的控制信息
            text = rtf_text

            # 移除所有大括号块
            text = re.sub(r'\{[^{}]*\}', ' ', text)

            # 处理常见的RTF命令
            replacements = {
                r'\\par\b': '\n',
                r'\\line\b': '\n',
                r'\\tab\b': '\t',
                r'\\bullet\b': '• ',
                r'\\u(\d+)[\?\s]': lambda m: self._safe_unicode_convert(int(m.group(1))),
                r'\\[a-zA-Z]+\d*': '',
                r'[{}\\]': '',
                r'\s+': ' ',
                r'\n\s*\n': '\n\n'
            }

            for pattern, repl in replacements.items():
                text = re.sub(pattern, repl, text)

            return text.strip()

        except Exception:
            return ""

    def _safe_unicode_convert(self, code_point):
        """安全地将Unicode码点转换为字符"""
        try:
            char = chr(code_point)
            # 过滤掉一些控制字符和特殊字符
            if ord(char) >= 32 or char in '\n\t\r':
                return char
            else:
                return ''
        except (ValueError, OverflowError):
            # 如果Unicode码点无效，返回空字符串
            return ''

    def _extract_simple_text(self, file_path):
        """简单的文本提取"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            text_pattern = rb'[\x20-\x7E\x0A\x0D]{4,}'
            matches = re.findall(text_pattern, content)

            if matches:
                text = b'\n'.join(matches)
                try:
                    decoded_text = text.decode('utf-8', errors='ignore')
                    # 检查是否是RTF格式
                    if decoded_text.startswith('{\\rtf1'):
                        return self._clean_rtf_content(decoded_text)
                    return decoded_text
                except:
                    decoded_text = text.decode('gbk', errors='ignore')
                    if decoded_text.startswith('{\\rtf1'):
                        return self._clean_rtf_content(decoded_text)
                    return decoded_text

            return None

        except Exception:
            return None

    def _simple_ole_text_extraction(self, file_path):
        """简单的OLE文本提取"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            # 首先尝试将整个内容解码为文本，检查是否是RTF
            try:
                text_content = content.decode('utf-8', errors='ignore')
                if text_content.startswith('{\\rtf1'):
                    return self._clean_rtf_content(text_content)
            except:
                pass

            # 如果不是RTF，使用原有的二进制提取方法
            text_content = []
            in_text = False
            current_text = []

            for byte_val in content:
                if 32 <= byte_val <= 126:
                    current_text.append(chr(byte_val))
                    in_text = True
                elif in_text:
                    if len(current_text) >= 4:
                        text_content.append(''.join(current_text))
                    current_text = []
                    in_text = False

            if len(current_text) >= 4:
                text_content.append(''.join(current_text))

            result = '\n'.join(text_content)

            # 再次检查结果是否是RTF
            if result.startswith('{\\rtf1'):
                return self._clean_rtf_content(result)

            return result

        except Exception:
            return None

    def _text_to_markdown(self, text):
        """将纯文本转换为Markdown格式"""
        lines = text.split('\n')
        markdown_lines = []

        i = 0
        while i < len(lines):
            line = lines[i].strip()

            if not line:
                markdown_lines.append('')
                i += 1
                continue

            if (len(line) < 50 and
                line.isupper() and
                i + 1 < len(lines) and
                not lines[i + 1].strip()):
                markdown_lines.append(f"## {line}")
                i += 1
            elif len(line) < 30 and not line.endswith('.') and line.endswith(':'):
                markdown_lines.append(f"### {line}")
            elif (len(line) < 20 and
                  line.strip() and
                  (i == 0 or not lines[i-1].strip())):
                markdown_lines.append(f"# {line}")
            else:
                if line.startswith(('•', '-', '*', '·')):
                    markdown_lines.append(f"- {line[1:].strip()}")
                elif line[0].isdigit() and ('.' in line or ')' in line):
                    markdown_lines.append(f"1. {line}")
                else:
                    markdown_lines.append(line)

            i += 1

        result_lines = []
        prev_empty = False

        for line in markdown_lines:
            if line.strip():
                result_lines.append(line)
                prev_empty = False
            elif not prev_empty:
                result_lines.append('')
                prev_empty = True

        return '\n'.join(result_lines)


class DocxToMarkdownConverter:
    """DOCX文档转Markdown转换器"""

    def __init__(self):
        self.ns = {
            'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
            'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
            'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
            'pic': 'http://schemas.openxmlformats.org/drawingml/2006/picture'
        }

        self.markdown_lines = []
        self.image_counter = 0
        self.extracted_images = []

    def convert(self, file_path):
        """主转换方法"""
        self.markdown_lines = []
        self.image_counter = 0
        self.extracted_images = []

        try:
            content = self._parse_docx_content(file_path)
            self._process_content(content)

            if self.extracted_images:
                self._add_image_section()

            return '\n'.join(self.markdown_lines)

        except Exception as e:
            return f"# 转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _parse_docx_content(self, file_path):
        """解析docx文件内容"""
        with zipfile.ZipFile(file_path, 'r') as docx_zip:
            with docx_zip.open('word/document.xml') as xml_file:
                tree = ET.parse(xml_file)
                return tree.getroot()

    def _process_content(self, root):
        """处理文档内容"""
        paragraphs = root.findall('.//w:p', self.ns)
        tables = root.findall('.//w:tbl', self.ns)

        for paragraph in paragraphs:
            self._process_paragraph(paragraph)

        for table in tables:
            self._process_table(table)

    def _process_paragraph(self, paragraph):
        """处理段落"""
        style_info = self._get_paragraph_style(paragraph)
        text_content = self._extract_paragraph_text(paragraph)

        if not text_content.strip():
            self.markdown_lines.append('')
            return

        formatted_text = self._apply_paragraph_format(text_content, style_info)

        if formatted_text.strip():
            self.markdown_lines.append(formatted_text)

    def _get_paragraph_style(self, paragraph):
        """获取段落样式信息"""
        style = {
            'is_heading': False,
            'heading_level': 0,
            'is_list': False,
            'list_level': 0,
            'list_type': 'bullet',
            'alignment': 'left',
            'is_quote': False
        }

        p_style = paragraph.find('.//w:pStyle', self.ns)
        if p_style is not None:
            style_name = p_style.get(f'{{{self.ns["w"]}}}val', '')

            if 'Heading' in style_name:
                style['is_heading'] = True
                match = re.search(r'Heading(\d+)', style_name)
                if match:
                    style['heading_level'] = int(match.group(1))

            elif 'List' in style_name:
                style['is_list'] = True

        num_pr = paragraph.find('.//w:numPr', self.ns)
        if num_pr is not None:
            style['is_list'] = True
            ilvl = num_pr.find('.//w:ilvl', self.ns)
            if ilvl is not None:
                style['list_level'] = int(ilvl.get(f'{{{self.ns["w"]}}}val', 0))

            num_id = num_pr.find('.//w:numId', self.ns)
            if num_id is not None:
                style['list_type'] = 'number'

        jc = paragraph.find('.//w:jc', self.ns)
        if jc is not None:
            align = jc.get(f'{{{self.ns["w"]}}}val', 'left')
            if align in ['center', 'right', 'justify']:
                style['alignment'] = align

        return style

    def _extract_paragraph_text(self, paragraph):
        """提取段落文本内容和格式"""
        text_parts = []

        for run in paragraph.findall('.//w:r', self.ns):
            run_text = self._process_run(run)
            if run_text:
                text_parts.append(run_text)

        return ''.join(text_parts)

    def _process_run(self, run):
        """处理文本块，应用格式"""
        text = ''

        is_bold = run.find('.//w:b', self.ns) is not None
        is_italic = run.find('.//w:i', self.ns) is not None
        is_underline = run.find('.//w:u', self.ns) is not None
        is_strike = run.find('.//w:strike', self.ns) is not None

        for t_elem in run.findall('.//w:t', self.ns):
            if t_elem.text:
                text += t_elem.text

        if not text:
            return ''

        if is_strike:
            text = f'~~{text}~~'

        if is_underline:
            text = f"{text}"  # 标准Markdown无下划线语法，保留原文本

        if is_italic:
            text = f'*{text}*'

        if is_bold:
            text = f'**{text}**'

        return text

    def _apply_paragraph_format(self, text, style):
        """应用段落格式"""
        if style['is_heading'] and style['heading_level'] > 0:
            return f"{'#' * style['heading_level']} {text}"

        elif style['is_list']:
            indent = '  ' * style['list_level']
            if style['list_type'] == 'number':
                return f"{indent}1. {text}"
            else:
                return f"{indent}- {text}"

        elif style['is_quote']:
            lines = text.split('\n')
            return '\n'.join(f"> {line}" for line in lines)

        elif style['alignment'] == 'center':
            return f"{text}\n<!-- 居中对齐 -->"
        elif style['alignment'] == 'right':
            return f"{text}\n<!-- 右对齐 -->"
        elif style['alignment'] == 'justify':
            return f"{text}\n<!-- 两端对齐 -->"
        else:
            return text

    def _process_table(self, table):
        """处理表格"""
        rows = []

        for tr in table.findall('.//w:tr', self.ns):
            cells = []
            for tc in tr.findall('.//w:tc', self.ns):
                cell_text = self._extract_cell_content(tc)
                cells.append(cell_text.strip())

            if cells:
                rows.append(cells)

        if rows:
            self._convert_table_to_markdown(rows)

    def _extract_cell_content(self, cell):
        """提取单元格内容"""
        cell_text = []

        for p in cell.findall('.//w:p', self.ns):
            paragraph_text = self._extract_paragraph_text(p)
            if paragraph_text.strip():
                cell_text.append(paragraph_text.strip())

        return ' '.join(cell_text)

    def _convert_table_to_markdown(self, rows):
        """将表格数据转换为Markdown格式"""
        if not rows:
            return

        max_cols = max(len(row) for row in rows)
        normalized_rows = []

        for row in rows:
            normalized_row = row + [''] * (max_cols - len(row))
            normalized_rows.append(normalized_row)

        header = '| ' + ' | '.join(normalized_rows[0]) + ' |'
        self.markdown_lines.append(header)

        separator = '|' + '|'.join([' --- ' for _ in range(max_cols)]) + '|'
        self.markdown_lines.append(separator)

        for row in normalized_rows[1:]:
            data_row = '| ' + ' | '.join(cell or ' ' for cell in row) + ' |'
            self.markdown_lines.append(data_row)

        self.markdown_lines.append('')

    def _add_image_section(self):
        """添加图片信息部分"""
        if not self.extracted_images:
            return

        self.markdown_lines.append('')
        self.markdown_lines.append('---')
        self.markdown_lines.append('')
        self.markdown_lines.append('## 文档中的图片')
        self.markdown_lines.append('')

        for img in self.extracted_images:
            self.markdown_lines.append(f"![{img['filename']}](images/{img['filename']})")
            self.markdown_lines.append(f"*文件: {img['filename']}, 大小: {img['size']} 字节, 格式: {img['format']}*")
            self.markdown_lines.append('')


