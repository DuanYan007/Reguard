import uuid
from pathlib import Path
import urllib.request
import urllib.error
import os


def is_url(source: str) -> bool:
    """判断是否为 URL"""
    return source.startswith(('http://', 'https://'))


def download_file(source: str, temp_dir: str = None) -> Path:
    if not is_url(source):
        return Path(source)

    temp_dir = os.getenv("TEMP_DIR")
    # 确保临时目录存在
    Path(temp_dir).mkdir(parents=True, exist_ok=True)

    filename = os.path.basename(source)
    local_path = Path(temp_dir) / f"markitdown_{uuid.uuid4()}_{filename}"

    try:
        urllib.request.urlretrieve(source, local_path)
        return local_path
    except urllib.error.URLError as e:
        raise urllib.error.URLError(f"Failed to download {source}: {str(e)}")


def get_file_extension(source: str) -> str:
    """获取文件扩展名"""
    return Path(source).suffix.lower()


