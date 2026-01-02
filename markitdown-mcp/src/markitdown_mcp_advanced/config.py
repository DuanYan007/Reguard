import os
from typing import Optional


class Config:
    """全局配置类"""

    # PaddleOCR API 服务配置
    PADDLE_API_URL: Optional[str] = os.getenv("PADDLE_API_URL")
    PADDLE_TOKEN: Optional[str] = os.getenv("PADDLE_TOKEN")

    # OCR 开关
    # USE_OCR: bool = os.getenv("USE_OCR", "true").lower() == "true"

    # 临时文件目录
    # TEMP_DIR: str = os.getenv("MARKITDOWN_TEMP_DIR", "/tmp")
    TEMP_DIR: str = os.getenv("MARKITDOWN_TEMP_DIR")
