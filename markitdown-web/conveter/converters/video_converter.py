#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import struct
import re
from datetime import datetime, timedelta
from pathlib import Path


def video_converter(file_path):
    """将视频文件转换为Markdown格式字符串（仅使用Python原生库）"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()
    supported_formats = ['.mp4', '.avi', '.mov', '.mkv', '.wmv', '.flv', '.webm', '.m4v', '.3gp', '.mpg', '.mpeg']

    if file_ext not in supported_formats:
        raise ValueError(f"目前只支持这些视频格式: {', '.join(supported_formats)}")

    converter = NativeVideoToMarkdownConverter()
    return converter.convert(file_path)


class NativeVideoToMarkdownConverter:
    """原生视频文件转Markdown转换器（仅使用Python标准库）"""

    def __init__(self):
        self.metadata = {}

    def convert(self, file_path):
        """主转换方法"""
        try:
            file_ext = os.path.splitext(file_path)[1].lower()

            # 提取基本文件信息
            self._extract_file_info(file_path)

            # 根据格式提取元数据
            if file_ext in ['.mp4', '.m4v', '.mov']:
                self._extract_mp4_metadata(file_path)
            elif file_ext == '.avi':
                self._extract_avi_metadata(file_path)
            elif file_ext == '.mkv':
                self._extract_mkv_metadata(file_path)
            elif file_ext == '.wmv':
                self._extract_wmv_metadata(file_path)
            elif file_ext == '.flv':
                self._extract_flv_metadata(file_path)
            elif file_ext == '.webm':
                self._extract_webm_metadata(file_path)
            elif file_ext in ['.mpg', '.mpeg']:
                self._extract_mpeg_metadata(file_path)
            elif file_ext == '.3gp':
                self._extract_3gp_metadata(file_path)
            else:
                self._extract_generic_video_metadata(file_path)

            return self._format_result(file_path)

        except Exception as e:
            return f"# 视频转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _extract_file_info(self, file_path):
        """提取基本文件信息"""
        self.metadata['file_name'] = os.path.basename(file_path)
        self.metadata['file_size'] = os.path.getsize(file_path)
        self.metadata['file_extension'] = os.path.splitext(file_path)[1].upper()
        self.metadata['conversion_time'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    def _extract_mp4_metadata(self, file_path):
        """提取MP4文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            # 查找主要atoms
            atoms = self._parse_mp4_atoms(content)

            # 提取文件类型信息
            if 'ftyp' in atoms:
                ftyp_data = atoms['ftyp']
                if len(ftyp_data) >= 8:
                    major_brand = ftyp_data[4:8].decode('ascii', errors='ignore')
                    self.metadata['文件类型'] = major_brand

            # 提取视频尺寸和时长信息
            if 'mvhd' in atoms:
                self._parse_mvhd_atom(atoms['mvhd'])

            if 'tkhd' in atoms:
                self._parse_tkhd_atom(atoms['tkhd'])

            # 提取媒体信息
            if 'mdia' in atoms:
                self._parse_mdia_atom(atoms['mdia'])

            # 简单搜索可能的时间信息
            self._search_duration_patterns(content)

        except Exception as e:
            print(f"[警告] MP4元数据提取失败: {str(e)}")

    def _parse_mp4_atoms(self, content, offset=0):
        """解析MP4 atoms"""
        atoms = {}
        pos = offset

        while pos < len(content) - 8:
            # 读取atom头部
            if pos + 8 > len(content):
                break

            size = struct.unpack('>I', content[pos:pos+4])[0]
            atom_type = content[pos+4:pos+8].decode('ascii', errors='ignore')

            if size == 0:
                # atom到文件结尾
                atom_data = content[pos+8:]
                atoms[atom_type] = atom_data
                break
            elif size == 1:
                # 64位大小
                if pos + 16 > len(content):
                    break
                size = struct.unpack('>Q', content[pos+8:pos+16])[0]
                atom_start = pos + 16
            else:
                atom_start = pos + 8

            if size < 8 or atom_start + size > len(content):
                break

            atom_data = content[atom_start:atom_start+size-8]
            atoms[atom_type] = atom_data

            # 如果atom包含子atoms，递归解析
            if atom_type in ['moov', 'trak', 'mdia', 'minf', 'dinf', 'stbl']:
                sub_atoms = self._parse_mp4_atoms(atom_data)
                atoms.update(sub_atoms)

            pos = atom_start + (size - 8)

            # 防止无限循环
            if pos > len(content) or len(atoms) > 100:
                break

        return atoms

    def _parse_mvhd_atom(self, mvhd_data):
        """解析mvhd atom (movie header)"""
        try:
            if len(mvhd_data) < 24:
                return

            # 版本和标志
            version = mvhd_data[0]
            flags = mvhd_data[1:4]

            if version == 0:
                # 32位时间戳
                creation_time = struct.unpack('>I', mvhd_data[4:8])[0]
                modification_time = struct.unpack('>I', mvhd_data[8:12])[0]
                timescale = struct.unpack('>I', mvhd_data[12:16])[0]
                duration = struct.unpack('>I', mvhd_data[16:20])[0]
            else:
                # 64位时间戳
                creation_time = struct.unpack('>Q', mvhd_data[4:12])[0]
                modification_time = struct.unpack('>Q', mvhd_data[12:20])[0]
                timescale = struct.unpack('>I', mvhd_data[20:24])[0]
                duration = struct.unpack('>Q', mvhd_data[24:32])[0]

            # 计算时长
            if timescale > 0 and duration > 0:
                duration_seconds = duration / timescale
                duration_str = self._format_duration(duration_seconds)
                self.metadata['时长'] = duration_str
                self.metadata['时间基'] = f"1/{timescale}"

            # 时间基准 (1904-01-01)
            if creation_time > 0:
                creation_date = datetime(1904, 1, 1) + timedelta(seconds=creation_time)
                self.metadata['创建时间'] = creation_date.strftime('%Y-%m-%d %H:%M:%S')

        except Exception:
            pass

    def _parse_tkhd_atom(self, tkhd_data):
        """解析tkhd atom (track header)"""
        try:
            if len(tkhd_data) < 78:
                return

            # 版本和标志
            version = tkhd_data[0]
            flags = struct.unpack('>I', b'\x00' + tkhd_data[1:4])[0]

            offset = 12 if version == 0 else 24

            # 跳过时间戳信息，读取宽度和高度
            if len(tkhd_data) >= offset + 16:
                width = struct.unpack('>I', tkhd_data[offset+12:offset+16])[0] >> 16
                height = struct.unpack('>I', tkhd_data[offset+16:offset+20])[0] >> 16

                if width > 0 and height > 0:
                    self.metadata['视频尺寸'] = f"{width} x {height}"

        except Exception:
            pass

    def _parse_mdia_atom(self, mdia_data):
        """解析mdia atom (media)"""
        try:
            # 查找子atoms
            sub_atoms = self._parse_mp4_atoms(mdia_data)

            # 解析video media header
            if 'vmhd' in sub_atoms:
                self.metadata['媒体类型'] = '视频'

            # 解析sound media header
            if 'smhd' in sub_atoms:
                self.metadata['音频轨道'] = '是'

        except Exception:
            pass

    def _search_duration_patterns(self, content):
        """搜索时长相关信息"""
        try:
            # 搜索常见的时间信息模式
            patterns = [
                rb'([0-9]{2}):([0-9]{2}):([0-9]{2})',  # HH:MM:SS
                rb'([0-9]{1,2}):([0-9]{2}):([0-9]{2})\.([0-9]{3})',  # MM:SS.mmm
                rb'duration[":=]\s*([0-9]+)',  # duration=12345
                rb'DURATION[":=]\s*([0-9]+)',  # DURATION=12345
            ]

            for pattern in patterns:
                matches = re.findall(pattern, content)
                if matches:
                    for match in matches[:1]:  # 只取第一个匹配
                        try:
                            if len(match) == 3:
                                h, m, s = map(int, match)
                                total_seconds = h * 3600 + m * 60 + s
                                duration_str = self._format_duration(total_seconds)
                                self.metadata['检测时长'] = duration_str
                                break
                        except:
                            continue

        except Exception:
            pass

    def _extract_avi_metadata(self, file_path):
        """提取AVI文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                # AVI文件以RIFF开头
                header = f.read(12)
                if len(header) < 12 or not header.startswith(b'RIFF') or not header[8:12] == b'AVI ':
                    return

                # 读取AVI头部
                while True:
                    chunk_header = f.read(8)
                    if len(chunk_header) < 8:
                        break

                    chunk_id = chunk_header[:4]
                    chunk_size = struct.unpack('<I', chunk_header[4:8])[0]

                    if chunk_id == b'avih':
                        # AVI主头部
                        avih_data = f.read(min(56, chunk_size))
                        self._parse_avih(avih)
                    elif chunk_id == b'strl':
                        # 流信息
                        self._parse_strl(f, chunk_size)
                    else:
                        # 跳过其他块
                        f.read(chunk_size)

                    if chunk_size % 2 == 1:  # AVI块需要字对齐
                        f.read(1)

        except Exception as e:
            print(f"[警告] AVI元数据提取失败: {str(e)}")

    def _parse_avih(self, avih_data):
        """解析AVI主头部"""
        try:
            if len(avih_data) < 32:
                return

            micro_sec_per_frame = struct.unpack('<I', avih_data[0:4])[0]
            max_bytes_per_sec = struct.unpack('<I', avih_data[4:8])[0]
            width = struct.unpack('<I', avih_data[32:36])[0] if len(avih_data) >= 36 else 0
            height = struct.unpack('<I', avih_data[36:40])[0] if len(avih_data) >= 40 else 0

            if width > 0 and height > 0:
                self.metadata['视频尺寸'] = f"{width} x {height}"

            if micro_sec_per_frame > 0:
                fps = 1000000 / micro_sec_per_frame
                self.metadata['帧率'] = f"{fps:.2f} fps"

        except Exception:
            pass

    def _parse_strl(self, f, chunk_size):
        """解析流信息"""
        try:
            end_pos = f.tell() + chunk_size

            while f.tell() < end_pos:
                chunk_header = f.read(8)
                if len(chunk_header) < 8:
                    break

                chunk_id = chunk_header[:4]
                stream_size = struct.unpack('<I', chunk_header[4:8])[0]

                if chunk_id == b'strh':
                    strh_data = f.read(min(48, stream_size))
                    self._parse_strh(strh_data)
                elif chunk_id == b'strf':
                    strf_data = f.read(stream_size)
                    self._parse_strf(strf_data)
                else:
                    f.read(stream_size)

                if stream_size % 2 == 1:
                    f.read(1)

        except Exception:
            pass

    def _parse_strh(self, strh_data):
        """解析流头部"""
        try:
            if len(strh_data) < 8:
                return

            fcc_type = strh_data[0:4].decode('ascii', errors='ignore')
            if fcc_type == 'vids':
                self.metadata['视频流'] = '是'
            elif fcc_type == 'auds':
                self.metadata['音频流'] = '是'

        except Exception:
            pass

    def _parse_strf(self, strf_data):
        """解析流格式"""
        try:
            if len(strf_data) < 40:
                return

            # BITMAPINFOHEADER格式
            width = struct.unpack('<I', strf_data[4:8])[0]
            height = struct.unpack('<I', strf_data[8:12])[0]
            bit_count = struct.unpack('<H', strf_data[14:16])[0]

            if width > 0 and height > 0 and '视频尺寸' not in self.metadata:
                self.metadata['视频尺寸'] = f"{width} x {height}"

            if bit_count > 0:
                self.metadata['位深度'] = f"{bit_count} bit"

        except Exception:
            pass

    def _extract_mkv_metadata(self, file_path):
        """提取MKV文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read(1024)  # 只读取前1KB

            # 查找EBML头部
            if content[:4] == b'\x1a\x45\xdf\xa3':
                self.metadata['容器格式'] = 'Matroska'

            # 搜索分辨率信息
            resolution_patterns = [
                rb'([0-9]{3,4})x([0-9]{3,4})',  # 1920x1080
                rb'width[":=]\s*([0-9]+)',      # width=1920
                rb'height[":=]\s*([0-9]+)',     # height=1080
            ]

            for pattern in resolution_patterns:
                matches = re.findall(pattern, content)
                if matches:
                    for match in matches[:1]:
                        try:
                            if isinstance(match, tuple) and len(match) == 2:
                                width, height = map(int, match)
                                self.metadata['检测分辨率'] = f"{width} x {height}"
                                break
                        except:
                            continue

        except Exception as e:
            print(f"[警告] MKV元数据提取失败: {str(e)}")

    def _extract_wmv_metadata(self, file_path):
        """提取WMV文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                header = f.read(24)

            # WMV/ASF文件标识
            if header.startswith(b'\x30\x26\xb2\x75\x8e\x66\xcf\x11\xa6\xd9'):
                self.metadata['容器格式'] = 'Advanced Systems Format (ASF)'
                self.metadata['编码格式'] = 'Windows Media Video'

        except Exception as e:
            print(f"[警告] WMV元数据提取失败: {str(e)}")

    def _extract_flv_metadata(self, file_path):
        """提取FLV文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                header = f.read(13)

            # FLV文件标识
            if header.startswith(b'FLV'):
                version = header[3]
                has_audio = (header[4] & 0x04) != 0
                has_video = (header[4] & 0x01) != 0

                self.metadata['FLV版本'] = version
                if has_video:
                    self.metadata['视频流'] = '是'
                if has_audio:
                    self.metadata['音频流'] = '是'

        except Exception as e:
            print(f"[警告] FLV元数据提取失败: {str(e)}")

    def _extract_webm_metadata(self, file_path):
        """提取WebM文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read(64)

            # WebM使用类似MKV的EBML结构
            if content[:4] == b'\x1a\x45\xdf\xa3':
                self.metadata['容器格式'] = 'WebM'
                self.metadata['编码格式'] = 'VP8/VP9'

        except Exception as e:
            print(f"[警告] WebM元数据提取失败: {str(e)}")

    def _extract_mpeg_metadata(self, file_path):
        """提取MPEG文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read(1024)

            # MPEG文件包标识
            if b'\x00\x00\x01\xba' in content:
                self.metadata['容器格式'] = 'MPEG Program Stream'

            # 搜索分辨率信息
            resolution_match = re.search(rb'([0-9]{3,4})x([0-9]{3,4})', content)
            if resolution_match:
                try:
                    width, height = map(int, resolution_match.groups())
                    self.metadata['检测分辨率'] = f"{width} x {height}"
                except:
                    pass

        except Exception as e:
            print(f"[警告] MPEG元数据提取失败: {str(e)}")

    def _extract_3gp_metadata(self, file_path):
        """提取3GP文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                header = f.read(12)

            # 3GP基于MP4格式
            if header.startswith(b'ftyp'):
                major_brand = header[4:8].decode('ascii', errors='ignore')
                if major_brand in ['3gp4', '3gp5', '3gp6', '3g2a']:
                    self.metadata['文件类型'] = f'3GP ({major_brand})'
                    self.metadata['容器格式'] = '3GPP'

        except Exception as e:
            print(f"[警告] 3GP元数据提取失败: {str(e)}")

    def _extract_generic_video_metadata(self, file_path):
        """提取通用视频文件信息"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read(512)  # 只读取前512字节

            # 检测常见视频格式标识
            format_signatures = {
                b'ftyp': 'MP4/QuickTime格式',
                b'RIFF': 'AVI/WMV格式',
                b'\x1a\x45\xdf\xa3': 'Matroska/WebM格式',
                b'FLV': 'Flash Video格式',
                b'ID3': 'MP3格式 (可能包含视频)',
                b'OGG': 'OGG格式'
            }

            for signature, description in format_signatures.items():
                if signature in content:
                    self.metadata['检测格式'] = description
                    break

        except Exception:
            pass

    def _format_duration(self, seconds):
        """格式化时长"""
        try:
            hours = int(seconds // 3600)
            minutes = int((seconds % 3600) // 60)
            secs = int(seconds % 60)

            if hours > 0:
                return f"{hours:02d}:{minutes:02d}:{secs:02d}"
            else:
                return f"{minutes:02d}:{secs:02d}"
        except:
            return "00:00"

    def _format_result(self, file_path):
        """格式化最终结果"""
        file_name = os.path.basename(file_path)
        file_size = self.metadata.get('file_size', 0)

        result = f"""# 视频文件元数据

**文件信息：**
- 文件名：{file_name}
- 文件格式：{self.metadata.get('file_extension', 'Unknown')}
- 文件大小：{file_size/1024/1024:.1f} MB
- 转换时间：{self.metadata.get('conversion_time', datetime.now().strftime('%Y-%m-%d %H:%M:%S'))}

**技术信息：**
"""

        # 添加容器和编码信息
        container_fields = ['容器格式', '文件类型', '检测格式', '编码格式']
        for field in container_fields:
            if field in self.metadata:
                result += f"- {field}：{self.metadata[field]}\n"

        # 添加视频信息
        video_fields = ['视频尺寸', '检测分辨率', '帧率', '位深度', '视频流']
        video_info = []

        for field in video_fields:
            if field in self.metadata:
                video_info.append(f"- {field}：{self.metadata[field]}")

        if video_info:
            result += "\n**视频信息：**\n"
            result += "\n".join(video_info) + "\n"

        # 添加音频信息
        audio_fields = ['音频流', '音频轨道']
        audio_info = []

        for field in audio_fields:
            if field in self.metadata:
                audio_info.append(f"- {field}：{self.metadata[field]}")

        if audio_info:
            result += "\n**音频信息：**\n"
            result += "\n".join(audio_info) + "\n"

        # 添加时长信息
        duration_fields = ['时长', '检测时长', '时间基']
        duration_info = []

        for field in duration_fields:
            if field in self.metadata:
                duration_info.append(f"- {field}：{self.metadata[field]}")

        if duration_info:
            result += "\n**时长信息：**\n"
            result += "\n".join(duration_info) + "\n"

        # 添加其他信息
        other_fields = ['创建时间', 'FLV版本']
        other_info = []

        for field in other_fields:
            if field in self.metadata:
                other_info.append(f"- {field}：{self.metadata[field]}")

        if other_info:
            result += "\n**其他信息：**\n"
            result += "\n".join(other_info) + "\n"

        result += f"""
---

**转换说明：**
- 此转换器使用Python原生库提取视频文件元数据
- 支持的格式：MP4, AVI, MOV, MKV, WMV, FLV, WebM, M4V, 3GP, MPG/MPEG
- 提取的信息包括：文件基本信息、视频编码参数、时长信息、分辨率等
- 对于某些格式，可能需要完整的文件解析才能提取所有信息

支持的视频格式：
- MP4/M4V (QuickTime/ISO基础媒体文件格式)
- AVI (Audio Video Interleave)
- MKV (Matroska)
- MOV (QuickTime电影文件)
- WMV (Windows Media Video)
- FLV (Flash Video)
- WebM (Web媒体格式)
- 3GP (3GPP媒体格式)
- MPG/MPEG (MPEG程序流)

*转换完成 | 使用Python原生视频元数据提取*"""

        return result


# 兼容性别名
video_conveter = video_converter