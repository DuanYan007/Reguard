#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import struct
import zipfile
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path


def ppt_native_converter(file_path):
    """将PPT/PPTX文档转换为Markdown格式字符串（仅使用Python原生库）"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()
    if file_ext not in ['.ppt', '.pptx']:
        raise ValueError("目前只支持 .ppt 和 .pptx 格式文件")

    converter = NativePptToMarkdownConverter()
    return converter.convert(file_path)


class NativePptToMarkdownConverter:
    """原生PPT文件转Markdown转换器（仅使用Python标准库）"""

    def __init__(self):
        self.markdown_lines = []

    def convert(self, file_path):
        """主转换方法"""
        self.markdown_lines = []

        try:
            file_ext = os.path.splitext(file_path)[1].lower()

            if file_ext == '.pptx':
                content = self._convert_pptx(file_path)
            elif file_ext == '.ppt':
                content = self._convert_ppt(file_path)
            else:
                return "# 转换错误\n\n不支持的文件格式"

            if not content:
                return """# PPT转换结果

PowerPoint文件中未检测到可提取的内容。

注意：此转换器使用Python原生库，功能有限。
对于复杂格式的PPT，建议：

1. 使用包含可提取文字的PPT/PPTX文件
2. 尝试将PPT另存为文本格式
3. 使用专业PPT转换工具

