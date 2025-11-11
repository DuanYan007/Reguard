# MarkItDown Web 配置热部署功能说明

## 概述

MarkItDown Web 现已支持完整的配置热部署功能，允许您在不重启应用的情况下动态修改和更新配置参数。该功能包括：

- **动态配置管理**: 支持运行时修改应用配置
- **配置热重载**: 自动检测配置文件变化并重新加载
- **Web管理界面**: 提供用户友好的配置管理页面
- **API接口**: 支持通过RESTful API管理配置
- **前端热更新**: 前端自动同步配置变化

## 配置文件结构

配置文件 `config.json` 包含以下主要配置段：

### 应用配置 (`app`)
```json
{
  "app": {
    "secret_key": "your-secret-key-change-in-production",
    "debug": true,
    "host": "0.0.0.0",
    "port": 5000
  }
}
```

### 存储配置 (`storage`)
```json
{
  "storage": {
    "upload_folder": "uploads",
    "download_folder": "downloads",
    "history_file": "history.json",
    "batch_status_file": "batch_status.json"
  }
}
```

### 限制配置 (`limits`)
```json
{
  "limits": {
    "max_file_size": 104857600,
    "max_file_size_human": "100MB",
    "max_history_records": 100,
    "batch_status_poll_interval": 2000,
    "cleanup_temp_files_hours": 24
  }
}
```

### 支持的格式 (`supported_formats`)
```json
{
  "supported_formats": {
    "pdf": ".pdf",
    "word": ".doc,.docx",
    "excel": ".xls,.xlsx",
    "ppt": ".ppt,.pptx",
    "image": ".jpg,.jpeg,.png,.gif,.bmp",
    "audio": ".mp3,.wav,.flac,.aac,.ogg,.m4a,.wma",
    "video": ".mp4,.avi,.mov,.mkv,.wmv,.flv,.webm,.m4v,.3gp,.mpg,.mpeg",
    "html": ".html,.htm",
    "csv": ".csv",
    "json": ".json",
    "xml": ".xml",
    "zip": ".zip",
    "rar": ".rar"
  }
}
```

### 功能开关 (`feature_flags`)
```json
{
  "feature_flags": {
    "enable_batch_conversion": true,
    "enable_history_management": true,
    "enable_file_preview": true,
    "enable_password_protection": true,
    "auto_cleanup_temp_files": true
  }
}
```

### UI配置 (`ui`)
```json
{
  "ui": {
    "default_preview_mode": "render",
    "notification_duration": 5000,
    "page_title": "MarkItDown Web - 文档转换平台",
    "theme": "default"
  }
}
```

## 使用方法

### 1. Web界面管理

访问 `http://localhost:5000/config` 打开配置管理页面：

- **查看配置**: 查看当前所有配置参数
- **修改配置**: 通过表单修改各项配置
- **保存配置**: 点击"保存配置"按钮应用更改
- **重新加载**: 点击"重新加载"按钮强制重新加载配置
- **导出配置**: 导出当前配置为JSON文件
- **导入配置**: 从JSON文件导入配置

### 2. API接口管理

#### 获取配置
```bash
curl -X GET http://localhost:5000/api/config
```

#### 更新配置
```bash
curl -X PUT http://localhost:5000/api/config \
  -H "Content-Type: application/json" \
  -d '{
    "ui": {
      "page_title": "新标题",
      "notification_duration": 3000
    }
  }'
```

#### 重新加载配置
```bash
curl -X POST http://localhost:5000/api/config/reload
```

### 3. 直接编辑配置文件

您可以直接编辑 `config.json` 文件，配置管理器会自动检测到变化并重新加载：

```bash
# 修改配置
vim config.json

# 配置管理器会在1秒内自动检测到变化并重新加载
```

## 热部署特性

### 自动检测
- 配置管理器每秒检查一次配置文件的修改时间
- 当检测到变化时，自动验证并重新加载配置

### 配置验证
- 自动验证配置格式和参数有效性
- 提供默认值处理，确保应用稳定运行

### 回调机制
- 配置变化时自动触发回调函数
- 更新Flask应用配置和相关组件

### 前端同步
- 前端每30秒检查一次配置变化
- 自动应用新的UI配置和功能开关

## 常见配置修改场景

### 1. 修改上传目录
```json
{
  "storage": {
    "upload_folder": "/path/to/new/uploads",
    "download_folder": "/path/to/new/downloads"
  }
}
```

### 2. 调整文件大小限制
```json
{
  "limits": {
    "max_file_size": 209715200,
    "max_file_size_human": "200MB"
  }
}
```

### 3. 修改历史记录数量
```json
{
  "limits": {
    "max_history_records": 200
  }
}
```

### 4. 关闭某个功能
```json
{
  "feature_flags": {
    "enable_batch_conversion": false
  }
}
```

### 5. 自定义页面标题
```json
{
  "ui": {
    "page_title": "我的文档转换器",
    "notification_duration": 8000
  }
}
```

## 测试功能

运行测试脚本验证热部署功能：

```bash
cd markitdown-web/conveter
python test_hot_reload.py
```

测试脚本会验证：
- API连接
- 配置获取
- 配置更新
- 配置重载
- 文件热重载
- 格式API

## 注意事项

### 生产环境
1. 确保配置文件权限正确设置
2. 定期备份配置文件
3. 修改密钥和敏感配置后建议重启应用
4. 监控配置变化日志

### 性能考虑
- 配置文件监控开销很小（每秒检查一次）
- 配置验证采用默认值策略，避免应用崩溃
- 前端配置检查间隔可调整（默认30秒）

### 安全考虑
- API配置接口默认开放，生产环境建议添加认证
- 配置文件中的敏感信息会自动隐藏
- 自动创建配置文件备份

## 故障排除

### 配置未生效
1. 检查配置文件格式是否正确（JSON格式）
2. 查看应用日志中的配置加载信息
3. 使用API接口检查当前配置
4. 手动调用重载接口

### 配置文件损坏
1. 系统会自动使用默认配置
2. 检查 `config.json.backup` 备份文件
3. 手动恢复配置文件

### 前端配置未更新
1. 检查浏览器缓存
2. 手动刷新页面
3. 检查控制台错误信息

## 日志信息

配置相关日志会记录到：
- 控制台输出
- 日志文件（如果配置了`logging.file`）
- 浏览器控制台（前端配置变化）

关键日志信息包括：
- 配置加载/重新加载
- 配置验证结果
- 配置应用成功/失败
- 热重载触发事件

## 扩展开发

### 添加新的配置项
1. 在 `config.json` 中添加配置
2. 在代码中使用 `get_config('path.to.config')` 获取配置
3. 如需要热更新，在 `on_config_change` 回调中处理

### 自定义配置验证
在 `config_manager.py` 的 `_validate_config` 方法中添加验证逻辑。

### 添加配置变化监听器
```python
def on_config_change(old_config, new_config):
    # 处理配置变化
    pass

config_manager.add_callback(on_config_change)
```

---

通过以上功能，MarkItDown Web 现在具备了生产级的配置管理能力，支持灵活的热部署和动态配置更新。