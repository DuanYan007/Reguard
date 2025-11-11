#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import struct
import re
from datetime import datetime
from pathlib import Path


def audio_converter(file_path):
    """将音频文件转换为Markdown格式字符串（仅使用Python原生库）"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件不存在: {file_path}")

    file_ext = os.path.splitext(file_path)[1].lower()
    supported_formats = ['.mp3', '.wav', '.flac', '.aac', '.ogg', '.m4a', '.wma']

    if file_ext not in supported_formats:
        raise ValueError(f"目前只支持这些音频格式: {', '.join(supported_formats)}")

    converter = NativeAudioToMarkdownConverter()
    return converter.convert(file_path)


class NativeAudioToMarkdownConverter:
    """原生音频文件转Markdown转换器（仅使用Python标准库）"""

    def __init__(self):
        self.metadata = {}

    def convert(self, file_path):
        """主转换方法"""
        try:
            file_ext = os.path.splitext(file_path)[1].lower()

            # 提取基本文件信息
            self._extract_file_info(file_path)

            # 根据格式提取元数据
            if file_ext == '.mp3':
                self._extract_mp3_metadata(file_path)
            elif file_ext == '.wav':
                self._extract_wav_metadata(file_path)
            elif file_ext == '.flac':
                self._extract_flac_metadata(file_path)
            elif file_ext in ['.aac', '.m4a']:
                self._extract_aac_metadata(file_path)
            elif file_ext == '.ogg':
                self._extract_ogg_metadata(file_path)
            else:
                self._extract_generic_audio_metadata(file_path)

            return self._format_result(file_path)

        except Exception as e:
            return f"# 音频转换错误\n\n在处理文件 `{file_path}` 时发生错误: {str(e)}"

    def _extract_file_info(self, file_path):
        """提取基本文件信息"""
        self.metadata['file_name'] = os.path.basename(file_path)
        self.metadata['file_size'] = os.path.getsize(file_path)
        self.metadata['file_extension'] = os.path.splitext(file_path)[1].upper()
        self.metadata['conversion_time'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    def _extract_mp3_metadata(self, file_path):
        """提取MP3文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()

            # 查找ID3v2标签
            id3v2_start = content.find(b'ID3')
            if id3v2_start >= 0:
                self._parse_id3v2_tags(content, id3v2_start)

            # 查找ID3v1标签（文件末尾128字节）
            if len(content) >= 128:
                id3v1_start = len(content) - 128
                if content[id3v1_start:id3v1_start+3] == b'TAG':
                    self._parse_id3v1_tags(content, id3v1_start)

            # 提取MP3帧信息
            self._extract_mp3_frame_info(content)

        except Exception as e:
            print(f"[警告] MP3元数据提取失败: {str(e)}")

    def _parse_id3v2_tags(self, content, start_pos):
        """解析ID3v2标签"""
        try:
            # ID3v2头部信息
            header = content[start_pos:start_pos+10]
            if len(header) < 10:
                return

            version = header[3]
            flags = header[5]
            size = self._synchsafe_to_int(header[6:10])

            self.metadata['id3_version'] = f"2.{version}"

            # 解析标签帧
            pos = start_pos + 10
            end_pos = start_pos + 10 + size

            while pos < end_pos - 10:
                # 读取帧头
                frame_id = content[pos:pos+4].decode('ascii', errors='ignore')
                frame_size = self._synchsafe_to_int(content[pos+4:pos+8])
                flags = content[pos+8:pos+10]

                if frame_size == 0 or not frame_id.isalnum():
                    break

                pos += 10
                if pos + frame_size > end_pos:
                    break

                # 读取帧数据
                frame_data = content[pos:pos+frame_size]

                # 解码文本（通常以ISO-8859-1或UTF-8编码）
                try:
                    if frame_data and frame_data[0] in [0, 1, 2, 3]:
                        # 有编码标识
                        encoding = frame_data[0]
                        text_data = frame_data[1:]

                        if encoding == 0:  # ISO-8859-1
                            text = text_data.decode('iso-8859-1', errors='ignore')
                        elif encoding == 1:  # UTF-16 with BOM
                            text = text_data.decode('utf-16', errors='ignore')
                        elif encoding == 2:  # UTF-16BE without BOM
                            text = text_data.decode('utf-16-be', errors='ignore')
                        elif encoding == 3:  # UTF-8
                            text = text_data.decode('utf-8', errors='ignore')
                        else:
                            text = text_data.decode('utf-8', errors='ignore')
                    else:
                        # 没有编码标识，假设为UTF-8
                        text = frame_data.decode('utf-8', errors='ignore')

                    # 清理文本
                    text = text.strip('\x00').strip()

                    if text and len(text) > 0:
                        # 映射标签ID到描述性名称
                        tag_mapping = {
                            'TIT2': '标题',
                            'TPE1': '艺术家',
                            'TALB': '专辑',
                            'TDRC': '年份',
                            'TRCK': '音轨号',
                            'TCON': '流派',
                            'TPE2': '专辑艺术家',
                            'TPOS': '光盘编号',
                            'TYER': '录制年份',
                            'TLEN': '长度（毫秒）',
                            'TBPM': 'BPM',
                            'TCOM': '作曲家',
                            'TPE3': '指挥家',
                            'TEXT': '作词家',
                            'TPUB': '发行商',
                            'TKEY': '调性'
                        }

                        tag_name = tag_mapping.get(frame_id, frame_id)
                        self.metadata[tag_name] = text

                except Exception:
                    pass

                pos += frame_size

        except Exception:
            pass

    def _parse_id3v1_tags(self, content, start_pos):
        """解析ID3v1标签"""
        try:
            # ID3v1标签格式：TAG(3) + 标题(30) + 艺术家(30) + 专辑(30) + 年份(4) + 注释(30) + 音轨(1) + 流派(1)

            title = content[start_pos+3:start_pos+33].decode('latin-1', errors='ignore').strip('\x00 ').strip()
            artist = content[start_pos+33:start_pos+63].decode('latin-1', errors='ignore').strip('\x00 ').strip()
            album = content[start_pos+63:start_pos+93].decode('latin-1', errors='ignore').strip('\x00 ').strip()
            year = content[start_pos+93:start_pos+97].decode('latin-1', errors='ignore').strip('\x00 ').strip()

            if title and '标题' not in self.metadata:
                self.metadata['标题'] = title
            if artist and '艺术家' not in self.metadata:
                self.metadata['艺术家'] = artist
            if album and '专辑' not in self.metadata:
                self.metadata['专辑'] = album
            if year and '年份' not in self.metadata:
                self.metadata['年份'] = year

        except Exception:
            pass

    def _extract_mp3_frame_info(self, content):
        """提取MP3帧信息"""
        try:
            # 查找MP3帧同步字（11个连续的1位）
            sync_pattern = b'\xFF\xFB'
            pos = 0
            frame_count = 0

            while pos < len(content) - 4:
                pos = content.find(sync_pattern, pos)
                if pos == -1:
                    break

                # 提取帧头信息
                header_bytes = content[pos:pos+4]
                if len(header_bytes) == 4:
                    header = struct.unpack('>I', header_bytes)[0]

                    # 解析MPEG版本
                    version_bits = (header >> 19) & 0x3
                    version = {1: 'MPEG 1', 3: 'MPEG 2'}.get(version_bits, 'Unknown')

                    # 解析层
                    layer_bits = (header >> 17) & 0x3
                    layer = {1: 'Layer III', 2: 'Layer II', 3: 'Layer I'}.get(layer_bits, 'Unknown')

                    # 解析比特率
                    bitrate_index = (header >> 12) & 0xF
                    bitrate_mapping = {
                        1: 32, 2: 40, 3: 48, 4: 56, 5: 64, 6: 80, 7: 96, 8: 112,
                        9: 128, 10: 160, 11: 192, 12: 224, 13: 256, 14: 320
                    }
                    bitrate = bitrate_mapping.get(bitrate_index, 'Unknown')

                    # 解析采样率
                    sample_rate_index = (header >> 10) & 0x3
                    sample_rate = {0: 44100, 1: 48000, 2: 32000}.get(sample_rate_index, 'Unknown')

                    if version and layer and bitrate != 'Unknown':
                        self.metadata['音频格式'] = f"{version} {layer}"
                        self.metadata['比特率'] = f"{bitrate} kbps"
                        if sample_rate != 'Unknown':
                            self.metadata['采样率'] = f"{sample_rate} Hz"

                        # 估算时长
                        if bitrate != 'Unknown':
                            duration_seconds = (len(content) * 8) // (bitrate * 1000)
                            minutes = duration_seconds // 60
                            seconds = duration_seconds % 60
                            self.metadata['估算时长'] = f"{minutes:02d}:{seconds:02d}"

                        break

                frame_count += 1
                if frame_count > 10:  # 只检查前10帧
                    break

                pos += 1

        except Exception:
            pass

    def _extract_wav_metadata(self, file_path):
        """提取WAV文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                # RIFF头部
                riff_header = f.read(12)
                if len(riff_header) < 12 or not riff_header.startswith(b'RIFF') or not riff_header[8:12] == b'WAVE':
                    return

                # 读取格式块
                while True:
                    chunk_header = f.read(8)
                    if len(chunk_header) < 8:
                        break

                    chunk_id = chunk_header[:4]
                    chunk_size = struct.unpack('<I', chunk_header[4:8])[0]

                    if chunk_id == b'fmt ':
                        # 格式块
                        fmt_data = f.read(min(16, chunk_size))
                        if len(fmt_data) >= 16:
                            format_type = struct.unpack('<H', fmt_data[0:2])[0]
                            channels = struct.unpack('<H', fmt_data[2:4])[0]
                            sample_rate = struct.unpack('<I', fmt_data[4:8])[0]
                            byte_rate = struct.unpack('<I', fmt_data[8:12])[0]
                            block_align = struct.unpack('<H', fmt_data[12:14])[0]
                            bits_per_sample = struct.unpack('<H', fmt_data[14:16])[0]

                            format_names = {1: 'PCM', 3: 'IEEE Float', 6: 'A-law', 7: 'μ-law'}
                            self.metadata['音频格式'] = format_names.get(format_type, f'Format {format_type}')
                            self.metadata['声道数'] = channels
                            self.metadata['采样率'] = f"{sample_rate} Hz"
                            self.metadata['比特深度'] = f"{bits_per_sample} bit"
                            self.metadata['字节率'] = f"{byte_rate // 1000} kB/s"

                    elif chunk_id == b'data':
                        # 数据块
                        self.metadata['音频数据大小'] = f"{chunk_size // 1024} KB"

                        # 计算估算时长
                        if '采样率' in self.metadata and '比特深度' in self.metadata and '声道数' in self.metadata:
                            sample_rate = int(str(self.metadata['采样率']).replace(' Hz', ''))
                            bits_per_sample = int(str(self.metadata['比特深度']).replace(' bit', ''))
                            channels = self.metadata['声道数']

                            if sample_rate > 0 and bits_per_sample > 0:
                                bytes_per_second = sample_rate * channels * (bits_per_sample // 8)
                                duration_seconds = chunk_size / bytes_per_second
                                minutes = int(duration_seconds // 60)
                                seconds = int(duration_seconds % 60)
                                self.metadata['估算时长'] = f"{minutes:02d}:{seconds:02d}"

                        break
                    else:
                        # 跳过其他块
                        f.read(chunk_size)

        except Exception as e:
            print(f"[警告] WAV元数据提取失败: {str(e)}")

    def _extract_flac_metadata(self, file_path):
        """提取FLAC文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                # FLAC文件以fLaC开头
                if f.read(4) != b'fLaC':
                    return

                while True:
                    # 读取元数据块头部
                    header = f.read(4)
                    if len(header) < 4:
                        break

                    last_flag = (header[0] & 0x80) >> 7
                    block_type = header[0] & 0x7F
                    block_size = struct.unpack('>I', b'\x00' + header[1:4])[0]

                    if block_type == 4:  # Vorbis comment块
                        comment_data = f.read(block_size)
                        self._parse_vorbis_comments(comment_data)
                    else:
                        f.read(block_size)

                    if last_flag:
                        break

        except Exception as e:
            print(f"[警告] FLAC元数据提取失败: {str(e)}")

    def _extract_aac_metadata(self, file_path):
        """提取AAC/M4A文件元数据"""
        try:
            # AAC/M4A文件通常使用MP4容器，包含moov atom
            with open(file_path, 'rb') as f:
                content = f.read()

            # 查找ftyp box
            ftyp_pos = content.find(b'ftyp')
            if ftyp_pos >= 0:
                # 读取文件类型
                major_brand = content[ftyp_pos+4:ftyp_pos+8].decode('ascii', errors='ignore')
                self.metadata['文件类型'] = major_brand

            # 查找moov atom（包含元数据）
            moov_pos = content.find(b'moov')
            if moov_pos >= 0:
                # 简单的文本搜索查找可能的标签信息
                text_patterns = [
                    rb'\xa9nam',  # 标题
                    rb'\xa9ART',  # 艺术家
                    rb'\xa9alb',  # 专辑
                    rb'\xa9gen',  # 流派
                    rb'\xa9day',  # 年份
                ]

                pattern_names = {
                    rb'\xa9nam': '标题',
                    rb'\xa9ART': '艺术家',
                    rb'\xa9alb': '专辑',
                    rb'\xa9gen': '流派',
                    rb'\xa9day': '年份',
                }

                for pattern in text_patterns:
                    pos = content.find(pattern, moov_pos)
                    if pos >= 0:
                        # 尝试提取后面的文本
                        text_data = content[pos+4:pos+100]  # 读取后面100字节
                        # 查找可打印文本
                        text_match = re.search(rb'[A-Za-z0-9\u4e00-\u9fff\s]{2,50}', text_data)
                        if text_match:
                            try:
                                text = text_match.group(0).decode('utf-8', errors='ignore').strip()
                                if text:
                                    self.metadata[pattern_names[pattern]] = text
                            except:
                                pass

        except Exception as e:
            print(f"[警告] AAC元数据提取失败: {str(e)}")

    def _extract_ogg_metadata(self, file_path):
        """提取OGG文件元数据"""
        try:
            with open(file_path, 'rb') as f:
                # OGG文件以OggS开头
                if f.read(4) != b'OggS':
                    return

                # 跳过版本和header类型等信息
                f.read(22)

                # 读取segment table
                segment_count = ord(f.read(1))
                segments = [ord(f.read(1)) for _ in range(segment_count)]
                total_size = sum(segments)

                # 读取segment数据
                segment_data = f.read(total_size)

                # 查找Vorbis comment
                if b'vorbis' in segment_data:
                    self._parse_vorbis_comments(segment_data)

        except Exception as e:
            print(f"[警告] OGG元数据提取失败: {str(e)}")

    def _parse_vorbis_comments(self, comment_data):
        """解析Vorbis comment格式"""
        try:
            # Vorbis comment格式：vendor_length(4) + vendor_string + comment_count(4) + comments...
            if len(comment_data) < 8:
                return

            # 跳过vendor信息
            vendor_length = struct.unpack('<I', comment_data[0:4])[0]
            pos = 4 + vendor_length

            if pos + 4 > len(comment_data):
                return

            comment_count = struct.unpack('<I', comment_data[pos:pos+4])[0]
            pos += 4

            for i in range(min(comment_count, 50)):  # 限制最多50个注释
                if pos + 4 > len(comment_data):
                    break

                comment_length = struct.unpack('<I', comment_data[pos:pos+4])[0]
                pos += 4

                if pos + comment_length > len(comment_data):
                    break

                comment = comment_data[pos:pos+comment_length].decode('utf-8', errors='ignore')
                pos += comment_length

                # 解析键值对
                if '=' in comment:
                    key, value = comment.split('=', 1)
                    key = key.upper()

                    # 映射常见标签
                    tag_mapping = {
                        'TITLE': '标题',
                        'ARTIST': '艺术家',
                        'ALBUM': '专辑',
                        'DATE': '年份',
                        'GENRE': '流派',
                        'TRACKNUMBER': '音轨号',
                        'ALBUMARTIST': '专辑艺术家',
                        'COMPOSER': '作曲家',
                        'PERFORMER': '表演者',
                        'ENCODEDBY': '编码者',
                        'ENCODER': '编码工具',
                        'BPM': 'BPM',
                        'COMMENT': '注释'
                    }

                    tag_name = tag_mapping.get(key, key)
                    self.metadata[tag_name] = value

        except Exception:
            pass

    def _extract_generic_audio_metadata(self, file_path):
        """提取通用音频文件信息"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read(1024)  # 只读取前1KB

            # 尝试检测文件格式
            if content.startswith(b'ID3'):
                self.metadata['检测格式'] = 'MP3 (ID3标签)'
            elif content.startswith(b'RIFF') and b'WAVE' in content:
                self.metadata['检测格式'] = 'WAV'
            elif content.startswith(b'fLaC'):
                self.metadata['检测格式'] = 'FLAC'
            elif content.startswith(b'OggS'):
                self.metadata['检测格式'] = 'OGG'
            else:
                self.metadata['检测格式'] = '未知音频格式'

        except Exception:
            pass

    def _synchsafe_to_int(self, bytes_data):
        """将同步安全整数转换为普通整数"""
        result = 0
        for byte in bytes_data:
            result = (result << 7) | (byte & 0x7F)
        return result

    def _format_result(self, file_path):
        """格式化最终结果"""
        file_name = os.path.basename(file_path)
        file_size = self.metadata.get('file_size', 0)

        result = f"""# 音频文件元数据

