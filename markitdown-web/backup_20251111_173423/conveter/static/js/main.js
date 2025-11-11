// 增强版文件转换器JavaScript - 支持动态配置和热重载
class FileConverter {
    constructor() {
        this.currentFormat = null;
        this.currentSingleFile = null;
        this.currentPreviewData = null;
        this.previewMode = 'render';

        // 状态管理
        this.isConverting = false;
        this.isUploading = false;

        // 批量转换状态
        this.currentBatchId = null;
        this.currentBatchFiles = [];
        this.batchStatus = null;
        this.batchStatusInterval = null;

        // 预览状态管理 - 记录预览前的页面上下文
        this.previewSource = null; // 'single' | 'batch' | 'history'

        // 配置管理
        this.appConfig = {};
        this.configWatchInterval = null;

        this.init();
    }

    init() {
        console.log('FileConverter.init() 开始');
        this.setupEventListeners();
        console.log('事件监听器设置完成');
        this.loadAppConfig().then(() => {
            this.startConfigWatching();
            this.showWelcome();
            console.log('FileConverter.init() 完成');
        });
    }

    // 配置管理方法
    async loadAppConfig() {
        try {
            const response = await fetch('/api/config');
            const result = await response.json();

            if (result.success) {
                this.appConfig = result.config;
                console.log('应用配置加载成功:', this.appConfig);
                this.applyConfig();
                return true;
            } else {
                console.error('加载应用配置失败:', result.message);
                return false;
            }
        } catch (error) {
            console.error('加载应用配置异常:', error);
            return false;
        }
    }

