from flask import Flask, render_template, request, jsonify, send_file, abort, redirect
import os
import uuid
import json
import datetime
import threading
import time
from converters import csv_converter, pdf_converter, img_converter
from converters.word_converter import word_converter
from converters.pdf_native_converter import pdf_native_converter
from converters.ppt_native_converter import ppt_native_converter
from converters.audio_converter import audio_converter
from converters.video_converter import video_converter
# 修改为内联的解压功能，避免rarfile依赖
def extract_archive_safe(archive_path, password=None):
    """安全的ZIP文件提取，不依赖外部工具"""
    import zipfile
    import tempfile
    import shutil

    try:
        temp_dir = tempfile.mkdtemp(prefix="archive_extract_")
        extracted_files = []

        with zipfile.ZipFile(archive_path, 'r') as zip_ref:
            file_list = zip_ref.infolist()
            total_files = len([f for f in file_list if not f.is_dir()])

            for file_info in file_list:
                if file_info.is_dir():
                    continue

                try:
                    # 处理文件名
                    filename = file_info.filename
                    filename_only = os.path.basename(filename)
                    extract_path = os.path.join(temp_dir, filename_only)

                    # 处理重名
                    base_name, ext = os.path.splitext(filename_only)
                    counter = 1
                    while os.path.exists(extract_path):
                        filename_only = f"{base_name}_{counter}{ext}"
                        extract_path = os.path.join(temp_dir, filename_only)
                        counter += 1

                    # 提取文件
                    with zip_ref.open(file_info, pwd=password.encode() if password else None) as source:
                        with open(extract_path, 'wb') as target:
                            shutil.copyfileobj(source, target)

                    # 检测格式
                    ext = filename_only.split('.')[-1].lower() if '.' in filename_only else ''
                    format_map = {
                        'pdf': 'pdf', 'doc': 'word', 'docx': 'word',
                        'xls': 'excel', 'xlsx': 'excel', 'ppt': 'ppt', 'pptx': 'ppt',
                        'jpg': 'image', 'jpeg': 'image', 'png': 'image', 'gif': 'image',
                        'html': 'html', 'htm': 'html', 'txt': 'text',
                        'csv': 'csv', 'json': 'json', 'xml': 'xml'
                    }
                    format_name = format_map.get(ext, 'unknown')

                    # 记录文件信息
                    file_info_dict = {
                        'filename': filename_only,
                        'original_path': filename,
                        'extracted_path': extract_path,
                        'size': file_info.file_size,
                        'format': format_name,
                        'error': None,
                        'extract_time': time.time()
                    }
                    extracted_files.append(file_info_dict)

                except Exception as e:
                    # 记录失败文件
                    file_info_dict = {
                        'filename': file_info.filename,
                        'original_path': file_info.filename,
                        'extracted_path': None,
                        'size': file_info.file_size,
                        'format': 'unknown',
                        'error': str(e),
                        'extract_time': time.time()
                    }
                    extracted_files.append(file_info_dict)

        return {
            'success': True,
            'total_files': total_files,
            'extracted_files': len([f for f in extracted_files if not f.get('error')]),
            'failed_files': len([f for f in extracted_files if f.get('error')]),
            'files': extracted_files,
            'temp_dir': temp_dir
        }

    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'files': []
        }

# 创建一个简化的解压器对象
class SimpleArchiveExtractor:
    def __init__(self):
        pass

    def is_supported_format(self, filename):
        if not filename:
            return False
        ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''
        return ext in ['.zip']

    def extract_archive(self, archive_path, password=None):
        return extract_archive_safe(archive_path, password)

# 替换原来的archive_extractor
archive_extractor = SimpleArchiveExtractor()

app = Flask(__name__)

# 配置
app.config['MAX_CONTENT_LENGTH'] = 100 * 1024 * 1024  # 100MB max file size
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['DOWNLOAD_FOLDER'] = 'downloads'
app.config['SECRET_KEY'] = 'your-secret-key-change-in-production'

