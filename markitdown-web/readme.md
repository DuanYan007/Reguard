# MarkItDown Web 应用

基于 Flask 的 Web 应用，提供可视化界面和批量处理能力，支持将多种格式文件转换为 Markdown 文档。

## 功能特性

- 支持多种文件格式：PDF, PPT, Word, Excel, 图片, 音频, HTML, CSV, JSON, XML, ZIP 等
- 拖拽上传界面
- 批量文件处理（ZIP 压缩包）
- 实时转换进度显示
- 在线预览转换结果
- 转换历史管理
- 动态配置管理
- 响应式设计，支持移动端

---

## 快速开始

### 1. 安装 PaddleOCR

```bash
# 50系显卡
python -m pip install paddlepaddle-gpu==3.2.1 -i https://www.paddlepaddle.org.cn/packages/stable/cu129/
python -m pip install -U "paddleocr[doc-parser]"

# 对于 Linux 系统，执行：
python -m pip install https://paddle-whl.bj.bcebos.com/nightly/cu126/safetensors/safetensors-0.6.2.dev0-cp38-abi3-linux_x86_64.whl

# 对于 Windows 系统，执行：
python -m pip install https://xly-devops.cdn.bcebos.com/safetensors-nightly/safetensors-0.6.2.dev0-cp38-abi3-win_amd64.whl
```

### 2. 安装项目依赖

```bash
cd markitdown-web/conveter
pip install -r requirements.txt
```

### 3. 启动应用

```bash
python app.py
```

访问 http://localhost:5000

---

## 🖼️ 功能演示

### 主界面预览

![主界面](images/web-interface-demo.png)

主界面提供简洁的文件上传和转换功能

### 核心功能

#### 📁 智能文件上传
- 拖拽上传支持，格式自动识别
- 实时上传进度显示
- 多文件批量上传
- 文件大小和格式验证

![上传演示](images/upload-demo.png)

#### 🔄 多格式转换引擎
- 支持 PDF、Word、Excel、PPT 等 12+ 种文件格式
- PaddleOCR 智能版面分析
- 表格智能识别和重建
- 高保真格式转换

#### 👁️ 实时预览系统
- 转换完成后立即预览 Markdown 效果
- 图片路径智能处理
- 格式化内容显示
- 支持原始 Markdown 和渲染预览

![Word转换示例](images/test_docx.png)
![Markdown预览](images/test_docx_md.png)

#### 📋 ZIP 批量转换
- 上传 ZIP 压缩包进行批量转换
- 实时进度监控和状态显示
- 转换失败文件错误报告
- 批量结果管理

![批量转换1](images/batch_1.png)
![批量转换2](images/batch_2.png)
![批量转换3](images/batch_3.png)

#### 🕐 转换历史追踪
- 完整的转换历史记录
- 一键重新下载功能
- 历史记录搜索和过滤
- 批量管理操作

![历史记录](images/history-demo.png)

#### 🔧 动态配置管理
- 存储路径动态配置
- 文件迁移原子操作
- 配置热更新生效
- 系统参数调优

![配置界面](images/config-interface-demo.png)

#### 📄 智能文档解析
- PaddleOCR PP-StructureV3 版面分析
- 表格智能识别和重建
- 图片提取和 OCR 识别
- 复杂文档结构保持

#### 🔍 高精度文字识别
- PaddleOCR 高精度识别
- 中英文混合识别支持
- 多种图片格式支持
- 识别结果结构化输出

![图片OCR](images/test_png.png)
![OCR结果](images/test_png_md.png)

---

## 使用指南

### 单个文件转换

1. 访问 http://localhost:5000
2. 拖拽文件到上传区域，或点击选择文件
3. 系统自动识别格式并转换
4. 在网页中实时查看转换效果
5. 点击下载按钮获取 Markdown 文件

### 批量文件转换

1. 点击"批量转换"标签
2. 上传 ZIP 压缩包
3. 系统自动解压并列出文件
4. 选择需要转换的文件
5. 点击开始批量转换
6. 实时查看转换进度
7. 在历史记录中管理和下载结果

### 配置管理

访问 http://localhost:5000/config 可以：
- 修改存储路径（上传目录、下载目录）
- 调整文件大小限制
- 设置历史记录数量
- 配置会自动保存并立即生效

