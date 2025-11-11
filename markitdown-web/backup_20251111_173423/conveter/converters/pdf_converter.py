#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
from datetime import datetime
from pathlib import Path


def pdf_converter(file_path):
    """将PDF文档转换为Markdown格式字符串"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()
    if file_ext != '.pdf':
        raise ValueError("目前只支持 .pdf 格式文件")

    converter = PdfToMarkdownConverter()
    return converter.convert(file_path)


class PdfToMarkdownConverter:
    """PDF文件转Markdown转换器"""

    def __init__(self):
        self.markdown_content = []
        self.extracted_images = []

    def convert(self, file_path):
        """主转换方法"""
        self.markdown_content = []
        self.extracted_images = []

        try:
            # 验证文件
            if not self._validate_file(file_path):
                return "# 转换错误\n\n文件验证失败"

            # 检查依赖
            if not self._check_dependencies():
                return """# 转换错误

PaddleOCR未安装

请安装PaddleOCR：
```bash
pip install paddleocr
```"""

            print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 开始处理PDF: {file_path}")

            # 使用PaddleOCR处理 - 让它自己处理图片保存
            content = self._process_with_paddleocr(file_path)

            # 如果OCR失败，尝试使用其他方法
            if not content or len(content.strip()) < 10:
                content = self._process_with_fallback(file_path)

            # 整合最终结果
            return self._format_final_result(content, file_path)

        except Exception as e:
            return f"# 转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _validate_file(self, file_path):
        """验证PDF文件"""
        try:
            if not os.path.exists(file_path):
                return False

            file_size = os.path.getsize(file_path)
            if file_size == 0:
                return False
            elif file_size > 100 * 1024 * 1024:  # 100MB限制
                return False

            # 尝试用PyMuPDF验证
            try:
                import fitz
                with fitz.open(file_path) as doc:
                    return doc.page_count > 0
            except ImportError:
                return True  # 如果PyMuPDF没安装，跳过验证
            except Exception:
                return False

        except Exception:
            return False

    def _check_dependencies(self):
        """检查依赖库"""
        try:
            from paddleocr import PPStructureV3
            return True
        except ImportError:
            return False

    def _process_with_paddleocr(self, file_path):
        """使用PaddleOCR处理PDF"""
        from paddleocr import PPStructureV3

        pipeline = PPStructureV3(
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            device="gpu"
        )

        output = pipeline.predict(input=file_path)
        return self._extract_content_from_output(output, file_path)

    def _extract_content_from_output(self, output, file_path):
        """从PaddleOCR输出中提取内容和图片 - 按照官方方式"""
        if not output:
            return None

        markdown_list = []
        markdown_images = []

        for i, res in enumerate(output):
            try:
                if hasattr(res, 'markdown') and res.markdown:
                    md_info = res.markdown

                    # 按照官方示例处理
                    markdown_list.append(md_info)
                    if isinstance(md_info, dict) and "markdown_images" in md_info:
                        markdown_images.append(md_info["markdown_images"])

            except Exception as e:
                print(f"[警告] 处理第 {i + 1} 页结果时出错: {str(e)}")
                continue

        # 按照官方方式保存图片
        self._save_images_official_way(markdown_images, file_path)

        # 合并markdown内容
        try:
            from paddleocr import PPStructureV3
            pipeline = PPStructureV3()
            content = pipeline.concatenate_markdown_pages(markdown_list)
        except Exception:
            content = "\n\n".join([str(md) for md in markdown_list])

        return content

    def _save_images_official_way(self, markdown_images, file_path):
        """按照PaddleOCR官方方式保存图片 - 不干预路径"""
        try:
            # 获取当前工作目录作为基准
            current_dir = os.getcwd()

            for item in markdown_images:
                if item:
                    for path, image in item.items():
                        if image is not None:
                            # 直接使用PaddleOCR生成的路径，不做任何修改
                            # PaddleOCR生成的path如"imgs/xxx.jpg"，我们就按这个路径保存
                            file_path_abs = os.path.join(current_dir, path)
                            parent_dir = os.path.dirname(file_path_abs)

                            # 确保目录存在
                            os.makedirs(parent_dir, exist_ok=True)

                            # 保存图片到PaddleOCR指定的位置
                            image.save(file_path_abs)
                            print(f"[信息] PaddleOCR保存图片: {file_path_abs}")

        except Exception as e:
            print(f"[警告] 保存PaddleOCR图片时出错: {str(e)}")

    def _process_with_fallback(self, file_path):
        """备用处理方法（使用其他库）"""
        try:
            # 尝试使用PyMuPDF提取文本
            import fitz

            content_parts = []
            with fitz.open(file_path) as doc:
                for page_num in range(doc.page_count):
                    page = doc[page_num]
                    text = page.get_text()
                    if text.strip():
                        content_parts.append(f"## 第 {page_num + 1} 页\n\n{text}")

            if content_parts:
                return "\n\n".join(content_parts)

        except ImportError:
            pass
        except Exception as e:
            print(f"[警告] 备用方法失败: {str(e)}")

        return None

    def _process_images_to_absolute_paths(self, content):
        """将图片路径从相对路径转换为绝对路径"""
        try:
            import re

            # 获取当前工作目录的绝对路径
            current_dir = os.getcwd()

            # 处理HTML img标签中的相对路径
            # 匹配 <img src="imgs/xxx.jpg"> 格式
            img_pattern = r'<img\s+src="imgs/([^"]+)"'

            def replace_img_src(match):
                img_file = match.group(1)
                img_rel_path = f"imgs/{img_file}"
                img_abs_path = os.path.join(current_dir, img_rel_path)
                img_abs_path = os.path.abspath(img_abs_path)
                # 使用正斜杠保持跨平台兼容性
                img_abs_path = img_abs_path.replace('\\', '/')
                return f'<img src="{img_abs_path}"'

            content = re.sub(img_pattern, replace_img_src, content)

            # 处理markdown格式的图片路径 ![alt](imgs/xxx.jpg)
            md_img_pattern = r'!\[(.*?)\]\(imgs/([^)]+)\)'

            def replace_md_img(match):
                alt_text = match.group(1)
                img_file = match.group(2)
                img_rel_path = f"imgs/{img_file}"
                img_abs_path = os.path.join(current_dir, img_rel_path)
                img_abs_path = os.path.abspath(img_abs_path)
                # 使用正斜杠保持跨平台兼容性
                img_abs_path = img_abs_path.replace('\\', '/')
                return f'![{alt_text}]({img_abs_path})'

            content = re.sub(md_img_pattern, replace_md_img, content)

            print(f"[信息] 已将PDF中的图片路径转换为绝对路径")
            return content

        except Exception as e:
            print(f"[警告] 处理图片路径转换时出错: {str(e)}")
            return content

    def _format_final_result(self, content, file_path):
        """格式化最终结果"""
        if not content or len(content.strip()) < 10:
            return """# PDF识别结果

    PDF中未检测到有效的文本内容。

    可能的原因：
    1. PDF文件是图片扫描件
    2. PDF文件加密或受保护
    3. PDF文件内容为空

    建议：
    1. 尝试使用包含文字内容的PDF文件
    2. 检查PDF文件是否可以正常打开
    3. 确认PDF文件未被加密"""

        # 处理图片路径为绝对路径
        content = self._process_images_to_absolute_paths(content)

        # 添加文档信息头部
        file_name = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)

        result = f"""# PDF文档转换结果

    **文件信息：**
    - 文件名：{file_name}
    - 文件大小：{file_size / 1024:.1f} KB
    - 转换时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

    ---

    {content}

    ---

    *转换完成 | 使用PaddleOCR进行文字识别和版面分析*"""

        return result