    async updateAppConfig(configUpdates) {
        try {
            const response = await fetch('/api/config', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(configUpdates)
            });

            const result = await response.json();

            if (result.success) {
                console.log('应用配置更新成功');
                await this.loadAppConfig(); // 重新加载配置
                this.showNotification('配置更新成功', 'success');
                return true;
            } else {
                console.error('更新应用配置失败:', result.message);
                this.showNotification('配置更新失败: ' + result.message, 'error');
                return false;
            }
        } catch (error) {
            console.error('更新应用配置异常:', error);
            this.showNotification('配置更新异常: ' + error.message, 'error');
            return false;
        }
    }

    async reloadAppConfig() {
        try {
            const response = await fetch('/api/config/reload', {
                method: 'POST'
            });

            const result = await response.json();

            if (result.success) {
                console.log('应用配置重新加载成功');
                await this.loadAppConfig();
                this.showNotification('配置重新加载成功', 'success');
                return true;
            } else {
                console.error('重新加载应用配置失败:', result.message);
                this.showNotification('配置重新加载失败: ' + result.message, 'error');
                return false;
            }
        } catch (error) {
            console.error('重新加载应用配置异常:', error);
            this.showNotification('配置重新加载异常: ' + error.message, 'error');
            return false;
        }
    }

    applyConfig() {
        // 简化配置应用 - 由于配置已简化，大部分功能使用默认值

        // 设置通知持续时间（使用默认值）
        this.notificationDuration = 5000; // 5秒默认值

        // 所有功能默认启用，无需检查feature_flags

        console.log('配置应用完成（使用简化配置）');
    }

    toggleFeatures(features) {
        // 简化版功能切换 - 所有功能默认启用，无需配置
        // 由于简化配置，所有功能都保持启用状态
        console.log('功能切换已简化，所有功能默认启用');
    }

    startConfigWatching() {
        // 每30秒检查一次配置变化
        this.configWatchInterval = setInterval(() => {
            this.checkConfigChanges();
        }, 30000);
    }

    stopConfigWatching() {
        if (this.configWatchInterval) {
            clearInterval(this.configWatchInterval);
            this.configWatchInterval = null;
        }
    }

    async checkConfigChanges() {
        try {
            const response = await fetch('/api/config');
            const result = await response.json();

            if (result.success) {
                const newConfig = result.config;

                // 简单的变化检测（比较JSON字符串）
                if (JSON.stringify(newConfig) !== JSON.stringify(this.appConfig)) {
                    console.log('检测到配置变化，重新加载...');
                    this.appConfig = newConfig;
                    this.applyConfig();
                    this.showNotification('配置已自动更新', 'info');
                }
            }
        } catch (error) {
            console.error('检查配置变化失败:', error);
        }
    }

    getConfig(path, defaultValue = null) {
        // 获取嵌套配置值，例如 getConfig('ui.page_title')
        const keys = path.split('.');
        let value = this.appConfig;

        for (const key of keys) {
            if (value && typeof value === 'object' && key in value) {
                value = value[key];
            } else {
                return defaultValue;
            }
        }

        return value !== undefined ? value : defaultValue;
    }

    setupEventListeners() {
        console.log('设置事件监听器...');

        // 单文件上传
        const singleFileInput = document.getElementById('singleFileInput');
        const singleUploadArea = document.getElementById('singleUploadArea');
        const selectFileButton = document.getElementById('selectFileButton');

        console.log('singleFileInput:', singleFileInput);
        console.log('singleUploadArea:', singleUploadArea);
        console.log('selectFileButton:', selectFileButton);

        // 批量上传事件监听器
        const batchFileInput = document.getElementById('batchFileInput');
        const batchUploadArea = document.getElementById('batchUploadArea');
        const selectBatchFileButton = document.getElementById('selectBatchFileButton');

        console.log('batchFileInput:', batchFileInput);
        console.log('batchUploadArea:', batchUploadArea);
        console.log('selectBatchFileButton:', selectBatchFileButton);

        // 批量文件选择事件
        if (batchFileInput) {
            batchFileInput.addEventListener('change', (e) => {
                console.log('=== 批量文件选择事件触发 ===');
                if (e.target.files.length > 0) {
                    const file = e.target.files[0];
                    console.log('选择的压缩包:', {
                        name: file.name,
                        size: file.size,
                        type: file.type
                    });

                    this.handleBatchArchive(file);
                }
            });
        }

        // 批量上传按钮点击事件
        if (selectBatchFileButton) {
            selectBatchFileButton.addEventListener('click', (e) => {
                console.log('批量上传按钮被点击');
                e.preventDefault();
                e.stopPropagation();

                if (batchFileInput && !batchFileInput.disabled) {
                    console.log('触发批量文件选择对话框');
                    batchFileInput.click();
                } else {
                    console.warn('批量文件输入框不可用');
                }
            });
        }

        // 批量上传区域拖拽事件
        if (batchUploadArea) {
            batchUploadArea.addEventListener('dragover', (e) => {
                e.preventDefault();
                batchUploadArea.classList.add('dragover');
            });

            batchUploadArea.addEventListener('dragleave', (e) => {
                e.preventDefault();
                batchUploadArea.classList.remove('dragover');
            });

            batchUploadArea.addEventListener('drop', (e) => {
                e.preventDefault();
                batchUploadArea.classList.remove('dragover');

                const files = e.dataTransfer.files;
                if (files.length > 0) {
                    const file = files[0];
                    if (this.isArchiveFile(file.name)) {
                        this.handleBatchArchive(file);
                    } else {
                        this.showNotification('请上传ZIP格式的压缩包', 'error');
                    }
                }
            });

            batchUploadArea.addEventListener('click', () => {
                if (batchFileInput && !batchFileInput.disabled) {
                    batchFileInput.click();
                }
            });
        }

        // 文件选择事件
        singleFileInput.addEventListener('change', (e) => {
            console.log('=== 文件选择事件触发 ===');
            console.log('事件对象:', e);
            console.log('目标元素:', e.target);
            console.log('文件列表:', e.target.files);
            console.log('文件数量:', e.target.files.length);

            if (e.target.files.length > 0) {
                const file = e.target.files[0];
                console.log('选择的文件:', {
                    name: file.name,
                    size: file.size,
                    type: file.type,
                    lastModified: file.lastModified
                });

                // 立即处理文件
                this.handleSingleFile(file);
            } else {
                console.log('没有选择文件');
            }
        });

        // 简化事件处理 - 只给按钮添加点击事件
        if (selectFileButton) {
            selectFileButton.addEventListener('click', (e) => {
                console.log('选择文件按钮被点击');
                e.preventDefault();
                e.stopPropagation();

                if (singleFileInput && !singleFileInput.disabled) {
                    console.log('触发文件选择对话框');
                    singleFileInput.click();
                } else {
                    console.warn('文件输入框不可用');
                }
            });
        }
    }

    // 页面导航方法
    showWelcome() {
        // 检查是否正在转换中
        if (this.isConverting || this.isUploading) {
            this.showNotification('正在处理文件，请等待完成', 'warning');
            return;
        }

        // 重置所有状态
        this.isConverting = false;
        this.isUploading = false;
        this.currentSingleFile = null;
        this.currentPreviewData = null;
        this.currentFormat = null;

        // 重置界面状态
        this.setUploadAreaState(false);
        this.setConvertButtonState(false);
        this.hideProgress();

        // 清空文件输入
        const singleFileInput = document.getElementById('singleFileInput');
        if (singleFileInput) {
            singleFileInput.value = '';
        }

        this.hideAllPages();
        document.getElementById('welcomePage').style.display = 'block';
        this.clearActiveFormats();

        console.log('状态已重置，返回欢迎页面');
    }

    showUploadArea(format) {
        // 检查是否正在转换中
        if (this.isConverting || this.isUploading) {
            this.showNotification('正在处理文件，请等待完成', 'warning');
            return;
        }

        this.currentFormat = format;
        this.hideAllPages();
        document.getElementById('uploadPage').style.display = 'block';

        // 更新页面标题和格式提示
        this.updateFormatInfo(format);

        // 设置活动格式
        this.setActiveFormat(format);

        // 清空之前的文件
        this.clearSingleFile();
    }

    showPreview(data, source = 'auto') {
        this.currentPreviewData = data;

        // 如果没有明确指定来源，则检测当前页面上下文
        if (source === 'auto') {
            const batchUploadPage = document.getElementById('batchUploadPage');
            const historyPage = document.getElementById('historyPage');

            if (batchUploadPage && batchUploadPage.style.display !== 'none') {
                this.previewSource = 'batch';
                console.log('自动检测预览来源：批量转换页面');
            } else if (historyPage && historyPage.style.display !== 'none') {
                this.previewSource = 'history';
                console.log('自动检测预览来源：历史记录页面');
            } else {
                this.previewSource = 'single';
                console.log('自动检测预览来源：单文件转换页面');
            }
        } else {
            // 使用明确指定的来源
            this.previewSource = source;
            console.log(`明确指定预览来源: ${source}`);
        }

        console.log(`最终确定的预览来源: ${this.previewSource}`);

        this.hideAllPages();
        document.getElementById('previewPage').style.display = 'block';

        // 更新预览标题
        document.getElementById('previewTitle').textContent = `转换结果 - ${data.originalName}`;
        document.getElementById('previewInfo').textContent = `文件大小: ${data.originalSize} | 转换时间: ${data.convertTime}`;

        // 渲染预览内容
        this.renderPreview(data.content);
    }

    hideAllPages() {
        const pages = ['welcomePage', 'uploadPage', 'previewPage', 'historyPage', 'batchUploadPage'];
        pages.forEach(pageId => {
            document.getElementById(pageId).style.display = 'none';
        });
    }

    // 格式分类管理
    showFormatCategory(category) {
        const items = document.getElementById(`${category}-items`);
        const arrow = event.currentTarget.querySelector('.category-arrow');
        const header = event.currentTarget;

        if (items.classList.contains('show')) {
            items.classList.remove('show');
            arrow.classList.remove('rotated');
            header.classList.remove('active');
        } else {
            // 关闭其他分类
            document.querySelectorAll('.category-items').forEach(item => {
                item.classList.remove('show');
            });
            document.querySelectorAll('.category-arrow').forEach(arrow => {
                arrow.classList.remove('rotated');
            });
            document.querySelectorAll('.category-header').forEach(header => {
                header.classList.remove('active');
            });

            // 打开当前分类
            items.classList.add('show');
            arrow.classList.add('rotated');
            header.classList.add('active');
        }
    }

    setActiveFormat(format) {
        document.querySelectorAll('.format-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-format="${format}"]`).classList.add('active');
    }

    clearActiveFormats() {
        document.querySelectorAll('.format-item').forEach(item => {
            item.classList.remove('active');
        });
    }

    updateFormatInfo(format) {
        const formatConfig = this.getFormatConfig(format);

        document.getElementById('formatTitle').textContent = `${formatConfig.title} 转换`;
        document.getElementById('singleFormatHint').textContent = `支持格式: ${formatConfig.extensions.join(', ')}`;

        // 更新文件输入accept属性
        document.getElementById('singleFileInput').accept = formatConfig.accept;
    }

    getFormatConfig(format) {
        const configs = {
            pdf: {
                title: 'PDF文档',
                extensions: ['PDF'],
                accept: '.pdf'
            },
            word: {
                title: 'Word文档',
                extensions: ['DOC', 'DOCX'],
                accept: '.doc,.docx'
            },
            excel: {
                title: 'Excel表格',
                extensions: ['XLS', 'XLSX'],
                accept: '.xls,.xlsx'
            },
            ppt: {
                title: 'PowerPoint演示文稿',
                extensions: ['PPT', 'PPTX'],
                accept: '.ppt,.pptx'
            },
            csv: {
                title: 'CSV文件',
                extensions: ['CSV'],
                accept: '.csv'
            },
            html: {
                title: 'HTML网页',
                extensions: ['HTML', 'HTM'],
                accept: '.html,.htm'
            },
            json: {
                title: 'JSON数据',
                extensions: ['JSON'],
                accept: '.json'
            },
            xml: {
                title: 'XML文档',
                extensions: ['XML'],
                accept: '.xml'
            },
            image: {
                title: '图片文件',
                extensions: ['JPG', 'JPEG', 'PNG', 'GIF', 'BMP'],
                accept: '.jpg,.jpeg,.png,.gif,.bmp'
            },
            audio: {
                title: '音频文件',
                extensions: ['MP3', 'WAV', 'FLAC', 'AAC', 'OGG', 'M4A', 'WMA'],
                accept: '.mp3,.wav,.flac,.aac,.ogg,.m4a,.wma'
            },
            video: {
                title: '视频文件',
                extensions: ['MP4', 'AVI', 'MOV', 'MKV', 'WMV', 'FLV', 'WEBM', 'M4V', '3GP', 'MPG', 'MPEG'],
                accept: '.mp4,.avi,.mov,.mkv,.wmv,.flv,.webm,.m4v,.3gp,.mpg,.mpeg'
            },
                    };

        return configs[format] || configs.csv;
    }

    // 文件处理方法
    async handleSingleFile(file) {
        if (!file) return;

        console.log('=== 开始处理文件 ===');
        console.log('文件名:', file.name);
        console.log('文件大小:', file.size);
        console.log('当前格式:', this.currentFormat);

        // 检查基本条件
        if (!this.currentFormat) {
            console.error('当前格式未设置');
            alert('请先选择文件格式');
            return;
        }

        if (!this.validateFileType(file, this.currentFormat)) {
            console.error('文件类型不匹配');
            alert(`文件类型不支持，请选择 ${this.getFormatConfig(this.currentFormat).extensions.join(', ')} 文件`);
            return;
        }

        console.log('文件验证通过，开始上传...');

        // 创建FormData
        const formData = new FormData();
        formData.append('file', file);

        console.log('上传URL:', `/upload/${this.currentFormat}`);
        console.log('FormData内容:', formData);

        try {
            // 显示上传状态
            this.showNotification('正在上传文件...', 'info');

            console.log('发送fetch请求...');
            const response = await fetch(`/upload/${this.currentFormat}`, {
                method: 'POST',
                body: formData
            });

            console.log('收到响应:', response.status, response.statusText);

            if (!response.ok) {
                throw new Error(`HTTP错误: ${response.status} ${response.statusText}`);
            }

            const result = await response.json();
            console.log('服务器响应:', result);

            if (result.success) {
                console.log('上传成功');

                // 存储文件信息
                this.currentSingleFile = {
                    file_id: result.file_id,
                    name: result.original_name,
                    size: this.formatFileSize(result.file_size),
                    type: this.currentFormat,
                    upload_path: result.upload_path
                };

                console.log('当前文件信息:', this.currentSingleFile);

                // 更新显示
                this.updateSingleFileDisplay();
                this.showNotification('文件上传成功！', 'success');
            } else {
                console.error('上传失败:', result.message);
                throw new Error(result.message || '上传失败');
            }

        } catch (error) {
            console.error('上传过程出错:', error);
            this.showNotification('文件上传失败: ' + error.message, 'error');
            this.clearSingleFile();
        }
    }

    validateFileType(file, format) {
        const formatConfig = this.getFormatConfig(format);
        const extension = '.' + file.name.split('.').pop().toLowerCase();
        return formatConfig.accept.split(',').includes(extension);
    }

    updateSingleFileDisplay() {
        const fileInfo = document.getElementById('singleFileInfo');
        const fileName = document.getElementById('singleFileName');
        const fileSize = document.getElementById('singleFileSize');

        console.log('updateSingleFileDisplay called, fileInfo:', fileInfo);
        console.log('currentSingleFile:', this.currentSingleFile);

        if (this.currentSingleFile && fileInfo) {
            fileName.textContent = this.currentSingleFile.name;
            fileSize.textContent = this.currentSingleFile.size;
            fileInfo.style.display = 'block';

            // 更新图标 - 添加安全检查
            const icon = fileInfo.querySelector('.file-preview-icon');
            console.log('found icon:', icon);
            if (icon) {
                icon.className = this.getFileIconClass(this.currentSingleFile.name);
                console.log('updated icon className to:', this.getFileIconClass(this.currentSingleFile.name));
            } else {
                console.warn('file-preview-icon element not found!');
            }
        } else {
            if (fileInfo) {
                fileInfo.style.display = 'none';
            }
        }
    }

    clearSingleFile() {
        console.log('clearSingleFile called');

        // 先获取DOM元素
        const singleFileInput = document.getElementById('singleFileInput');
        const fileInfo = document.getElementById('singleFileInfo');

        // 清空数据
        this.currentSingleFile = null;

        // 清空文件输入框
        if (singleFileInput) {
            singleFileInput.value = '';
            console.log('singleFileInput.value cleared');
        }

        // 隐藏文件信息显示
        if (fileInfo) {
            fileInfo.style.display = 'none';
            console.log('fileInfo hidden');
        }

        // 重置转换按钮状态
        this.setConvertButtonState(false);

        console.log('clearSingleFile completed');
    }

    // 状态控制方法
    setUploadAreaState(disabled) {
        const singleUploadArea = document.getElementById('singleUploadArea');
        const singleFileInput = document.getElementById('singleFileInput');

        // 设置上传区域的样式和可点击性
        if (singleUploadArea) {
            singleUploadArea.style.opacity = disabled ? '0.5' : '1';
            singleUploadArea.style.pointerEvents = disabled ? 'none' : 'auto';
        }

        // 禁用文件输入
        if (singleFileInput) {
            singleFileInput.disabled = disabled;
        }

        console.log(`Upload area ${disabled ? 'disabled' : 'enabled'}`);
    }

    setConvertButtonState(disabled) {
        // 查找转换按钮
        const convertButtons = document.querySelectorAll('button[onclick*="convertSingle"]');

        convertButtons.forEach(button => {
            button.disabled = disabled;
            button.style.opacity = disabled ? '0.5' : '1';

            // 更新按钮文本和样式
            if (disabled) {
                button.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>转换中...';
                button.classList.remove('btn-success');
                button.classList.add('btn-secondary');
            } else {
                button.innerHTML = '<i class="fas fa-cogs me-1"></i>转换';
                button.classList.remove('btn-secondary');
                button.classList.add('btn-success');
            }
        });

        console.log(`Convert button ${disabled ? 'disabled' : 'enabled'}`);
    }

    // 转换方法
    async convertSingle() {
        // 检查是否正在转换中
        if (this.isConverting) {
            this.showNotification('正在转换中，请等待完成', 'warning');
            return;
        }

        // 检查是否正在上传中
        if (this.isUploading) {
            this.showNotification('正在上传文件，请等待', 'warning');
            return;
        }

        if (!this.currentSingleFile || !this.currentSingleFile.file_id) {
            this.showNotification('请先选择文件', 'warning');
            return;
        }

        // 设置转换状态
        this.isConverting = true;
        this.setUploadAreaState(true); // 禁用上传区域
        this.setConvertButtonState(true); // 禁用转换按钮

        // 显示进度条
        this.showProgress();
        this.updateProgress(0, '准备转换...');

        try {
            // 更新进度：开始发送请求
            this.updateProgress(20, '正在发送转换请求...');

            const response = await fetch(`/convert/${this.currentFormat}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    file_id: this.currentSingleFile.file_id
                })
            });

            // 更新进度：服务器处理中
            this.updateProgress(50, '服务器正在处理文件...');

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            if (result.success) {
                console.log('✅ 单文件转换成功:', result);
                console.log('  md_file_path:', result.md_file_path);
                console.log('  message:', result.message);

                // 更新进度：转换完成，开始读取内容
                this.updateProgress(80, '正在读取转换结果...');

                // 读取MD文件内容
                try {
                    const contentResponse = await fetch(`/read-md-file`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            file_path: result.md_file_path
                        })
                    });

                    if (contentResponse.ok) {
                        const contentResult = await contentResponse.json();
                        console.log('✅ MD文件内容读取成功');

                        // 转换完成
                        this.updateProgress(100, '转换完成！');

                        // 延迟显示预览，让用户看到完成状态
                        setTimeout(() => {
                            this.hideProgress();
                            console.log('调用 showPreview，传入预览来源: single');
                            this.showPreview({
                                originalName: this.currentSingleFile.name,
                                originalSize: this.currentSingleFile.size,
                                content: contentResult.content,
                                mdFilePath: result.md_file_path,
                                convertTime: new Date().toLocaleString(),
                                format: this.currentFormat
                            }, 'single');
                        }, 800);
                    } else {
                        console.error('❌ MD文件内容读取失败:', contentResponse.status, contentResponse.statusText);
                        throw new Error('无法读取转换后的文件');
                    }
                } catch (contentError) {
                    console.error('❌ 内容读取错误:', contentError);
                    this.updateProgress(100, '转换完成，但预览加载失败');
                    setTimeout(() => {
                        this.hideProgress();
                        this.showNotification('转换完成，但预览加载失败', 'warning');
                        // 仍然显示基本预览，即使内容读取失败
                        console.log('调用 showPreview，传入预览来源: single (error case)');
                        this.showPreview({
                            originalName: this.currentSingleFile.name,
                            originalSize: this.currentSingleFile.size,
                            content: `# ${this.currentSingleFile.name}\n\n转换已完成，但内容加载失败。\n\n文件路径: ${result.md_file_path}`,
                            mdFilePath: result.md_file_path,
                            convertTime: new Date().toLocaleString(),
                            format: this.currentFormat
                        }, 'single');
                    }, 800);
                }
            } else {
                console.error('❌ 单文件转换失败:', result.message);
                throw new Error(result.message || '转换失败');
            }

        } catch (error) {
            console.error('Conversion error:', error);
            this.updateProgress(0, '转换失败');
            setTimeout(() => {
                this.hideProgress();
                this.showNotification('转换失败: ' + error.message, 'error');
            }, 500);
        } finally {
            // 清除转换状态
            this.isConverting = false;
            this.setUploadAreaState(false); // 启用上传区域
            this.setConvertButtonState(false); // 启用转换按钮
        }
    }

    // 预览相关方法
    renderPreview(content) {
        const previewContent = document.getElementById('previewContent');
        const previewCode = document.getElementById('previewCode');
        const previewRawContent = document.getElementById('previewRawContent');

        if (this.previewMode === 'render') {
            previewContent.innerHTML = marked.parse(content);
            previewContent.style.display = 'block';
            previewCode.style.display = 'none';
            previewRawContent.style.display = 'none';
        } else {
            previewRawContent.textContent = content;
            previewContent.style.display = 'none';
            previewCode.style.display = 'block';
            previewRawContent.style.display = 'block';

            // 移除语法高亮类，避免Markdown符号被渲染
            previewRawContent.className = '';
        }
    }

    switchPreviewMode(mode) {
        this.previewMode = mode;

        // 更新按钮状态
        document.querySelectorAll('.preview-toolbar .btn-group .btn').forEach(btn => {
            btn.classList.remove('active');
        });
        event.target.classList.add('active');

        // 重新渲染预览
        if (this.currentPreviewData) {
            if (mode === 'raw') {
                // 原始Markdown模式 - 获取绝对路径版本
                this.loadAbsoluteContent();
            } else {
                // 渲染预览模式 - 使用现有的相对路径内容
                this.renderPreview(this.currentPreviewData.content);
            }
        }
    }

    async loadAbsoluteContent() {
        if (!this.currentPreviewData || !this.currentPreviewData.mdFilePath) {
            console.error('缺少文件路径信息');
            return;
        }

        try {
            console.log('获取原始Markdown内容（绝对路径）...');
            const response = await fetch('/read-md-file', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    file_path: this.currentPreviewData.mdFilePath,
                    use_absolute_paths: true  // 请求绝对路径版本
                })
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    console.log('成功获取绝对路径内容');
                    this.renderPreview(result.content);
                } else {
                    console.error('获取绝对路径内容失败:', result.message);
                    // 降级到现有内容
                    this.renderPreview(this.currentPreviewData.content);
                }
            } else {
                console.error('请求绝对路径内容失败:', response.status);
                // 降级到现有内容
                this.renderPreview(this.currentPreviewData.content);
            }
        } catch (error) {
            console.error('加载绝对路径内容异常:', error);
            // 降级到现有内容
            this.renderPreview(this.currentPreviewData.content);
        }
    }

    backToUpload() {
        // 检查是否正在转换中
        if (this.isConverting || this.isUploading) {
            this.showNotification('正在处理文件，请等待完成', 'warning');
            return;
        }

        console.log(`返回上一页，预览来源: ${this.previewSource}`);

        // 根据预览来源返回到正确的页面
        switch (this.previewSource) {
            case 'batch':
                // 返回到批量转换页面
                this.hideAllPages();
                document.getElementById('batchUploadPage').style.display = 'block';
                console.log('返回到批量转换页面');
                break;
            case 'history':
                // 返回到历史记录页面
                this.showHistory();
                console.log('返回到历史记录页面');
                break;
            case 'single':
            default:
                // 返回到单文件上传页面
                this.isConverting = false;
                this.isUploading = false;
                this.currentSingleFile = null;
                this.currentPreviewData = null;

                // 重置界面状态
                this.setUploadAreaState(false);
                this.setConvertButtonState(false);
                this.hideProgress();

                // 清空文件输入
                const singleFileInput = document.getElementById('singleFileInput');
                if (singleFileInput) {
                    singleFileInput.value = '';
                }

                // 重新显示上传页面
                this.showUploadArea(this.currentFormat);
                console.log('返回到单文件上传页面');
                break;
        }

        // 重置预览状态
        this.currentPreviewData = null;
        this.previewSource = null;
    }

    // 下载和复制方法
    downloadCurrent() {
        if (this.currentPreviewData && this.currentPreviewData.mdFilePath) {
            const filename = this.currentPreviewData.originalName.replace(/\.[^/.]+$/, '') + '.md';
            const downloadUrl = `/download-md?file_path=${encodeURIComponent(this.currentPreviewData.mdFilePath)}&filename=${encodeURIComponent(filename)}`;

            // 使用安全的下载方式
            const downloadLink = document.createElement('a');
            downloadLink.href = downloadUrl;
            downloadLink.download = filename;
            downloadLink.target = '_blank';
            downloadLink.style.display = 'none';

            document.body.appendChild(downloadLink);
            downloadLink.click();

            setTimeout(() => {
                document.body.removeChild(downloadLink);
            }, 100);

            this.showNotification('正在下载Markdown文件...', 'info');
        }
    }

    copyToClipboard() {
        if (this.currentPreviewData) {
            navigator.clipboard.writeText(this.currentPreviewData.content).then(() => {
                this.showNotification('内容已复制到剪贴板', 'success');
            }).catch(() => {
                this.showNotification('复制失败', 'error');
            });
        }
    }

    // 进度条管理
    showProgress() {
        const progressContainer = document.getElementById('progressContainer');
        if (progressContainer) {
            progressContainer.style.display = 'block';
            // 强制重新渲染以确保动画效果
            progressContainer.offsetHeight;
        }
        this.updateProgress(0, '准备转换...');
    }

    updateProgress(percent, text) {
        const progressBar = document.getElementById('progressBar');
        const progressText = document.getElementById('progressText');

        if (progressBar) {
            progressBar.style.width = percent + '%';
            progressBar.textContent = Math.round(percent) + '%';

            // 添加进度条动画类
            if (percent > 0) {
                progressBar.classList.add('progress-bar-animated', 'progress-bar-striped');
            } else {
                progressBar.classList.remove('progress-bar-animated', 'progress-bar-striped');
            }
        }

        if (progressText) {
            progressText.textContent = text;
        }

        // 打印调试信息
        console.log(`Progress: ${Math.round(percent)}% - ${text}`);
    }

    hideProgress() {
        const progressContainer = document.getElementById('progressContainer');
        if (progressContainer) {
            progressContainer.style.display = 'none';
        }

        // 清除进度条动画
        const progressBar = document.getElementById('progressBar');
        if (progressBar) {
            progressBar.classList.remove('progress-bar-animated', 'progress-bar-striped');
        }
    }

    // 工具方法
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    getFileIconClass(filename) {
        const ext = filename.split('.').pop().toLowerCase();
        const iconMap = {
            'pdf': 'fa-file-pdf text-danger',
            'doc': 'fa-file-word text-primary', 'docx': 'fa-file-word text-primary',
            'xls': 'fa-file-excel text-success', 'xlsx': 'fa-file-excel text-success',
            'ppt': 'fa-file-powerpoint text-warning', 'pptx': 'fa-file-powerpoint text-warning',
            'jpg': 'fa-file-image text-purple', 'jpeg': 'fa-file-image text-purple',
            'png': 'fa-file-image text-purple', 'gif': 'fa-file-image text-purple',
            'bmp': 'fa-file-image text-purple',
            'mp3': 'fa-file-audio text-pink', 'wav': 'fa-file-audio text-pink',
            'flac': 'fa-file-audio text-pink', 'aac': 'fa-file-audio text-pink',
            'ogg': 'fa-file-audio text-pink', 'm4a': 'fa-file-audio text-pink',
            'wma': 'fa-file-audio text-pink',
            'mp4': 'fa-file-video text-orange', 'avi': 'fa-file-video text-orange',
            'mov': 'fa-file-video text-orange', 'mkv': 'fa-file-video text-orange',
            'wmv': 'fa-file-video text-orange', 'flv': 'fa-file-video text-orange',
            'webm': 'fa-file-video text-orange', 'm4v': 'fa-file-video text-orange',
            '3gp': 'fa-file-video text-orange', 'mpg': 'fa-file-video text-orange',
            'mpeg': 'fa-file-video text-orange',
            'html': 'fa-file-code text-warning', 'htm': 'fa-file-code text-warning',
            'csv': 'fa-file-csv text-info',
            'json': 'fa-file-code text-dark',
            'xml': 'fa-file-code text-brown'
        };
        return iconMap[ext] || 'fa-file text-secondary';
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${this.getAlertClass(type)} alert-dismissible fade show notification`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            animation: slideInRight 0.3s ease;
        `;
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas ${this.getNotificationIcon(type)} me-2"></i>
                <div class="flex-grow-1">${message}</div>
                <button type="button" class="btn-close ms-2" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;

        document.body.appendChild(notification);

        // 使用硬编码的通知持续时间（简化配置）
        const duration = 5000; // 5秒

        // 自动移除通知
        setTimeout(() => {
            if (notification.parentNode) {
                notification.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.remove();
                    }
                }, 300);
            }
        }, duration);

        // 添加控制台日志
        console.log(`Notification [${type}]: ${message}`);
    }

    getNotificationIcon(type) {
        const iconMap = {
            'success': 'fa-check-circle',
            'error': 'fa-exclamation-circle',
            'warning': 'fa-exclamation-triangle',
            'info': 'fa-info-circle'
        };
        return iconMap[type] || 'fa-info-circle';
    }

    getAlertClass(type) {
        const typeMap = {
            'success': 'success',
            'error': 'danger',
            'warning': 'warning',
            'info': 'info'
        };
        return typeMap[type] || 'info';
    }

    // 历史记录管理
    async showHistory() {
        // 简化配置 - 历史记录功能始终启用

        this.hideAllPages();
        document.getElementById('historyPage').style.display = 'block';

        try {
            this.showNotification('正在加载历史记录...', 'info');
            const response = await fetch('/api/history');
            const result = await response.json();

            if (result.success) {
                if (result.message && result.message === '历史记录功能已禁用') {
                    this.renderHistory([]);
                    this.showNotification('历史记录功能已禁用', 'info');
                } else {
                    this.renderHistory(result.history);
                }
            } else {
                throw new Error(result.message || '加载历史记录失败');
            }
        } catch (error) {
            console.error('Load history error:', error);
            this.showNotification('加载历史记录失败: ' + error.message, 'error');
            this.renderHistory([]); // 显示空历史记录
        }
    }

    renderHistory(history) {
        const historyList = document.getElementById('historyList');
        const emptyHistory = document.getElementById('emptyHistory');

        if (!history || history.length === 0) {
            historyList.style.display = 'none';
            emptyHistory.style.display = 'block';
            return;
        }

        historyList.style.display = 'grid';
        emptyHistory.style.display = 'none';

        historyList.innerHTML = history.map(item => this.createHistoryItem(item)).join('');

        // 添加事件监听器
        this.attachHistoryEventListeners();
    }

    createHistoryItem(item) {
        const date = new Date(item.converted_at);
        const dateStr = date.toLocaleDateString('zh-CN');
        const timeStr = date.toLocaleTimeString('zh-CN');
        const fileSize = this.formatFileSize(item.file_size);
        const iconClass = this.getFileIconClass(item.original_name);

        return `
            <div class="history-item" data-history-id="${item.id}">
                <div class="history-item-header">
                    <div class="history-item-info">
                        <div class="history-item-title">
                            <i class="fas ${iconClass}"></i>
                            ${item.original_name}
                        </div>
                        <div class="history-item-meta">
                            <i class="fas fa-file"></i>
                            <span>${item.format.toUpperCase()}</span>
                        </div>
                        <div class="history-item-meta">
                            <i class="fas fa-hdd"></i>
                            <span>${fileSize}</span>
                        </div>
                        <div class="history-item-meta">
                            <i class="fas fa-calendar"></i>
                            <span>${dateStr} ${timeStr}</span>
                        </div>
                    </div>
                    <div class="history-item-actions">
                        <button class="btn btn-success btn-sm" onclick="fileConverter.previewHistoryItem('${item.id}')" title="预览">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn btn-primary btn-sm" onclick="fileConverter.downloadHistoryItem('${item.id}')" title="下载">
                            <i class="fas fa-download"></i>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="fileConverter.deleteHistoryItem('${item.id}')" title="删除">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    attachHistoryEventListeners() {
        // 历史记录项悬停效果已经通过CSS实现
    }

    async previewHistoryItem(historyId) {
        try {
            const response = await fetch('/api/history');
            const result = await response.json();

            if (result.success) {
                const item = result.history.find(h => h.id === historyId);
                if (item) {
                    // 读取文件内容
                    const contentResponse = await fetch('/read-md-file', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            file_path: item.md_file_path
                        })
                    });

                    if (contentResponse.ok) {
                        const contentResult = await contentResponse.json();
                        if (contentResult.success) {
                            console.log('调用 showPreview，传入预览来源: history');
                            this.showPreview({
                                originalName: item.original_name,
                                originalSize: this.formatFileSize(item.file_size),
                                content: contentResult.content,
                                mdFilePath: item.md_file_path,
                                convertTime: new Date(item.converted_at).toLocaleString(),
                                format: item.format
                            }, 'history');
                        } else {
                            throw new Error(contentResult.message || '无法读取文件内容');
                        }
                    } else {
                        throw new Error('文件读取失败');
                    }
                } else {
                    throw new Error('历史记录不存在');
                }
            } else {
                throw new Error(result.message || '获取历史记录失败');
            }
        } catch (error) {
            console.error('Preview history item error:', error);
            this.showNotification('预览失败: ' + error.message, 'error');
        }
    }

    async downloadHistoryItem(historyId) {
        try {
            const response = await fetch('/api/history');
            const result = await response.json();

            if (result.success) {
                const item = result.history.find(h => h.id === historyId);
                if (item) {
                    const filename = item.original_name.replace(/\.[^/.]+$/, '') + '.md';

                    // 创建临时下载链接，避免长URL导致的布局问题
                    const downloadLink = document.createElement('a');
                    downloadLink.href = `${item.download_url}&filename=${encodeURIComponent(filename)}`;
                    downloadLink.download = filename;
                    downloadLink.target = '_blank';
                    downloadLink.style.display = 'none';

                    // 添加到DOM并触发点击
                    document.body.appendChild(downloadLink);
                    downloadLink.click();

                    // 清理DOM元素
                    setTimeout(() => {
                        document.body.removeChild(downloadLink);
                    }, 100);

                    this.showNotification('开始下载...', 'info');
                } else {
                    throw new Error('历史记录不存在');
                }
            } else {
                throw new Error(result.message || '获取历史记录失败');
            }
        } catch (error) {
            console.error('Download history item error:', error);
            this.showNotification('下载失败: ' + error.message, 'error');
        }
    }

    async deleteHistoryItem(historyId) {
        // 简化配置 - 历史记录功能始终启用

        if (!confirm('确定要删除这条历史记录吗？')) {
            return;
        }

        try {
            const response = await fetch(`/api/history/${historyId}`, {
                method: 'DELETE'
            });
            const result = await response.json();

            if (result.success) {
                this.showNotification('历史记录已删除', 'success');
                // 重新加载历史记录
                await this.showHistory();
            } else {
                throw new Error(result.message || '删除失败');
            }
        } catch (error) {
            console.error('Delete history item error:', error);
            this.showNotification('删除失败: ' + error.message, 'error');
        }
    }

    async clearHistory() {
        // 简化配置 - 历史记录功能始终启用

        if (!confirm('确定要清空所有历史记录吗？此操作不可恢复。')) {
            return;
        }

        try {
            const response = await fetch('/api/history/clear', {
                method: 'POST'
            });
            const result = await response.json();

            if (result.success) {
                this.showNotification('历史记录已清空', 'success');
                // 重新加载历史记录
                await this.showHistory();
            } else {
                throw new Error(result.message || '清空失败');
            }
        } catch (error) {
            console.error('Clear history error:', error);
            this.showNotification('清空失败: ' + error.message, 'error');
        }
    }

    // 批量转换功能
    showBatchUploadArea() {
        // 检查是否正在转换中
        if (this.isConverting || this.isUploading) {
            this.showNotification('正在处理文件，请等待完成', 'warning');
            return;
        }

        this.hideAllPages();
        document.getElementById('batchUploadPage').style.display = 'block';

        // 重置批量转换状态
        this.resetBatchState();

        console.log('显示批量转换页面');
    }

    resetBatchState() {
        this.currentBatchId = null;
        this.currentBatchFiles = [];
        this.batchStatus = null;

        // 清空状态轮询
        if (this.batchStatusInterval) {
            clearInterval(this.batchStatusInterval);
            this.batchStatusInterval = null;
        }

        // 重置UI状态
        const batchFileInput = document.getElementById('batchFileInput');
        const batchArchiveInfo = document.getElementById('batchArchiveInfo');
        const batchFilesContainer = document.getElementById('batchFilesContainer');
        const batchProgressContainer = document.getElementById('batchProgressContainer');
        const convertAllButton = document.getElementById('convertAllButton');

        if (batchFileInput) batchFileInput.value = '';
        if (batchArchiveInfo) batchArchiveInfo.style.display = 'none';
        if (batchFilesContainer) batchFilesContainer.style.display = 'none';
        if (batchProgressContainer) batchProgressContainer.style.display = 'none';
        if (convertAllButton) convertAllButton.disabled = true;
    }

    isArchiveFile(filename) {
        const ext = '.' + filename.split('.').pop().toLowerCase();
        return ['.zip'].includes(ext);
    }

    async handleBatchArchive(file) {
        if (!this.isArchiveFile(file.name)) {
            this.showNotification('请上传ZIP格式的压缩包', 'error');
            return;
        }

        console.log('开始处理批量压缩包:', file.name);

        // 创建FormData
        const formData = new FormData();
        formData.append('file', file);

        // 获取密码
        const password = document.getElementById('archivePassword').value;

        if (password) {
            formData.append('password', password);
        }

        try {
            this.showNotification('正在上传压缩包...', 'info');

            const response = await fetch('/upload/batch', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            if (result.success) {
                this.currentBatchId = result.batch_id;
                this.showBatchArchiveInfo(result);
                this.showNotification('压缩包上传成功！', 'success');
            } else {
                throw new Error(result.message || '上传失败');
            }

        } catch (error) {
            console.error('Batch upload error:', error);
            this.showNotification('压缩包上传失败: ' + error.message, 'error');
        }
    }

    showBatchArchiveInfo(data) {
        const batchArchiveInfo = document.getElementById('batchArchiveInfo');
        const batchArchiveName = document.getElementById('batchArchiveName');
        const batchArchiveSize = document.getElementById('batchArchiveSize');
        const extractButton = document.getElementById('extractButton');

        if (batchArchiveName) batchArchiveName.textContent = data.archive_name;
        if (batchArchiveSize) batchArchiveSize.textContent = this.formatFileSize(data.file_size);
        if (batchArchiveInfo) batchArchiveInfo.style.display = 'block';
        if (extractButton) extractButton.disabled = false;

        console.log('显示压缩包信息:', data);
    }

    async extractArchive() {
        console.log('=== extractArchive 开始 ===');
        console.log('当前Batch ID:', this.currentBatchId);

        if (!this.currentBatchId) {
            console.log('没有Batch ID，无法解压');
            this.showNotification('没有可解压的压缩包', 'warning');
            return;
        }

        try {
            this.showNotification('正在解压压缩包...', 'info');
            console.log('发送解压请求到:', `/extract/batch/${this.currentBatchId}`);

            // 禁用解压按钮
            const extractButton = document.getElementById('extractButton');
            if (extractButton) {
                extractButton.disabled = true;
                console.log('禁用解压按钮');
            }

            const response = await fetch(`/extract/batch/${this.currentBatchId}`, {
                method: 'POST'
            });

            console.log('解压响应状态:', response.status, response.statusText);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('解压响应数据:', result);

            if (result.success) {
                this.currentBatchFiles = result.files || [];
                console.log('存储批量文件:', this.currentBatchFiles);
                console.log('调用 showBatchFilesList，传入文件:', result.files);

                this.showBatchFilesList(result.files);
                this.showNotification(`解压完成！共 ${result.total_files} 个文件`, 'success');
            } else {
                console.error('解压失败:', result.message);
                throw new Error(result.message || '解压失败');
            }

        } catch (error) {
            console.error('Extract archive error:', error);
            this.showNotification('解压失败: ' + error.message, 'error');

            // 重新启用解压按钮
            const extractButton = document.getElementById('extractButton');
            if (extractButton) extractButton.disabled = false;
        }

        console.log('=== extractArchive 完成 ===');
    }

    showBatchFilesList(files) {
        console.log('=== showBatchFilesList 开始 ===');
        console.log('接收到的文件数据:', files);

        const batchFilesContainer = document.getElementById('batchFilesContainer');
        const batchFilesList = document.getElementById('batchFilesList');
        const fileCount = document.getElementById('fileCount');
        const convertAllButton = document.getElementById('convertAllButton');

        console.log('DOM元素检查:');
        console.log('  batchFilesContainer:', batchFilesContainer);
        console.log('  batchFilesList:', batchFilesList);
        console.log('  fileCount:', fileCount);
        console.log('  convertAllButton:', convertAllButton);

        if (!files || files.length === 0) {
            console.log('文件列表为空，隐藏容器');
            if (batchFilesContainer) batchFilesContainer.style.display = 'none';
            return;
        }

        // 过滤出成功解压的文件
        const validFiles = files.filter(file => file.extracted_path && !file.error);

        console.log('文件过滤结果:');
        console.log('  总文件数:', files.length);
        console.log('  有效文件数:', validFiles.length);
        console.log('  有效文件详情:', validFiles);

        if (fileCount) {
            fileCount.textContent = validFiles.length;
            console.log('设置文件数量:', validFiles.length);
        } else {
            console.error('fileCount 元素未找到！');
        }

        if (batchFilesContainer) {
            batchFilesContainer.style.display = 'block';
            console.log('显示文件容器');
        } else {
            console.error('batchFilesContainer 元素未找到！');
        }

        if (convertAllButton) {
            convertAllButton.disabled = validFiles.length === 0;
            console.log('转换按钮状态:', validFiles.length === 0 ? '禁用' : '启用');
        } else {
            console.error('convertAllButton 元素未找到！');
        }

        if (batchFilesList) {
            const htmlContent = validFiles.map(file => this.createBatchFileItem(file)).join('');
            batchFilesList.innerHTML = htmlContent;
            console.log('生成文件列表HTML，长度:', htmlContent.length);
            console.log('生成的HTML预览:', htmlContent.substring(0, 200) + '...');

            // 添加复选框事件监听器
            this.attachBatchFileListeners();

            // 初始化选择状态
            this.updateBatchFilesSelection();
        } else {
            console.error('batchFilesList 元素未找到！');
        }

        console.log('=== showBatchFilesList 完成 ===');
    }

    createBatchFileItem(file) {
        const iconClass = this.getFileIconClass(file.filename);
        const fileSize = this.formatFileSize(file.size);

        return `
            <div class="batch-file-item" data-filename="${file.filename}">
                <div class="batch-file-checkbox">
                    <input type="checkbox" class="form-check-input batch-file-checkbox-input" checked data-file="${file.filename}">
                </div>
                <div class="batch-file-info">
                    <i class="fas ${iconClass} batch-file-icon"></i>
                    <div class="batch-file-details">
                        <div class="batch-file-name">${file.filename}</div>
                        <div class="batch-file-meta">
                            <span><i class="fas fa-file"></i>${file.format.toUpperCase()}</span>
                            <span><i class="fas fa-hdd"></i>${fileSize}</span>
                        </div>
                    </div>
                </div>
                <div class="batch-file-status">
                    <span class="badge bg-secondary">等待转换</span>
                </div>
            </div>
        `;
    }

    selectAllFiles() {
        const checkboxes = document.querySelectorAll('.batch-file-item input[type="checkbox"]');
        checkboxes.forEach(checkbox => checkbox.checked = true);
        this.updateBatchFilesSelection();
    }

    deselectAllFiles() {
        const checkboxes = document.querySelectorAll('.batch-file-item input[type="checkbox"]');
        checkboxes.forEach(checkbox => checkbox.checked = false);
        this.updateBatchFilesSelection();
    }

    // 添加批量文件事件监听器
    attachBatchFileListeners() {
        const checkboxes = document.querySelectorAll('.batch-file-checkbox-input');
        checkboxes.forEach(checkbox => {
            // 移除旧的事件监听器（如果有）
            checkbox.removeEventListener('change', this.onCheckboxChange);
            // 添加新的事件监听器
            checkbox.addEventListener('change', () => this.updateBatchFilesSelection());
        });
    }

    updateBatchFilesSelection() {
        const checkboxes = document.querySelectorAll('.batch-file-item input[type="checkbox"]:checked');
        const convertAllButton = document.getElementById('convertAllButton');
        const selectedCountElement = document.getElementById('selectedCount');

        // 更新选中数量显示
        if (selectedCountElement) {
            selectedCountElement.textContent = checkboxes.length;
        }

        // 只要有一个文件选中就可以转换
        if (convertAllButton) {
            convertAllButton.disabled = checkboxes.length === 0;

            // 更新按钮文本显示选中数量
            const buttonText = convertAllButton.querySelector('span');
            if (buttonText) {
                if (checkboxes.length === 0) {
                    buttonText.textContent = '请选择文件';
                } else {
                    buttonText.textContent = `转换选中的文件 (${checkboxes.length})`;
                }
            }
        }
    }

    async convertAllBatch() {
        if (!this.currentBatchId || !this.currentBatchFiles.length) {
            this.showNotification('没有可转换的文件', 'warning');
            return;
        }

        // 获取选中的文件
        const selectedFiles = [];
        const checkboxes = document.querySelectorAll('.batch-file-item input[type="checkbox"]:checked');

        checkboxes.forEach(checkbox => {
            const filename = checkbox.dataset.file;
            const file = this.currentBatchFiles.find(f => f.filename === filename);
            if (file) {
                selectedFiles.push(file);
            }
        });

        if (selectedFiles.length === 0) {
            this.showNotification('请至少选择一个文件进行转换', 'warning');
            return;
        }

        try {
            this.showNotification('开始批量转换...', 'info');

            const response = await fetch(`/convert/batch/${this.currentBatchId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    files: selectedFiles
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            if (result.success) {
                this.showNotification(`批量转换已开始，共 ${result.total_files} 个文件`, 'success');
                this.showBatchProgress();
                this.startBatchStatusPolling();
            } else {
                throw new Error(result.message || '启动批量转换失败');
            }

        } catch (error) {
            console.error('Convert batch error:', error);
            this.showNotification('批量转换失败: ' + error.message, 'error');
        }
    }

    showBatchProgress() {
        const batchProgressContainer = document.getElementById('batchProgressContainer');
        if (batchProgressContainer) {
            batchProgressContainer.style.display = 'block';
        }
    }

    startBatchStatusPolling() {
        if (this.batchStatusInterval) {
            clearInterval(this.batchStatusInterval);
        }

        this.batchStatusInterval = setInterval(() => {
            this.updateBatchStatus();
        }, 2000); // 每2秒更新一次状态
    }

    async updateBatchStatus() {
        if (!this.currentBatchId) return;

        try {
            const response = await fetch(`/status/batch/${this.currentBatchId}`);
            if (!response.ok) return;

            const result = await response.json();
            if (result.success) {
                console.log('=== 批量状态更新 ===');
                console.log('完整状态数据:', JSON.stringify(result.status, null, 2));

                if (result.status.files) {
                    console.log('=== 文件状态详情 ===');
                    result.status.files.forEach((file, index) => {
                        console.log(`文件 ${index + 1}: ${file.filename}`);
                        console.log(`  - conversion_status: ${file.conversion_status}`);
                        console.log(`  - conversion_progress: ${file.conversion_progress}`);
                        console.log(`  - download_url: ${file.download_url}`);
                        console.log(`  - md_file_path: ${file.md_file_path}`);
                        console.log(`  - conversion_error: ${file.conversion_error}`);
                    });
                }

                this.renderBatchProgress(result.status);

                // 检查是否所有文件都已完成（成功或失败）
                if (result.status.files && result.status.files.length > 0) {
                    const allFinished = result.status.files.every(file =>
                        file.conversion_status === 'completed' ||
                        file.conversion_status === 'failed'
                    );

                    if (allFinished) {
                        console.log('所有文件转换完成，停止轮询');
                        this.onBatchConversionComplete(result.status);
                        clearInterval(this.batchStatusInterval);
                        this.batchStatusInterval = null;
                    }
                }
            }

            // 如果后端显示整体完成，也停止轮询
            if (result.status.status === 'completed' || result.status.status === 'conversion_failed') {
                console.log('后端显示转换完成，停止轮询');
                clearInterval(this.batchStatusInterval);
                this.batchStatusInterval = null;
                this.onBatchConversionComplete(result.status);
            }

        } catch (error) {
            console.error('Update batch status error:', error);
        }
    }

    // 批量转换完成处理
    onBatchConversionComplete(status) {
        console.log('批量转换完成:', status);

        const completed = status.conversion_progress?.completed || 0;
        const failed = status.conversion_progress?.failed || 0;
        const total = status.conversion_progress?.total || 0;

        // 显示完成通知
        if (failed > 0) {
            this.showNotification(`批量转换完成！成功: ${completed}，失败: ${failed}`, completed > 0 ? 'warning' : 'error');
        } else {
            this.showNotification(`批量转换成功完成！共转换 ${completed} 个文件`, 'success');
        }

        // 更新总进度到100%
        const batchOverallProgress = document.getElementById('batchOverallProgress');
        const batchProgressText = document.getElementById('batchProgressText');

        if (batchOverallProgress) {
            batchOverallProgress.style.width = '100%';
            batchOverallProgress.textContent = '100%';
            batchOverallProgress.className = 'progress-bar bg-success';
        }

        if (batchProgressText) {
            batchProgressText.textContent = `转换完成！共 ${completed + failed} 个文件 (成功: ${completed}, 失败: ${failed})`;
        }

        // 禁用转换按钮，启用重新选择文件选项
        const convertAllButton = document.getElementById('convertAllButton');
        if (convertAllButton) {
            convertAllButton.disabled = true;
            const buttonText = convertAllButton.querySelector('span');
            if (buttonText) {
                buttonText.textContent = '转换已完成';
            }
        }
    }

    renderBatchProgress(status) {
        const batchOverallProgress = document.getElementById('batchOverallProgress');
        const batchProgressText = document.getElementById('batchProgressText');
        const batchFilesProgress = document.getElementById('batchFilesProgress');

        if (status.conversion_progress) {
            const progress = status.conversion_progress;
            const total = progress.total || 0;
            const completed = progress.completed || 0;
            const failed = progress.failed || 0;
            const processing = progress.processing || 0;

            const percentage = total > 0 ? Math.round(((completed + failed) / total) * 100) : 0;

            if (batchOverallProgress) {
                batchOverallProgress.style.width = percentage + '%';
                batchOverallProgress.textContent = percentage + '%';
            }

            if (batchProgressText) {
                batchProgressText.textContent = `总进度: ${completed + failed}/${total} (完成: ${completed}, 失败: ${failed}, 处理中: ${processing})`;
            }
        }

        // 渲染单个文件进度
        if (batchFilesProgress && status.files) {
            batchFilesProgress.innerHTML = status.files.map(file => this.createFileProgressItem(file)).join('');
        }

        // 更新文件列表中的状态
        this.updateFilesListStatus(status.files);
    }

    createFileProgressItem(file) {
        const status = file.conversion_status || 'pending';
        const progress = file.conversion_progress || 0;
        const statusClass = this.getStatusClass(status);
        const statusText = this.getStatusText(status);

        return `
            <div class="batch-file-progress ${statusClass}">
                <div class="batch-file-progress-header">
                    <div class="batch-file-progress-name">${file.filename}</div>
                    <div class="batch-file-progress-status ${statusClass}">${statusText}</div>
                </div>
                <div class="batch-file-progress-bar">
                    <div class="batch-file-progress-fill" style="width: ${progress}%"></div>
                </div>
                ${file.conversion_error ? `<div class="text-danger small mt-1">${file.conversion_error}</div>` : ''}
            </div>
        `;
    }

    updateFilesListStatus(files) {
        console.log('=== updateFilesListStatus 开始 ===');
        console.log('传入的文件数据:', files);

        files.forEach((file, index) => {
            console.log(`处理文件 ${index + 1}: ${file.filename}`);
            console.log(`  文件完整数据:`, file);

            // 处理文件名匹配 - 后端可能发送完整路径，需要提取文件名
            let displayName = file.filename;
            if (file.filename.includes('\\') || file.filename.includes('/')) {
                // 从完整路径中提取文件名
                const pathParts = file.filename.split(/[/\\]/);
                displayName = pathParts[pathParts.length - 1];
                console.log(`  从路径提取文件名: ${displayName}`);
            }

            const fileItem = document.querySelector(`.batch-file-item[data-filename="${displayName}"]`);
            console.log(`找到文件项元素 (${displayName}):`, fileItem);

            if (fileItem) {
                const statusElement = fileItem.querySelector('.batch-file-status span');
                const status = file.conversion_status || 'pending';

                console.log(`文件 ${file.filename} 状态:`, status);

                if (statusElement) {
                    const statusClass = this.getStatusBadgeClass(status);
                    const statusText = this.getStatusText(status);

                    console.log(`更新状态元素: ${statusElement.textContent} -> ${statusText}`);

                    statusElement.className = `badge ${statusClass}`;
                    statusElement.textContent = statusText;
                } else {
                    console.warn(`文件 ${file.filename} 找不到状态元素`);
                }

                // 如果转换完成，立即添加预览和下载按钮
                if (status === 'completed') {
                    console.log(`✅ 文件 ${file.filename} 转换完成，准备添加按钮`);
                    console.log(`完整文件数据:`, file);

                    // 更新本地文件数据，确保有下载URL
                    const localFile = this.currentBatchFiles.find(f => f.filename === displayName);
                    if (localFile) {
                        console.log(`✅ 找到本地文件，更新数据`);
                        // 同步后端数据到本地
                        localFile.conversion_status = file.conversion_status;
                        localFile.download_url = file.download_url;
                        localFile.md_file_path = file.md_file_path;
                        localFile.converted_at = file.converted_at;
                        console.log(`✅ 更新本地文件数据:`, localFile);
                    } else {
                        console.warn(`❌ 未找到本地文件: ${displayName}`);
                    }

                    if (file.download_url) {
                        console.log(`✅ 为文件 ${displayName} 添加查看和下载按钮`);
                        this.addFileActions(fileItem, file, displayName);
                    } else {
                        console.warn(`❌ 文件 ${displayName} 缺少 download_url`);
                    }
                } else if (status === 'failed') {
                    console.log(`❌ 文件 ${file.filename} 转换失败，错误: ${file.conversion_error}`);
                } else {
                    console.log(`⏳ 文件 ${file.filename} 状态: ${status}`);
                }
            } else {
                console.warn(`❌ 未找到文件项元素: ${file.filename}`);
            }
        });

        console.log('=== updateFilesListStatus 完成 ===');
    }

    addFileActions(fileItem, file, displayName) {
        // 先检查是否已经存在按钮，避免重复添加
        let actionsContainer = fileItem.querySelector('.file-actions');
        if (!actionsContainer) {
            actionsContainer = document.createElement('div');
            actionsContainer.className = 'file-actions';
            actionsContainer.style.cssText = 'margin-left: auto; display: flex; gap: 5px;';

            // 找到状态容器，在其后添加按钮
            const statusContainer = fileItem.querySelector('.batch-file-status');
            if (statusContainer) {
                statusContainer.appendChild(actionsContainer);
            } else {
                fileItem.appendChild(actionsContainer);
            }
        }

        actionsContainer.innerHTML = `
            <button class="btn btn-success btn-sm" onclick="fileConverter.previewBatchFile('${displayName}')" title="预览" style="margin-right: 5px;">
                <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-primary btn-sm" onclick="fileConverter.downloadBatchFile('${file.download_url}', '${displayName}')" title="下载">
                <i class="fas fa-download"></i>
            </button>
        `;

        console.log(`查看和下载按钮已添加到文件 ${displayName}`);
    }

    async previewBatchFile(filename) {
        console.log('=== previewBatchFile 开始 ===');
        console.log('查找文件:', filename);
        console.log('当前批量文件列表:', this.currentBatchFiles);

        const file = this.currentBatchFiles.find(f => f.filename === filename);
        console.log('找到文件:', file);

        if (!file) {
            console.error('未找到文件:', filename);
            this.showNotification('文件不存在', 'error');
            return;
        }

        if (!file.md_file_path) {
            console.error('文件缺少 md_file_path:', file);
            this.showNotification('文件尚未转换完成或缺少文件路径', 'warning');
            return;
        }

        try {
            const response = await fetch('/read-md-file', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    file_path: file.md_file_path
                })
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    console.log('调用 showPreview，传入预览来源: batch');
                    this.showPreview({
                        originalName: file.filename,
                        originalSize: this.formatFileSize(file.size),
                        content: result.content,
                        mdFilePath: file.md_file_path,
                        convertTime: new Date(file.converted_at).toLocaleString(),
                        format: file.format
                    }, 'batch');
                }
            }
        } catch (error) {
            console.error('Preview batch file error:', error);
            this.showNotification('预览失败: ' + error.message, 'error');
        }
    }

    downloadBatchFile(downloadUrl, filename) {
        const cleanFilename = filename.replace(/\.[^/.]+$/, '') + '.md';
        window.open(`${downloadUrl}&filename=${encodeURIComponent(cleanFilename)}`, '_blank');
    }

    getStatusClass(status) {
        const statusMap = {
            'pending': '',
            'processing': 'processing',
            'completed': 'completed',
            'failed': 'error'
        };
        return statusMap[status] || '';
    }

    getStatusBadgeClass(status) {
        const statusMap = {
            'pending': 'bg-secondary',
            'processing': 'bg-warning',
            'completed': 'bg-success',
            'failed': 'bg-danger'
        };
        return statusMap[status] || 'bg-secondary';
    }

    getStatusText(status) {
        const statusMap = {
            'pending': '等待中',
            'processing': '转换中',
            'completed': '已完成',
            'failed': '转换失败'
        };
        return statusMap[status] || '未知';
    }

    togglePasswordVisibility() {
        const passwordInput = document.getElementById('archivePassword');
        const toggleButton = document.getElementById('togglePassword');
        const icon = toggleButton.querySelector('i');

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            icon.className = 'fas fa-eye-slash';
        } else {
            passwordInput.type = 'password';
            icon.className = 'fas fa-eye';
        }
    }
}

// 全局函数供HTML调用
let fileConverter;

function convertFiles() {
    fileConverter.convertFiles();
}

function clearFiles() {
    fileConverter.clearFiles();
}

function downloadAll() {
    fileConverter.downloadAll();
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM加载完成，开始初始化FileConverter');
    fileConverter = new FileConverter();
    console.log('FileConverter初始化完成');
});