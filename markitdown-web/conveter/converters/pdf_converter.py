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
        self.output_dir = "./uploads/images"
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
                return """# 转换错误\n\nPaddleOCR未安装

请安装PaddleOCR：
```bash
pip install paddleocr
```"""

            print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 开始处理PDF: {file_path}")

            # 创建输出目录
            os.makedirs(self.output_dir, exist_ok=True)

            # 使用PaddleOCR处理
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
        try:
            from paddleocr import PPStructureV3

            # 优先尝试GPU，如果失败则使用CPU
            for device in ["gpu", "cpu"]:
                try:
                    pipeline = PPStructureV3(
                        use_doc_orientation_classify=False,
                        use_doc_unwarping=False,
                        device=device
                    )

                    output = pipeline.predict(input=file_path)
                    return self._extract_content_from_output(output, file_path)

                except Exception as e:
                    if device == "gpu" and ("CUDA" in str(e) or "GPU" in str(e)):
                        print(f"[信息] GPU不可用，切换到CPU处理: {str(e)}")
                        continue
                    else:
                        raise e

        except Exception as e:
            print(f"[警告] PaddleOCR处理失败: {str(e)}")
            return None

    def _extract_content_from_output(self, output, file_path):
        """从PaddleOCR输出中提取内容和图片"""
        if not output:
            return None

        markdown_text = []
        page_images = []

        for i, res in enumerate(output):
            try:
                if hasattr(res, 'markdown') and res.markdown:
                    md_info = res.markdown

                    if isinstance(md_info, dict):
                        if "markdown_texts" in md_info:
                            markdown_text.append(md_info["markdown_texts"])

                        if "markdown_images" in md_info:
                            images_info = md_info["markdown_images"]
                            if isinstance(images_info, dict) and images_info:
                                page_images.append(images_info)
                    elif isinstance(md_info, str):
                        markdown_text.append(md_info)

            except Exception as e:
                print(f"[警告] 处理第 {i+1} 页结果时出错: {str(e)}")
                continue

        # 保存图片
        saved_images = self._save_images(page_images, file_path)

        # 合并文本内容
        if markdown_text:
            try:
                from paddleocr import PPStructureV3
                pipeline = PPStructureV3()
                content = pipeline.concatenate_markdown_pages(markdown_text)
            except Exception:
                content = "\n\n".join(markdown_text)
        else:
            content = ""

        # 在内容中插入图片引用
        content = self._insert_image_references(content, saved_images)

        return content

    def _save_images(self, page_images, file_path):
        """保存提取的图片"""
        saved_images = []

        try:
            for page_idx, page_images_dict in enumerate(page_images):
                if not page_images_dict:
                    continue

                for img_path, img_data in page_images_dict.items():
                    if img_data is None:
                        continue

                    try:
                        if hasattr(img_data, 'save'):
                            # 生成安全的文件名
                            safe_filename = self._generate_safe_filename(file_path, page_idx, img_path)
                            full_img_path = os.path.join(self.output_dir, safe_filename)

                            # 确保目录存在
                            os.makedirs(os.path.dirname(full_img_path), exist_ok=True)

                            img_data.save(full_img_path)

                            # 保存图片信息
                            image_info = {
                                'filename': safe_filename,
                                'path': full_img_path,
                                'relative_path': os.path.relpath(full_img_path, self.output_dir),
                                'size': os.path.getsize(full_img_path),
                                'format': img_data.format if hasattr(img_data, 'format') else 'Unknown'
                            }

                            saved_images.append(image_info)
                            print(f"[信息] 图片已保存: {full_img_path}")

                    except Exception as e:
                        print(f"[警告] 保存图片失败 {img_path}: {str(e)}")
                        continue

        except Exception as e:
            print(f"[警告] 处理图片时出错: {str(e)}")

        return saved_images

    def _generate_safe_filename(self, file_path, page_idx, original_path):
        """生成安全的文件名"""
        base_name = os.path.splitext(os.path.basename(file_path))[0]
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

        # 提取原始文件扩展名
        if '.' in original_path:
            ext = original_path.split('.')[-1].lower()
        else:
            ext = 'jpg'

        safe_filename = f"{base_name}_page{page_idx+1}_{timestamp}.{ext}"
        return safe_filename

    def _insert_image_references(self, content, saved_images):
        """在内容中插入图片引用"""
        if not saved_images:
            return content

        # 简单的图片插入策略：在段落之间插入图片
        lines = content.split('\n')
        result_lines = []
        image_idx = 0

        for i, line in enumerate(lines):
            result_lines.append(line)

            # 在某些位置插入图片
            if (image_idx < len(saved_images) and
                (i > 0 and i % 5 == 0 or line.strip().endswith('。'))):
                img_info = saved_images[image_idx]
                result_lines.append(f"\n![PDF图片{image_idx+1}](images/{img_info['relative_path']})\n")
                image_idx += 1

        # 如果还有剩余图片，添加到末尾
        while image_idx < len(saved_images):
            img_info = saved_images[image_idx]
            result_lines.append(f"\n![PDF图片{image_idx+1}](images/{img_info['relative_path']})\n")
            image_idx += 1

        return '\n'.join(result_lines)

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

        # 添加文档信息头部
        file_name = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)

        result = f"""# PDF文档转换结果

**文件信息：**
- 文件名：{file_name}
- 文件大小：{file_size/1024:.1f} KB
- 提取图片：{len(self.extracted_images)} 张
- 转换时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

---

{content}

---

*转换完成 | 使用PaddleOCR进行文字识别和版面分析*"""

        return result


