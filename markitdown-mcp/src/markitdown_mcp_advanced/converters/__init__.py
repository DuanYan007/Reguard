"""
统一转换入口
根据文件扩展名自动选择转换器
"""
from pathlib import Path
from markitdown_mcp_advanced.converters import pdf, image, docx, pptx, xlsx, xls, html, csv
from markitdown_mcp_advanced.utils import download_file
from markitdown_mcp_advanced.config import Config

# 映射表：文件扩展名 -> 转换函数
CONVERTERS = {
    # PDF
    '.pdf': pdf.convert,

    # 图片
    '.png': image.convert,
    '.jpg': image.convert,
    '.jpeg': image.convert,
    '.gif': image.convert,
    '.bmp': image.convert,
    '.tiff': image.convert,
    '.webp': image.convert,

    # Office
    '.docx': docx.convert,
    '.pptx': pptx.convert,
    '.xlsx': xlsx.convert,
    '.xls': xls.convert,

    # 音频

    # 网页
    '.html': html.convert,
    '.htm': html.convert,

    # 文本
    '.csv': csv.convert,
}


def convert(source: str) -> str:

    ext = Path(source).suffix.lower()

    # 查找对应的转换器
    converter = CONVERTERS.get(ext)

    if not converter:
        raise ValueError(f"Unsupported file format: {ext}")

    # 如果是 URL，先下载到本地
    local_path = download_file(source, temp_dir=Config.TEMP_DIR)

    # 执行转换（传入本地路径）
    return converter(local_path)


def get_supported_formats() -> list[str]:
    """
    获取支持的文件格式列表

    Returns:
        list[str]: 支持的文件扩展名
    """
    return list(CONVERTERS.keys())