---

## 支持的文件格式

### 文档格式
- PDF (.pdf) - PaddleOCR 智能识别
- Microsoft Word (.doc, .docx)
- PowerPoint (.ppt, .pptx)
- Excel (.xls, .xlsx)

### 媒体格式
- 图片 (.jpg, .jpeg, .png, .gif, .bmp, .tiff, .webp) - OCR 文字识别
- 音频 (.mp3, .wav, .ogg, .flac, .m4a, .aac) - 元数据提取
- 视频 (.mp4, .avi, .mov, .mkv, .wmv, .flv, .webm, .m4v) - 元数据提取

### 数据格式
- HTML (.html, .htm)
- CSV (.csv)
- JSON (.json)
- XML (.xml)
- ZIP (.zip) - 批量处理

---

## 项目结构

```
conveter/
├── app.py                 # Flask 主应用文件
├── requirements.txt       # Python 依赖
├── static/               # 静态文件目录
│   ├── css/
│   │   └── style.css     # 自定义样式
│   └── js/
│       └── main.js       # 前端 JavaScript
├── templates/            # HTML 模板目录
│   ├── index.html        # 主页面模板
│   └── config.html       # 配置管理页面
├── converters/           # 转换器模块
│   ├── pdf_converter.py
│   ├── word_converter.py
│   ├── img_converter.py
│   └── ...
├── config_manager.py     # 配置管理器
├── file_migrator.py      # 文件迁移器
├── uploads/              # 上传文件临时目录
└── downloads/            # 转换结果目录
```

---

## API 接口

### 文件上传
- **URL**: `/upload/<format_type>`
- **方法**: POST
- **参数**: `file` (文件)
- **返回**: 上传成功的文件信息

### 文件转换
- **URL**: `/convert/<format_type>`
- **方法**: POST
- **参数**: `file_id` (文件ID)
- **返回**: 转换结果

### 文件下载
- **URL**: `/download-md?file_path=xxx&filename=xxx`
- **方法**: GET
- **功能**: 下载转换后的文件

### 批量转换
- **URL**: `/upload/batch`, `/extract/batch/<id>`, `/convert/batch/<id>`
- **方法**: POST
- **功能**: 批量上传、解压、转换

### 转换历史
- **URL**: `/api/history`
- **方法**: GET
- **返回**: 历史记录列表

### 配置管理
- **URL**: `/api/config`
- **方法**: GET, PUT
- **功能**: 获取和更新配置

---

## 技术架构

- **Flask** - 轻量级 Web 框架
- **PaddleOCR PP-StructureV3** - 先进的 OCR 和文档版面分析
- **文件迁移系统** - 原子性文件操作和配置热更新
- **图片路径处理** - 智能处理网页预览和本地下载的路径兼容性

---

## 配置说明

### 环境变量

可在 `app.py` 或环境变量中配置：

- `MAX_CONTENT_LENGTH` - 最大文件上传大小（默认 100MB）
- `UPLOAD_FOLDER` - 上传文件临时目录
- `DOWNLOAD_FOLDER` - 转换结果目录
- `SECRET_KEY` - Flask 密钥

### 动态配置

访问配置管理页面（`/config`）可以动态修改：
- 存储路径
- 文件大小限制
- 历史记录数量
- 系统参数

配置修改后会自动保存并立即生效，支持文件迁移。

---

## 使用场景

### 文档转换场景
- **学术论文**：PDF 论文转 Markdown，保留公式和表格结构
- **技术文档**：Word/PowerPoint 技术文档转换为纯文本格式
- **合同文件**：扫描版合同 PDF 通过 OCR 转为可编辑文本
- **会议资料**：批量转换 PPT、Word 等会议材料

### 技术应用场景
- **AI 语料准备**：为大语言模型准备高质量的训练数据
- **文档归档**：将各种格式文档统一为 Markdown 格式存储
- **内容提取**：从 PDF、图片中提取结构化文本内容
- **批量处理**：企业级文档批量转换需求

---

## 安全注意事项

1. 在生产环境中修改 `SECRET_KEY`
2. 限制文件上传大小和类型
3. 定期清理临时文件
4. 添加用户认证和权限控制
5. 使用 HTTPS 部署

---

## 许可证

MIT License
