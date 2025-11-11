#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import struct
from datetime import datetime
from pathlib import Path


def pdf_native_converter(file_path):
    """将PDF文档转换为Markdown格式字符串（仅使用Python原生库）"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()
    if file_ext != '.pdf':
        raise ValueError("目前只支持 .pdf 格式文件")

    converter = NativePdfToMarkdownConverter()
    return converter.convert(file_path)


class NativePdfToMarkdownConverter:
    """原生PDF文件转Markdown转换器（仅使用Python标准库）"""

    def __init__(self):
        self.markdown_lines = []

    def convert(self, file_path):
        """主转换方法"""
        self.markdown_lines = []

        try:
            # 验证文件
            if not self._validate_pdf_file(file_path):
                return "# 转换错误\n\n文件验证失败，不是有效的PDF文件"

            print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 开始处理PDF（原生模式）: {file_path}")

            # 使用原生方法提取文本
            content = self._extract_text_from_pdf(file_path)

            if not content:
                return """# PDF转换结果

PDF文件中未检测到可提取的文本内容。

注意：此转换器使用Python原生库，功能有限。
对于扫描件或复杂布局的PDF，建议使用PaddleOCR转换器。

可尝试：
1. 使用包含可提取文字的PDF文件
2. 尝试将PDF另存为文本格式"""

            # 格式化输出
            return self._format_result(content, file_path)

        except Exception as e:
            return f"# 转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _validate_pdf_file(self, file_path):
        """验证PDF文件头部"""
        try:
            with open(file_path, 'rb') as f:
                header = f.read(5)
                return header == b'%PDF-'
        except Exception:
            return False

    def _extract_text_from_pdf(self, file_path):
        """使用原生方法从PDF提取文本"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            # 方法1: 尝试提取流对象中的文本
            text_content = self._extract_text_from_streams(content)

            if text_content and len(text_content.strip()) > 20:
                return text_content

            # 方法2: 尝试直接查找可打印字符
            text_content = self._extract_printable_text(content)

            if text_content and len(text_content.strip()) > 20:
                return text_content

            # 方法3: 提取字符串对象
            text_content = self._extract_string_objects(content)

            return text_content

        except Exception as e:
            print(f"[警告] 文本提取失败: {str(e)}")
            return None

    def _extract_text_from_streams(self, content):
        """从PDF流对象中提取文本"""
        try:
            # 查找流对象
            stream_pattern = rb'stream\s*(.*?)\s*endstream'
            streams = re.findall(stream_pattern, content, re.DOTALL)

            extracted_text = []

            for stream in streams:
                try:
                    # 移除可能的编码数据
                    if len(stream) > 100:
                        # 检查是否包含可打印文本
                        text = self._decode_stream_text(stream)
                        if text and len(text.strip()) > 10:
                            extracted_text.append(text)
                except Exception:
                    continue

            if extracted_text:
                return "\n\n".join(extracted_text)

        except Exception:
            pass

        return None

    def _decode_stream_text(self, stream_data):
        """解码流数据中的文本"""
        try:
            # 移除常见的二进制数据模式
            if re.search(rb'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\xFF]', stream_data):
                return None

            # 尝试UTF-8解码
            try:
                text = stream_data.decode('utf-8')
                if self._is_meaningful_text(text):
                    return text
            except UnicodeDecodeError:
                pass

            # 尝试Latin-1解码
            try:
                text = stream_data.decode('latin-1')
                if self._is_meaningful_text(text):
                    return text
            except UnicodeDecodeError:
                pass

            # 尝试CP1252解码
            try:
                text = stream_data.decode('cp1252')
                if self._is_meaningful_text(text):
                    return text
            except UnicodeDecodeError:
                pass

        except Exception:
            pass

        return None

    def _extract_string_objects(self, content):
        """提取PDF字符串对象"""
        try:
            # 查找字符串对象模式
            string_pattern = rb'\(([^)]*)\)'
            matches = re.findall(string_pattern, content)

            text_parts = []

            for match in matches:
                try:
                    # 移除转义字符
                    decoded = self._decode_pdf_string(match)
                    if decoded and len(decoded.strip()) > 2:
                        text_parts.append(decoded)
                except Exception:
                    continue

            if text_parts:
                return " ".join(text_parts)

        except Exception:
            pass

        return None

    def _decode_pdf_string(self, pdf_string):
        """解码PDF字符串"""
        try:
            # 处理常见的转义序列
            decoded = pdf_string

            # 移除常见的PDF转义
            escaped_patterns = [
                rb'\\n',
                rb'\\r',
                rb'\\t',
                rb'\\b',
                rb'\\f',
                rb'\\\\',
                rb'\\(',
                rb'\\)'
            ]

            for pattern in escaped_patterns:
                decoded = decoded.replace(pattern, b'')

            # 尝试不同的编码
            for encoding in ['utf-8', 'latin-1', 'cp1252']:
                try:
                    text = decoded.decode(encoding)
                    if self._is_meaningful_text(text):
                        return text
                except UnicodeDecodeError:
                    continue

        except Exception:
            pass

        return None

    def _extract_printable_text(self, content):
        """提取文件中的可打印文本"""
        try:
            # 查找连续的可打印字符序列
            # PDF中的文本通常在BT和ET操作符之间
            text_blocks = []

            # 方法1: 查找BT/ET块
            bt_et_pattern = rb'BT\s*(.*?)\s*ET'
            blocks = re.findall(bt_et_pattern, content, re.DOTALL)

            for block in blocks:
                text = self._extract_text_from_block(block)
                if text:
                    text_blocks.append(text)

            # 方法2: 查找字符串操作符 (Tj)
            tj_pattern = rb'\((.*?)\)\s*Tj'
            strings = re.findall(tj_pattern, content)

            for s in strings:
                try:
                    text = s.decode('utf-8', errors='ignore')
                    if self._is_meaningful_text(text):
                        text_blocks.append(text)
                except:
                    pass

            if text_blocks:
                return " ".join(text_blocks)

        except Exception:
            pass

        return None

    def _extract_text_from_block(self, block):
        """从文本块中提取文字"""
        try:
            # 查找字符串内容
            string_pattern = rb'\(([^)]*)\)'
            matches = re.findall(string_pattern, block)

            texts = []
            for match in matches:
                try:
                    text = match.decode('utf-8', errors='ignore')
                    if self._is_meaningful_text(text):
                        texts.append(text)
                except:
                    pass

            if texts:
                return " ".join(texts)

        except Exception:
            pass

        return None

    def _is_meaningful_text(self, text):
        """判断文本是否有意义"""
        if not text or len(text.strip()) < 2:
            return False

        text = text.strip()

        # 排除纯数字或特殊字符
        if text.isdigit():
            return False

        # 检查是否包含足够的可打印字符
        printable_chars = sum(1 for c in text if c.isprintable() and not c.isspace())
        if printable_chars < len(text) * 0.7:  # 至少70%是可打印字符
            return False

        # 检查是否包含中文字符或英文字母
        has_chinese = any('\u4e00' <= c <= '\u9fff' for c in text)
        has_letters = any(c.isalpha() for c in text)

        return has_chinese or has_letters

    def _format_result(self, content, file_path):
        """格式化最终结果"""
        # 清理和格式化文本
        cleaned_content = self._clean_and_format_text(content)

        file_name = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)

        result = f"""# PDF文档转换结果（原生模式）

**文件信息：**
- 文件名：{file_name}
- 文件大小：{file_size/1024:.1f} KB
- 转换时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- 转换模式：Python原生库

---

{cleaned_content}

---

**转换说明：**
- 此转换器仅使用Python标准库
- 对于扫描件或复杂布局的PDF，效果可能有限
- 建议使用PaddleOCR转换器获得更好的效果
- 支持的PDF：包含可提取文字的PDF文件

*转换完成 | 使用Python原生PDF解析*"""

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

        # 尝试识别标题
        result_lines = []
        for i, line in enumerate(cleaned_lines):
            # 简单的标题识别逻辑
            if (len(line) < 50 and line.isupper() and
                i < len(cleaned_lines) - 1 and len(cleaned_lines[i + 1]) > 50):
                result_lines.append(f"## {line}")
            elif (len(line) < 30 and line.endswith(':') and
                  not line.endswith('.')):
                result_lines.append(f"### {line}")
            else:
                result_lines.append(line)

        # 重新组织段落
        final_lines = []
        for i, line in enumerate(result_lines):
            final_lines.append(line)

            # 适当添加空行分隔段落
            if (i < len(result_lines) - 1 and
                not line.startswith('#') and
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