# 确保必要的目录存在
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
os.makedirs(app.config['DOWNLOAD_FOLDER'], exist_ok=True)

# 历史记录存储
HISTORY_FILE = 'history.json'
BATCH_STATUS_FILE = 'batch_status.json'

# 全局批量转换状态
batch_conversion_status = {}
batch_lock = threading.Lock()

def load_history():
    """加载历史记录"""
    if os.path.exists(HISTORY_FILE):
        try:
            with open(HISTORY_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except:
            return []
    return []

def save_history(history):
    """保存历史记录"""
    try:
        with open(HISTORY_FILE, 'w', encoding='utf-8') as f:
            json.dump(history, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"保存历史记录失败: {e}")

def add_to_history(original_name, file_format, file_size, md_file_path, download_url):
    """添加转换记录到历史"""
    history = load_history()
    record = {
        'id': uuid.uuid4().hex,
        'original_name': original_name,
        'format': file_format,
        'file_size': file_size,
        'md_file_path': md_file_path,
        'download_url': download_url,
        'converted_at': datetime.datetime.now().isoformat(),
        'status': 'completed'
    }
    history.insert(0, record)  # 添加到开头

    # 只保留最近100条记录
    if len(history) > 100:
        history = history[:100]

    save_history(history)
    return record

def save_batch_status(batch_id, status_data):
    """保存批量转换状态"""
    with batch_lock:
        batch_conversion_status[batch_id] = status_data

        # 可选：持久化到文件
        try:
            with open(BATCH_STATUS_FILE, 'w', encoding='utf-8') as f:
                json.dump(batch_conversion_status, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"保存批量状态失败: {e}")

def get_batch_status(batch_id):
    """获取批量转换状态"""
    with batch_lock:
        return batch_conversion_status.get(batch_id, {})

# 支持的文件类型
SUPPORTED_FORMATS = {
    'pdf': '.pdf',
    'word': '.doc,.docx',
    'excel': '.xls,.xlsx',
    'ppt': '.ppt,.pptx',
    'image': '.jpg,.jpeg,.png,.gif,.bmp',
    'audio': '.mp3,.wav,.flac,.aac,.ogg,.m4a,.wma',
    'video': '.mp4,.avi,.mov,.mkv,.wmv,.flv,.webm,.m4v,.3gp,.mpg,.mpeg',
    'html': '.html,.htm',
    'csv': '.csv',
    'json': '.json',
    'xml': '.xml',
    'zip': '.zip',
    'rar': '.rar'
}


@app.route('/')
def index():
    """主页"""
    return render_template('index.html')

@app.route('/test_batch')
def test_batch():
    """批量转换测试页面"""
    return send_file('test_batch.html')

@app.route("/imgs/<img_file>", methods=['GET'])
def image(img_file):
    return redirect(f"./imgs/{img_file}")


@app.route('/upload/<format_type>', methods=['POST'])
def upload_file(format_type):
    """上传文件接口"""
    try:
        if format_type not in SUPPORTED_FORMATS:
            return jsonify({
                'success': False,
                'message': f'不支持的格式: {format_type}'
            }), 400

        if 'file' not in request.files:
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        # 验证文件格式
        if not is_supported_file(file.filename, format_type):
            return jsonify({
                'success': False,
                'message': f'文件格式不匹配，期望 {format_type} 格式'
            }), 400

        # 保存上传的文件
        import uuid
        filename = file.filename
        upload_path = os.path.join(app.config['UPLOAD_FOLDER'], f"{uuid.uuid4().hex}_{filename}")
        file.save(upload_path)

        # 获取文件大小
        file_size = os.path.getsize(upload_path)
        return jsonify({
            'success': True,
            'file_id': os.path.basename(upload_path),  # 返回保存后的文件名
            'original_name': filename,
            'file_size': file_size,
            'upload_path': upload_path,
            'message': f'文件 {filename} 上传成功'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'上传失败: {str(e)}'
        }), 500


"""
PDF: PADDLE PPStructureV3
WORD: 
"""
@app.route('/convert/<format_type>', methods=['POST'])
def convert_format(format_type):
    """格式特定的转换接口"""
    try:
        if format_type not in SUPPORTED_FORMATS:
            return jsonify({
                'success': False,
                'message': f'不支持的格式: {format_type}'
            }), 400

        # 获取文件ID
        data = request.get_json()
        if not data or 'file_id' not in data:
            return jsonify({
                'success': False,
                'message': '没有提供文件ID'
            }), 400

        file_id = data['file_id']
        upload_path = os.path.join(app.config['UPLOAD_FOLDER'], file_id)
        # 验证文件是否存在
        if not os.path.exists(upload_path):
            return jsonify({
                'success': False,
                'message': f'文件不存在，请重新上传'
            }), 400

        filename = os.path.basename(upload_path)
        content = ""
        # CSV文件转换逻辑
        if format_type == 'csv':
            # 保存转换后的Markdown文件
            with open(upload_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = csv_converter.csv_converter(content)
        elif format_type == "pdf":
            try:
                # 先尝试使用PaddleOCR转换器
                content = pdf_converter.pdf_converter(upload_path)
            except Exception as e:
                print(f"[信息] PaddleOCR PDF转换失败，尝试原生转换器: {str(e)}")
                try:
                    # 失败时使用原生转换器
                    content = pdf_native_converter(upload_path)
                except Exception as e2:
                    print(f"[错误] 原生PDF转换也失败: {str(e2)}")
                    content = f"# 转换错误\n\nPDF转换失败\n\nPaddleOCR错误: {str(e)}\n原生转换错误: {str(e2)}"
        elif format_type == "image":
            content = img_converter.img_converter(upload_path)
        elif format_type == "json":
            with open(upload_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```json\n" + content + "\n```"
        elif format_type == "xml":
            with open(upload_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```xml\n" + content + "\n```"
        elif format_type == "html":
            with open(upload_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```html\n" + content + "\n```"
        elif format_type == "word":
            content = word_converter(upload_path)
        elif format_type == "ppt":
            try:
                # 尝试使用原生PPT转换器
                content = ppt_native_converter(upload_path)
            except Exception as e:
                print(f"[错误] PPT转换失败: {str(e)}")
                content = f"# 转换错误\n\nPPT转换失败\n\n错误详情: {str(e)}"
        elif format_type == "audio":
            try:
                # 使用音频转换器提取元数据
                content = audio_converter(upload_path)
            except Exception as e:
                print(f"[错误] 音频转换失败: {str(e)}")
                content = f"# 转换错误\n\n音频转换失败\n\n错误详情: {str(e)}"
        elif format_type == "video":
            try:
                # 使用视频转换器提取元数据
                content = video_converter(upload_path)
            except Exception as e:
                print(f"[错误] 视频转换失败: {str(e)}")
                content = f"# 转换错误\n\n视频转换失败\n\n错误详情: {str(e)}"


    except Exception as e:
        print(e)
        return jsonify({
            'success': False,
            'message': f'服务器错误: {str(e)}'
        }), 500
    # 其他格式的占位符

    output_filename = f"{os.path.splitext(filename)[0]}_{uuid.uuid4().hex[:8]}.md"
    output_path = os.path.join(app.config['DOWNLOAD_FOLDER'], output_filename)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(content)
    # 删除临时上传文件
    os.remove(upload_path)

    # 添加到历史记录
    original_filename = os.path.basename(upload_path)
    download_url = f"/download-md?file_path={output_path}&filename={os.path.basename(output_filename)}"
    history_record = add_to_history(
        original_name=original_filename,
        file_format=format_type,
        file_size=os.path.getsize(output_path) if os.path.exists(output_path) else 0,
        md_file_path=output_path,
        download_url=download_url
    )

    return jsonify({
        'success': True,
        'md_file_path': output_path,
        'message': f'成功转换 {filename}',
        'history_id': history_record['id']
    })




@app.route('/download/<filename>')
def download_file(filename):
    """下载单个文件"""
    try:
        file_path = os.path.join(app.config['DOWNLOAD_FOLDER'], filename)
        if not os.path.exists(file_path):
            # 如果文件不存在，创建一个示例文件
            sample_content = f"# 示例文件\n\n这是一个示例下载文件: {filename}"
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(sample_content)

        return send_file(file_path, as_attachment=True)
    except Exception as e:
        abort(500)


@app.route('/download-md')
def download_md():
    """下载MD文件（通过文件路径）"""
    try:
        file_path = request.args.get('file_path')
        filename = request.args.get('filename', 'converted.md')

        if not file_path or not os.path.exists(file_path):
            abort(404)

        return send_file(file_path, as_attachment=True, download_name=filename)
    except Exception as e:
        abort(500)




@app.route('/api/formats')
def get_supported_formats():
    """获取支持的文件格式"""
    return jsonify({
        'supported_formats': SUPPORTED_FORMATS,
        'max_file_size': '100MB',
        'supported_types': list(SUPPORTED_FORMATS.keys())
    })




@app.route('/read-md-file', methods=['POST'])
def read_md_file():
    """读取MD文件内容"""
    try:
        data = request.get_json()
        if not data or 'file_path' not in data:
            return jsonify({
                'success': False,
                'message': '没有提供文件路径'
            }), 400

        file_path = data['file_path']

        # 验证文件是否存在
        if not os.path.exists(file_path):
            return jsonify({
                'success': False,
                'message': f'文件不存在: {file_path}'
            }), 400

        # 读取文件内容
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            # 尝试其他编码
            encodings = ['gbk', 'gb2312', 'utf-8-sig', 'latin1']
            content = None
            for encoding in encodings:
                try:
                    with open(file_path, 'r', encoding=encoding) as f:
                        content = f.read()
                    break
                except UnicodeDecodeError:
                    continue

            if content is None:
                return jsonify({
                    'success': False,
                    'message': '无法读取文件编码'
                }), 400

        return jsonify({
            'success': True,
            'content': content
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'读取文件失败: {str(e)}'
        }), 500


@app.route('/api/history', methods=['GET'])
def get_history():
    """获取转换历史记录"""
    try:
        history = load_history()
        return jsonify({
            'success': True,
            'history': history,
            'total': len(history)
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'获取历史记录失败: {str(e)}'
        }), 500


@app.route('/api/history/clear', methods=['POST'])
def clear_history():
    """清空历史记录"""
    try:
        save_history([])
        return jsonify({
            'success': True,
            'message': '历史记录已清空'
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'清空历史记录失败: {str(e)}'
        }), 500


@app.route('/api/history/<history_id>', methods=['DELETE'])
def delete_history_item(history_id):
    """删除单条历史记录"""
    try:
        history = load_history()
        new_history = [item for item in history if item['id'] != history_id]

        if len(history) == len(new_history):
            return jsonify({
                'success': False,
                'message': '记录不存在'
            }), 404

        save_history(new_history)
        return jsonify({
            'success': True,
            'message': '记录已删除'
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'删除记录失败: {str(e)}'
        }), 500


@app.route('/upload/batch', methods=['POST'])
def upload_batch():
    """批量上传压缩包文件"""
    try:
        print(f"[调试] 批量上传请求被触发")

        if 'file' not in request.files:
            print(f"[调试] 错误: 没有文件字段")
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        file = request.files['file']
        print(f"[调试] 文件名: {file.filename}")

        if file.filename == '':
            print(f"[调试] 错误: 文件名为空")
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        # 获取密码
        password = request.form.get('password', '')
        print(f"[调试] 密码: {password}")

        # 验证文件格式
        is_supported = archive_extractor.is_supported_format(file.filename)
        print(f"[调试] 文件格式支持检查: {file.filename} -> {is_supported}")

        if not is_supported:
            return jsonify({
                'success': False,
                'message': f'文件格式不支持，请上传ZIP文件。当前文件: {file.filename}'
            }), 400

        # 保存上传的压缩包
        batch_id = uuid.uuid4().hex
        archive_filename = f"batch_{batch_id}_{file.filename}"
        archive_path = os.path.join(app.config['UPLOAD_FOLDER'], archive_filename)
        file.save(archive_path)

        # 创建批次状态
        batch_status = {
            'batch_id': batch_id,
            'archive_name': file.filename,
            'archive_path': archive_path,
            'password': password,
            'status': 'uploaded',
            'total_files': 0,
            'extracted_files': 0,
            'failed_files': 0,
            'files': [],
            'extracted_at': None,
            'converted_at': None,
            'created_at': datetime.datetime.now().isoformat()
        }

        save_batch_status(batch_id, batch_status)

        return jsonify({
            'success': True,
            'batch_id': batch_id,
            'archive_name': file.filename,
            'file_size': os.path.getsize(archive_path),
            'message': f'压缩包 {file.filename} 上传成功'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'上传失败: {str(e)}'
        }), 500


@app.route('/extract/batch/<batch_id>', methods=['POST'])
def extract_batch(batch_id):
    """解压批量上传的压缩包"""
    try:
        batch_status = get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        if batch_status['status'] != 'uploaded':
            return jsonify({
                'success': False,
                'message': f'批次状态错误: {batch_status["status"]}'
            }), 400

        # 更新状态为解压中
        batch_status['status'] = 'extracting'
        save_batch_status(batch_id, batch_status)

        try:
            # 解压文件
            extract_result = archive_extractor.extract_archive(
                batch_status['archive_path'],
                batch_status['password'] or None
            )

            if extract_result['success']:
                # 更新批次状态
                batch_status.update({
                    'status': 'extracted',
                    'total_files': extract_result['total_files'],
                    'extracted_files': extract_result['extracted_files'],
                    'failed_files': extract_result['failed_files'],
                    'files': extract_result['files'],
                    'temp_dir': extract_result['temp_dir'],
                    'extracted_at': datetime.datetime.now().isoformat()
                })

                save_batch_status(batch_id, batch_status)

                return jsonify({
                    'success': True,
                    'message': f'解压完成，共 {extract_result["total_files"]} 个文件',
                    'total_files': extract_result['total_files'],
                    'extracted_files': extract_result['extracted_files'],
                    'failed_files': extract_result['failed_files'],
                    'files': extract_result['files']
                })
            else:
                raise Exception('解压失败')

        except Exception as e:
            # 更新状态为解压失败
            batch_status['status'] = 'extract_failed'
            batch_status['error'] = str(e)
            save_batch_status(batch_id, batch_status)
            raise e

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'解压失败: {str(e)}'
        }), 500


@app.route('/convert/batch/<batch_id>', methods=['POST'])
def convert_batch(batch_id):
    """异步批量转换文件"""
    try:
        batch_status = get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        if batch_status['status'] != 'extracted':
            return jsonify({
                'success': False,
                'message': f'批次状态错误: {batch_status["status"]}'
            }), 400

        # 获取要转换的文件列表
        data = request.get_json() or {}
        selected_files = data.get('files', [])

        # 如果没有指定文件，则转换所有成功的文件
        if not selected_files:
            selected_files = [f for f in batch_status['files'] if f.get('extracted_path') and not f.get('error')]

        # 更新状态为转换中
        batch_status['status'] = 'converting'
        batch_status['converted_at'] = datetime.datetime.now().isoformat()
        batch_status['conversion_progress'] = {
            'total': len(selected_files),
            'completed': 0,
            'failed': 0,
            'processing': 0
        }
        save_batch_status(batch_id, batch_status)

        # 启动异步转换线程
        def async_convert():
            try:
                convert_batch_files(batch_id, selected_files)
            except Exception as e:
                # 更新错误状态
                batch_status = get_batch_status(batch_id)
                batch_status['status'] = 'conversion_failed'
                batch_status['error'] = str(e)
                save_batch_status(batch_id, batch_status)

        thread = threading.Thread(target=async_convert)
        thread.daemon = True
        thread.start()

        return jsonify({
            'success': True,
            'message': '批量转换已开始',
            'total_files': len(selected_files)
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'启动批量转换失败: {str(e)}'
        }), 500


def convert_batch_files(batch_id, files):
    """异步执行批量文件转换"""
    batch_status = get_batch_status(batch_id)

    for i, file_info in enumerate(files):
        try:
            print(f"[批量转换] 开始处理文件 {i+1}/{len(files)}: {file_info['filename']}")

            if not file_info.get('extracted_path') or file_info.get('error'):
                print(f"[批量转换] 跳过文件 {file_info['filename']} - 无路径或存在错误: {file_info.get('error')}")
                continue

            # 找到全局状态中对应的文件对象并更新
            global_file = None
            for f in batch_status['files']:
                if f['filename'] == file_info['filename']:
                    global_file = f
                    break

            if not global_file:
                print(f"[批量转换] ❌ 找不到全局文件对象: {file_info['filename']}")
                continue

            # 更新文件状态为处理中
            global_file['conversion_status'] = 'processing'
            global_file['conversion_progress'] = 0
            print(f"[批量转换] 更新文件状态为处理中: {global_file['filename']}")
            save_batch_status(batch_id, batch_status)

            # 更新整体进度
            batch_status['conversion_progress']['processing'] += 1
            save_batch_status(batch_id, batch_status)

            # 执行转换
            file_path = global_file['extracted_path']
            file_format = global_file['format']
            filename = global_file['filename']

            print(f"[批量转换] 开始转换文件: {filename} (格式: {file_format}, 路径: {file_path})")

            if file_format not in SUPPORTED_FORMATS:
                print(f"[批量转换] ❌ 不支持的文件格式: {file_format}")
                # 更新文件状态为失败
                global_file['conversion_status'] = 'failed'
                global_file['conversion_error'] = f'不支持的文件格式: {file_format}'
                batch_status['conversion_progress']['failed'] += 1
                batch_status['conversion_progress']['processing'] -= 1
                save_batch_status(batch_id, batch_status)
                continue

            print(f"[批量转换] 调用转换函数...")
            content = convert_file_content(file_path, file_format)
            print(f"[批量转换] 转换完成，内容长度: {len(content)} 字符")

            # 保存转换后的文件
            output_filename = f"{os.path.splitext(filename)[0]}_{uuid.uuid4().hex[:8]}.md"
            output_path = os.path.join(app.config['DOWNLOAD_FOLDER'], output_filename)

            print(f"[批量转换] 保存文件到: {output_path}")
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write(content)

            # 创建下载链接
            download_url = f"/download-md?file_path={output_path}&filename={output_filename}"
            print(f"[批量转换] 创建下载链接: {download_url}")

            # 更新文件状态为完成
            global_file['conversion_status'] = 'completed'
            global_file['conversion_progress'] = 100
            global_file['md_file_path'] = output_path
            global_file['download_url'] = download_url
            global_file['converted_at'] = datetime.datetime.now().isoformat()
            print(f"[批量转换] ✅ 文件转换完成: {filename}")

            # 添加到历史记录
            print(f"[批量转换] 添加到历史记录...")
            add_to_history(
                original_name=filename,
                file_format=file_format,
                file_size=os.path.getsize(output_path),
                md_file_path=output_path,
                download_url=download_url
            )

            # 更新整体进度
            batch_status['conversion_progress']['completed'] += 1
            batch_status['conversion_progress']['processing'] -= 1
            save_batch_status(batch_id, batch_status)
            print(f"[批量转换] 更新进度: 完成 {batch_status['conversion_progress']['completed']}, 失败 {batch_status['conversion_progress']['failed']}, 处理中 {batch_status['conversion_progress']['processing']}")
            print(f"[批量转换] 🔄 保存状态后，文件 {filename} 的状态: {global_file['conversion_status']}")

        except Exception as e:
            print(f"[批量转换] ❌ 文件 {file_info.get('filename', 'unknown')} 处理失败: {str(e)}")

            # 找到全局文件对象并更新失败状态
            global_file = None
            for f in batch_status.get('files', []):
                if f['filename'] == file_info.get('filename', ''):
                    global_file = f
                    break

            if global_file:
                global_file['conversion_status'] = 'failed'
                global_file['conversion_error'] = str(e)
                # 更新整体进度
                batch_status['conversion_progress']['failed'] += 1
                batch_status['conversion_progress']['processing'] -= 1
                save_batch_status(batch_id, batch_status)

    # 标记批次转换完成
    batch_status = get_batch_status(batch_id)
    batch_status['status'] = 'completed'
    save_batch_status(batch_id, batch_status)
    print(f"[批量转换] ✅ 批次转换完成，最终状态: {batch_status['status']}")


def convert_file_content(file_path, file_format):
    """转换单个文件内容"""
    if file_format == 'csv':
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        return csv_converter.csv_converter(content)
    elif file_format == "pdf":
        try:
            content = pdf_converter.pdf_converter(file_path)
        except Exception as e:
            print(f"[信息] PaddleOCR PDF转换失败，尝试原生转换器: {str(e)}")
            try:
                content = pdf_native_converter(file_path)
            except Exception as e2:
                print(f"[错误] 原生PDF转换也失败: {str(e2)}")
                content = f"# 转换错误\n\nPDF转换失败\n\nPaddleOCR错误: {str(e)}\n原生转换错误: {str(e2)}"
    elif file_format == "image":
        content = img_converter.img_converter(file_path)
    elif file_format == "json":
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = "```json\n" + content + "\n```"
    elif file_format == "xml":
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = "```xml\n" + content + "\n```"
    elif file_format == "html":
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = "```html\n" + content + "\n```"
    elif file_format == "word":
        content = word_converter(file_path)
    elif file_format == "ppt":
        try:
            content = ppt_native_converter(file_path)
        except Exception as e:
            print(f"[错误] PPT转换失败: {str(e)}")
            content = f"# 转换错误\n\nPPT转换失败\n\n错误详情: {str(e)}"
    elif file_format == "audio":
        try:
            content = audio_converter(file_path)
        except Exception as e:
            print(f"[错误] 音频转换失败: {str(e)}")
            content = f"# 转换错误\n\n音频转换失败\n\n错误详情: {str(e)}"
    elif file_format == "video":
        try:
            content = video_converter(file_path)
        except Exception as e:
            print(f"[错误] 视频转换失败: {str(e)}")
            content = f"# 转换错误\n\n视频转换失败\n\n错误详情: {str(e)}"
    else:
        content = f"# 不支持的格式\n\n文件格式 {file_format} 暂不支持转换"

    return content


@app.route('/status/batch/<batch_id>', methods=['GET'])
def get_batch_conversion_status(batch_id):
    """获取批量转换状态"""
    try:
        batch_status = get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        return jsonify({
            'success': True,
            'status': batch_status
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'获取状态失败: {str(e)}'
        }), 500




def format_file_size(bytes_size):
    """格式化文件大小"""
    if bytes_size == 0:
        return "0 B"
    k = 1024
    sizes = ["B", "KB", "MB", "GB"]
    i = 0
    while bytes_size >= k and i < len(sizes) - 1:
        bytes_size /= k
        i += 1
    return f"{bytes_size:.2f} {sizes[i]}"


def detect_file_format(filename):
    """根据文件扩展名检测格式"""
    if not filename:
        return None

    ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''

    for format_type, extensions in SUPPORTED_FORMATS.items():
        if ext in extensions.split(','):
            return format_type

    return None


def is_supported_file(filename, format_type):
    """检查文件是否支持该格式"""
    if format_type not in SUPPORTED_FORMATS:
        return False

    ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''
    return ext in SUPPORTED_FORMATS[format_type].split(',')


@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': '文件未找到'}), 404


@app.errorhandler(400)
def bad_request(error):
    return jsonify({'error': '请求错误'}), 400


@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': '服务器内部错误'}), 500


if __name__ == '__main__':
    # 开发环境配置
    app.run(debug=True, host='0.0.0.0', port=5000)
