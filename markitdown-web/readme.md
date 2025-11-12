# 通用文件转换器

一个基于Flask的Web应用，支持将多种格式文件转换为Markdown文档。

## 功能特性

- 支持多种文件格式：PDF, PPT, Word, Excel, 图片, 音频, HTML, CSV, JSON, XML, ZIP等
- 拖拽上传界面
- 批量文件处理
- 实时转换进度显示
- 单个或批量下载转换结果
- 响应式设计，支持移动端

## 项目结构

```
conveter/
├── app.py                 # Flask主应用文件
├── requirements.txt       # Python依赖
├── README.md             # 项目说明
├── static/               # 静态文件目录
│   ├── css/
│   │   └── style.css     # 自定义样式
│   ├── js/
│   │   └── main.js       # 前端JavaScript
│   └── images/           # 图片资源
├── templates/            # HTML模板目录
│   └── index.html        # 主页面模板
├── uploads/              # 上传文件临时目录
└── downloads/            # 转换结果目录
```

## 安装和运行

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 运行应用

```bash
python app.py
```

应用将在 `http://localhost:5000` 启动。

## 支持的文件格式

### 文档格式
- PDF (.pdf)
- Microsoft Word (.doc, .docx)
- PowerPoint (.ppt, .pptx)
- Excel (.xls, .xlsx)

### 媒体格式
- 图片 (.jpg, .jpeg, .png, .gif, .bmp)
- 音频 (.mp3, .wav, .ogg)
- 视频 (.mp4, .avi, .mov)

### 数据格式
- HTML (.html, .htm)
- CSV (.csv)
- JSON (.json)
- XML (.xml)
- ZIP (.zip)

## API接口

### 文件转换
- **URL**: `/convert`
- **方法**: POST
- **参数**: `files[]` (文件数组)
- **返回**: JSON格式的转换结果

### 文件下载
- **URL**: `/download/<filename>`
- **方法**: GET
- **功能**: 下载单个转换后的文件

### 批量下载
- **URL**: `/download-batch`
- **方法**: GET
- **参数**: `ids` (文件ID数组，逗号分隔)
- **功能**: 打包下载多个文件

### 支持格式查询
- **URL**: `/api/formats`
- **方法**: GET
- **返回**: 支持的文件格式信息

## 实现转换逻辑

当前版本提供了完整的框架，转换逻辑为占位符实现。要实现具体的转换功能，需要在 `app.py` 的 `FileConverter.convert_file` 方法中添加相应的转换代码。

### 建议的依赖库

根据需要转换的文件类型，可以添加以下库：

```bash
# PDF处理
pip install PyPDF2 pdfplumber

# Word文档
pip install python-docx

# Excel文件
pip install openpyxl pandas

# PowerPoint
pip install python-pptx

# 图片OCR
pip install Pillow pytesseract

# 音频识别
pip install SpeechRecognition

# HTML解析
pip install beautifulsoup4

# 其他格式
pip install chardet lxml
```

### 转换示例

```python
def convert_pdf_to_markdown(self, file_path):
    """PDF转Markdown示例"""
    import PyPDF2

    content = ""
    with open(file_path, 'rb') as file:
        reader = PyPDF2.PdfReader(file)
        for page in reader.pages:
            content += page.extract_text() + "\n\n"

    return content

def convert_docx_to_markdown(self, file_path):
    """Word文档转Markdown示例"""
    from docx import Document

    doc = Document(file_path)
    content = ""

    for paragraph in doc.paragraphs:
        if paragraph.style.name == 'Heading 1':
            content += f"# {paragraph.text}\n\n"
        elif paragraph.style.name == 'Heading 2':
            content += f"## {paragraph.text}\n\n"
        else:
            content += f"{paragraph.text}\n\n"

    return content
```

## 配置说明

在 `app.py` 中可以修改以下配置：

- `MAX_CONTENT_LENGTH`: 最大文件上传大小 (默认100MB)
- `UPLOAD_FOLDER`: 上传文件临时目录
- `DOWNLOAD_FOLDER`: 转换结果目录
- `SECRET_KEY`: Flask密钥

## 安全注意事项

1. 在生产环境中修改 `SECRET_KEY`
2. 限制文件上传大小和类型
3. 定期清理临时文件
4. 添加用户认证和权限控制

## 许可证

MIT License
