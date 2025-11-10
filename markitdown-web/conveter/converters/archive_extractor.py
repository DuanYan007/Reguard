import zipfile
import os
import tempfile
import shutil
from pathlib import Path
import threading
import time

# 尝试导入rarfile，如果失败则提供替代方案
try:
    import rarfile
    RARFILE_AVAILABLE = True
except ImportError:
    RARFILE_AVAILABLE = False
    print("警告: rarfile模块未安装，RAR文件支持将被禁用。安装命令: pip install rarfile")

class ArchiveExtractor:
    """压缩文件提取器，支持ZIP和RAR格式"""

    def __init__(self):
        self.temp_dir = None
        self.extracted_files = []
        self.total_files = 0
        self.extracted_count = 0

    def create_temp_directory(self):
        """创建临时目录"""
        if not self.temp_dir:
            self.temp_dir = tempfile.mkdtemp(prefix="archive_extract_")
        return self.temp_dir

    def cleanup_temp_directory(self):
        """清理临时目录"""
        if self.temp_dir and os.path.exists(self.temp_dir):
            shutil.rmtree(self.temp_dir, ignore_errors=True)
            self.temp_dir = None
        self.extracted_files = []

    def extract_archive(self, archive_path, password=None):
        """
        提取压缩文件

        Args:
            archive_path (str): 压缩文件路径
            password (str, optional): 解压密码

        Returns:
            dict: 提取结果信息
        """
        try:
            self.create_temp_directory()
            self.extracted_files = []
            self.total_files = 0
            self.extracted_count = 0

            # 根据文件扩展名选择解压方法
            if archive_path.lower().endswith('.zip'):
                return self._extract_zip(archive_path, password)
            elif archive_path.lower().endswith('.rar'):
                return self._extract_rar(archive_path, password)
            else:
                raise ValueError(f"不支持的压缩文件格式: {archive_path}")

        except Exception as e:
            self.cleanup_temp_directory()
            raise Exception(f"解压失败: {str(e)}")

    def _extract_zip(self, zip_path, password=None):
        """提取ZIP文件"""
        try:
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                # 获取所有文件信息
                file_list = zip_ref.infolist()
                self.total_files = len([f for f in file_list if not f.is_dir()])

                extracted_info = []

                for file_info in file_list:
                    if file_info.is_dir():
                        continue

                    try:
                        # 处理文件名编码问题
                        try:
                            filename = file_info.filename.encode('cp437').decode('utf-8')
                        except:
                            try:
                                filename = file_info.filename.encode('gbk').decode('utf-8')
                            except:
                                filename = file_info.filename

                        # 检查文件是否被加密
                        if file_info.flag_bits & 0x1:
                            if not password:
                                raise ValueError("文件需要密码，但未提供密码")

                        # 提取文件 - 确保路径不包含目录结构，只保留文件名
                        filename_only = os.path.basename(filename)
                        extract_path = os.path.join(self.temp_dir, filename_only)

                        # 如果目标文件已存在，添加序号
                        base_name, ext = os.path.splitext(filename_only)
                        counter = 1
                        while os.path.exists(extract_path):
                            filename_only = f"{base_name}_{counter}{ext}"
                            extract_path = os.path.join(self.temp_dir, filename_only)
                            counter += 1

                        # 提取文件（带密码或无密码）
                        try:
                            with zip_ref.open(file_info, pwd=password.encode() if password else None) as source:
                                with open(extract_path, 'wb') as target:
                                    shutil.copyfileobj(source, target)
                        except Exception as extract_error:
                            print(f"[调试] 提取文件失败 {filename}: {str(extract_error)}")
                            raise extract_error

                        # 记录提取信息
                        file_info_dict = {
                            'filename': filename_only,  # 使用实际提取的文件名
                            'original_path': filename,
                            'extracted_path': extract_path,
                            'size': file_info.file_size,
                            'format': self._detect_file_format(filename_only),
                            'is_encrypted': bool(file_info.flag_bits & 0x1),
                            'extract_time': time.time()
                        }

                        self.extracted_files.append(file_info_dict)
                        self.extracted_count += 1

                    except Exception as e:
                        # 记录失败的文件
                        file_info_dict = {
                            'filename': file_info.filename,
                            'original_path': file_info.filename,
                            'extracted_path': None,
                            'size': file_info.file_size,
                            'format': self._detect_file_format(file_info.filename),
                            'error': str(e),
                            'extract_time': time.time()
                        }
                        self.extracted_files.append(file_info_dict)

                return {
                    'success': True,
                    'total_files': self.total_files,
                    'extracted_files': len([f for f in self.extracted_files if not f.get('error')]),
                    'failed_files': len([f for f in self.extracted_files if f.get('error')]),
                    'files': self.extracted_files,
                    'temp_dir': self.temp_dir
                }

        except zipfile.BadZipFile:
            raise Exception("ZIP文件格式错误或已损坏")
        except Exception as e:
            raise Exception(f"ZIP解压失败: {str(e)}")

    def _extract_rar(self, rar_path, password=None):
        """提取RAR文件"""
        if not RARFILE_AVAILABLE:
            raise Exception("RAR文件支持不可用，请安装rarfile模块: pip install rarfile")

        try:
            # 设置RAR文件的默认路径（如果需要的话）
            # rarfile.UNRAR_TOOL = "unrar"  # 确保系统安装了unrar

            with rarfile.RarFile(rar_path, 'r') as rar_ref:
                # 获取所有文件信息
                file_list = rar_ref.infolist()
                self.total_files = len([f for f in file_list if not f.is_dir()])

                extracted_info = []

                for file_info in file_list:
                    if file_info.is_dir():
                        continue

                    try:
                        filename = file_info.filename

                        # 检查文件是否被加密
                        if file_info.needs_password():
                            if not password:
                                raise ValueError("文件需要密码，但未提供密码")

                        # 提取文件
                        extract_path = os.path.join(self.temp_dir, os.path.basename(filename))

                        # 确保目录存在
                        os.makedirs(os.path.dirname(extract_path), exist_ok=True)

                        # 提取文件（带密码或无密码）
                        with rar_ref.open(file_info, pwd=password) as source:
                            with open(extract_path, 'wb') as target:
                                shutil.copyfileobj(source, target)

                        # 记录提取信息
                        file_info_dict = {
                            'filename': filename,
                            'original_path': filename,
                            'extracted_path': extract_path,
                            'size': file_info.file_size,
                            'format': self._detect_file_format(filename),
                            'is_encrypted': file_info.needs_password(),
                            'extract_time': time.time()
                        }

                        self.extracted_files.append(file_info_dict)
                        self.extracted_count += 1

                    except Exception as e:
                        # 记录失败的文件
                        file_info_dict = {
                            'filename': file_info.filename,
                            'original_path': file_info.filename,
                            'extracted_path': None,
                            'size': file_info.file_size,
                            'format': self._detect_file_format(file_info.filename),
                            'error': str(e),
                            'extract_time': time.time()
                        }
                        self.extracted_files.append(file_info_dict)

                return {
                    'success': True,
                    'total_files': self.total_files,
                    'extracted_files': len([f for f in self.extracted_files if not f.get('error')]),
                    'failed_files': len([f for f in self.extracted_files if f.get('error')]),
                    'files': self.extracted_files,
                    'temp_dir': self.temp_dir
                }

        except rarfile.BadRarFile:
            raise Exception("RAR文件格式错误或已损坏")
        except Exception as e:
            raise Exception(f"RAR解压失败: {str(e)}")

    def _detect_file_format(self, filename):
        """根据文件扩展名检测文件格式"""
        if not filename:
            return 'unknown'

        ext = filename.split('.')[-1].lower() if '.' in filename else ''
        print(f"[调试] 检测文件格式: {filename} -> {ext}")

        format_map = {
            'pdf': 'pdf',
            'doc': 'word', 'docx': 'word',
            'xls': 'excel', 'xlsx': 'excel',
            'ppt': 'ppt', 'pptx': 'ppt',
            'jpg': 'image', 'jpeg': 'image', 'png': 'image',
            'gif': 'image', 'bmp': 'image',
            'mp3': 'audio', 'wav': 'audio', 'flac': 'audio',
            'aac': 'audio', 'ogg': 'audio', 'm4a': 'audio',
            'wma': 'audio',
            'mp4': 'video', 'avi': 'video', 'mov': 'video',
            'mkv': 'video', 'wmv': 'video', 'flv': 'video',
            'webm': 'video', 'm4v': 'video', '3gp': 'video',
            'mpg': 'video', 'mpeg': 'video',
            'html': 'html', 'htm': 'html',
            'txt': 'text',  # 添加txt格式
            'csv': 'csv',
            'json': 'json',
            'xml': 'xml'
        }

        return format_map.get(ext, 'unknown')

    def get_supported_formats(self):
        """获取支持的压缩文件格式"""
        formats = ['.zip']
        if RARFILE_AVAILABLE:
            formats.append('.rar')
        return formats

    def is_supported_format(self, filename):
        """检查文件格式是否支持"""
        if not filename:
            print(f"[调试] 检查文件格式支持: {filename} -> 文件名为空，不支持")
            return False

        ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''
        supported_formats = self.get_supported_formats()
        is_supported = ext in supported_formats
        print(f"[调试] 检查文件格式支持: {filename} -> ext: {ext}, 支持格式: {supported_formats}, 结果: {is_supported}")
        return is_supported

# 全局解压器实例
archive_extractor = ArchiveExtractor()