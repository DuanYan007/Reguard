#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
from datetime import datetime


def img_converter(path):
    """将图片文件转换为Markdown文本"""
    if not path:
        return "# 转换错误\n\n图片路径不能为空"
    if not os.path.exists(path):
        return f"# 转换错误\n\n图片文件不存在: {path}"

    image_extensions = {'.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif', '.webp', '.gif'}
    file_ext = os.path.splitext(path)[1].lower()
    if file_ext not in image_extensions:
        return f"# 转换错误\n\n不支持的图片格式: {file_ext}\n支持的格式: {', '.join(image_extensions)}"

    file_size = os.path.getsize(path)
    if file_size == 0:
        return f"# 转换错误\n\n图片文件为空: {path}"
    elif file_size > 50 * 1024 * 1024:
        return f"# 转换错误\n\n图片文件过大: {file_size / 1024 / 1024:.1f}MB，请使用小于50MB的图片"

    try:
        from paddleocr import PPStructureV3
    except ImportError:
        return """# 转换错误\n\nPaddleOCR未安装

    请安装PaddleOCR：
    ```bash
    pip install paddleocr
    ```"""

    try:
        from PIL import Image
        with Image.open(path) as img:
            img.verify()
    except ImportError:
        pass
    except Exception as e:
        return f"# 转换错误\n\n图片文件损坏或格式不支持: {str(e)}"

    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 开始处理图片: {path}")

    pipeline = PPStructureV3(
        use_doc_orientation_classify=False,
        use_doc_unwarping=False,
        device="gpu"
    )
    output = pipeline.predict(input=path)

    if not output:
        return "# 识别结果\n\n未识别到任何文本内容"

    content = ""
    for i, res in enumerate(output):

        if hasattr(res, 'markdown') and res.markdown:
            markdown_data = res.markdown
            if isinstance(markdown_data, dict):
                if "markdown_texts" in markdown_data:
                    content += markdown_data["markdown_texts"] + "\n\n"
                else:
                    for key, value in markdown_data.items():
                        if isinstance(value, str) and len(value.strip()) > 10:
                            content += value + "\n\n"
            elif isinstance(markdown_data, str):
                content += markdown_data + "\n\n"
    if not content.strip():
        return "# 识别结果\n\n图片中未检测到有效文本内容"

    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 图片处理完成")

    return f"# 图片识别结果\n\n{content.strip()}"