**文件信息：**
- 文件名：{file_name}
- 文件格式：{self.metadata.get('file_extension', 'Unknown')}
- 文件大小：{file_size/1024:.1f} KB
- 转换时间：{self.metadata.get('conversion_time', datetime.now().strftime('%Y-%m-%d %H:%M:%S'))}

**技术信息：**
"""

        # 添加技术信息
        tech_fields = ['音频格式', '检测格式', '比特率', '采样率', '比特深度', '声道数', '字节率', '音频数据大小']
        for field in tech_fields:
            if field in self.metadata:
                result += f"- {field}：{self.metadata[field]}\n"

        # 添加时长信息
        if '估算时长' in self.metadata:
            result += f"- 估算时长：{self.metadata['估算时长']}\n"

        # 添加内容信息
        content_fields = ['标题', '艺术家', '专辑', '年份', '音轨号', '流派', '专辑艺术家', '作曲家', '表演者']
        content_info = []

        for field in content_fields:
            if field in self.metadata:
                content_info.append(f"- {field}：{self.metadata[field]}")

        if content_info:
            result += "\n**内容信息：**\n"
            result += "\n".join(content_info) + "\n"

        # 添加其他元数据
        other_fields = ['BPM', '发行商', '作词家', '指挥家', '编码者', '编码工具', '注释', '调性']
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
- 此转换器使用Python原生库提取音频文件元数据
- 支持的格式：MP3, WAV, FLAC, AAC, M4A, OGG
- 提取的信息包括：文件基本信息、音频编码参数、内容元数据
- 对于某些格式，可能需要特定的编码方式才能完全提取所有信息

支持的音频格式：
- MP3 (ID3v1, ID3v2)
- WAV (PCM)
- FLAC (Vorbis comments)
- AAC/M4A (iTunes格式)
- OGG Vorbis

*转换完成 | 使用Python原生音频元数据提取*"""

        return result


# 兼容性别名
audio_conveter = audio_converter