支持的提取内容：
- PPTX文件中的文本内容
- 基本标题和段落结构
- 简单列表内容"""

            # 格式化输出
            return self._format_result(content, file_path)

        except Exception as e:
            return f"# 转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _convert_pptx(self, file_path):
        """转换PPTX文件"""
        try:
            with zipfile.ZipFile(file_path, 'r') as pptx_zip:
                # 提取幻灯片内容
                slides_content = self._extract_pptx_slides(pptx_zip)

                if slides_content:
                    return "\n\n".join(slides_content)
                else:
                    return None

        except Exception as e:
            print(f"[警告] PPTX转换失败: {str(e)}")
            return None

    def _extract_pptx_slides(self, pptx_zip):
        """从PPTX文件中提取幻灯片内容"""
        slides = []

        try:
            # 查找所有幻灯片文件
            slide_files = [f for f in pptx_zip.namelist()
                          if f.startswith('ppt/slides/slide') and f.endswith('.xml')]

            for slide_file in sorted(slide_files):
                try:
                    with pptx_zip.open(slide_file) as slide_xml:
                        content = self._parse_slide_xml(slide_xml)
                        if content:
                            slides.append(content)
                except Exception as e:
                    print(f"[警告] 处理幻灯片 {slide_file} 失败: {str(e)}")
                    continue

        except Exception as e:
            print(f"[警告] 提取幻灯片列表失败: {str(e)}")

        return slides

    def _parse_slide_xml(self, slide_xml):
        """解析幻灯片XML内容"""
        try:
            tree = ET.parse(slide_xml)
            root = tree.getroot()

            # 定义命名空间
            namespaces = {
                'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
                'p': 'http://schemas.openxmlformats.org/presentationml/2006/main'
            }

            content_parts = []
            current_text = []

            # 查找所有文本块
            for text_element in root.findall('.//a:t', namespaces):
                if text_element.text:
                    current_text.append(text_element.text)

            # 查找所有段落
            for paragraph in root.findall('.//a:p', namespaces):
                paragraph_text = self._extract_paragraph_text(paragraph, namespaces)
                if paragraph_text:
                    content_parts.append(paragraph_text)

            # 如果找到内容，格式化它
            if content_parts:
                return "\n\n".join(content_parts)
            elif current_text:
                return " ".join(current_text)

        except Exception as e:
            print(f"[警告] 解析幻灯片XML失败: {str(e)}")

        return None

    def _extract_paragraph_text(self, paragraph, namespaces):
        """从段落元素中提取文本"""
        try:
            text_parts = []

            for text_run in paragraph.findall('.//a:r', namespaces):
                run_text = []

                # 检查文本内容
                for text_element in text_run.findall('.//a:t', namespaces):
                    if text_element.text:
                        run_text.append(text_element.text)

                if run_text:
                    # 检查格式（粗体、斜体等）
                    is_bold = text_run.find('.//a:b', namespaces) is not None
                    is_italic = text_run.find('.//a:i', namespaces) is not None

                    combined_text = "".join(run_text)
                    if is_bold:
                        combined_text = f"**{combined_text}**"
                    if is_italic:
                        combined_text = f"*{combined_text}*"

                    text_parts.append(combined_text)

            return " ".join(text_parts) if text_parts else None

        except Exception:
            return None

    def _convert_ppt(self, file_path):
        """转换PPT文件（二进制格式）"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            # 方法1: 提取OLE对象中的文本
            text = self._extract_text_from_ppt_ole(content)

            if text and len(text.strip()) > 10:
                return text

            # 方法2: 查找可打印文本
            text = self._extract_printable_text_from_ppt(content)

            if text and len(text.strip()) > 10:
                return text

        except Exception as e:
            print(f"[警告] PPT转换失败: {str(e)}")

        return None

    def _extract_text_from_ppt_ole(self, content):
        """从PPT文件中提取OLE对象文本"""
        try:
            # 查找OLE对象头部
            ole_pattern = rb'\xD0\xCF\x11\xE0\xA1\xB1\x1A\xE1'
            ole_matches = list(re.finditer(ole_pattern, content))

            if ole_matches:
                text_parts = []

                for match in ole_matches:
                    start = match.start()
                    # 提取OLE对象区域
                    ole_region = content[start:start+50000]  # 取前50KB

                    # 查找可打印文本
                    text = self._extract_printable_text(ole_region)
                    if text:
                        text_parts.append(text)

                if text_parts:
                    return "\n\n".join(text_parts)

        except Exception:
            pass

        return None

    def _extract_printable_text_from_ppt(self, content):
        """从PPT二进制内容中提取可打印文本"""
        try:
            # 查找连续的可打印字符序列
            printable_pattern = rb'[\x20-\x7E\x09\x0A\x0D]{5,}'
            matches = re.findall(printable_pattern, content)

            if matches:
                text_parts = []

                for match in matches:
                    try:
                        # 尝试UTF-8解码
                        text = match.decode('utf-8', errors='ignore')
                        if self._is_meaningful_text(text):
                            text_parts.append(text)
                    except:
                        # 尝试Latin-1解码
                        try:
                            text = match.decode('latin-1', errors='ignore')
                            if self._is_meaningful_text(text):
                                text_parts.append(text)
                        except:
                            continue

                if text_parts:
                    return " ".join(text_parts)

        except Exception:
            pass

        return None

    def _extract_printable_text(self, content):
        """提取可打印文本"""
        try:
            # 移除二进制控制字符
            clean_content = re.sub(rb'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\xFF]', b' ', content)

            # 查找连续的文本
            text_pattern = rb'[A-Za-z0-9\u4e00-\u9fff\s.,;:!?()\[\]{}"\'-]{5,}'
            matches = re.findall(text_pattern, clean_content)

            if matches:
                decoded_texts = []
                for match in matches:
                    try:
                        text = match.decode('utf-8', errors='ignore')
                        if self._is_meaningful_text(text):
                            decoded_texts.append(text)
                    except:
                        try:
                            text = match.decode('latin-1', errors='ignore')
                            if self._is_meaningful_text(text):
                                decoded_texts.append(text)
                        except:
                            continue

                if decoded_texts:
                    return " ".join(decoded_texts)

        except Exception:
            pass

        return None

    def _is_meaningful_text(self, text):
        """判断文本是否有意义"""
        if not text or len(text.strip()) < 2:
            return False

        text = text.strip()

        # 排除纯数字
        if text.isdigit():
            return False

        # 排除纯符号
        if not any(c.isalnum() for c in text):
            return False

        # 检查是否包含足够的字母或中文
        letters = sum(1 for c in text if c.isalpha())
        chinese = sum(1 for c in text if '\u4e00' <= c <= '\u9fff')

        return (letters >= len(text) * 0.3) or (chinese > 0)

    def _format_result(self, content, file_path):
        """格式化最终结果"""
        if not content:
            return ""

        # 清理和格式化文本
        cleaned_content = self._clean_and_format_text(content)

        file_name = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)
        file_ext = os.path.splitext(file_path)[1].upper()

        result = f"""# PowerPoint文档转换结果（原生模式）

**文件信息：**
- 文件名：{file_name}
- 文件格式：{file_ext}
- 文件大小：{file_size/1024:.1f} KB
- 转换时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- 转换模式：Python原生库

---

{cleaned_content}

---

**转换说明：**
- 此转换器仅使用Python标准库
- 提取PPT/PPTX文件中的文本内容
- 支持基本的标题、段落和列表结构
- 不支持复杂的动画、图表和多媒体内容

支持的文件格式：
- .pptx（PowerPoint 2007+）
- .ppt（PowerPoint 97-2003，有限支持）

*转换完成 | 使用Python原生PPT解析*"""

        return result

    def _clean_and_format_text(self, text):
        """清理和格式化文本"""
        if not text:
            return ""

        # 按行分割并清理
        lines = text.split('\n')
        cleaned_lines = []

        for line in lines:
            line = line.strip()
            if line and len(line) > 1:
                # 移除多余的空格
                line = re.sub(r'\s+', ' ', line)

                # 过滤掉明显的垃圾内容
                if not self._is_garbage_line(line):
                    cleaned_lines.append(line)

        if not cleaned_lines:
            return ""

        # 尝试识别标题和结构
        result_lines = []
        for i, line in enumerate(cleaned_lines):
            # 简单的标题识别逻辑
            if (len(line) < 50 and line.isupper() and
                i < len(cleaned_lines) - 1 and len(cleaned_lines[i + 1]) > 50):
                result_lines.append(f"## {line}")
            elif (len(line) < 30 and line.endswith(':') and
                  not line.endswith('.')):
                result_lines.append(f"### {line}")
            elif line.startswith(('-', '•', '*', '·')):
                result_lines.append(f"- {line[1:].strip()}")
            elif line[0].isdigit() and ('.' in line or ')' in line):
                result_lines.append(f"1. {line}")
            else:
                result_lines.append(line)

        # 重新组织段落
        final_lines = []
        for i, line in enumerate(result_lines):
            final_lines.append(line)

            # 适当添加空行分隔段落
            if (i < len(result_lines) - 1 and
                not line.startswith('#') and not line.startswith('-') and
                len(line) > 100 and len(result_lines[i + 1]) > 50):
                final_lines.append('')

        return '\n'.join(final_lines)

    def _is_garbage_line(self, line):
        """判断是否为垃圾行"""
        if not line:
            return True

        # 排除纯符号行
        if all(not c.isalnum() for c in line):
            return True

        # 排除过短的随机字符
        if len(line) < 3 and not any(c.isalpha() or '\u4e00' <= c <= '\u9fff' for c in line):
            return True

        # 排除看起来像随机数据的行
        if len(line) < 10:
            special_chars = sum(1 for c in line if not c.isalnum() and c not in ' .,;:!?,')
            if special_chars > len(line) * 0.5:
                return True

        return False

