"""
配置管理模块 - 支持热部署和动态配置更新
"""
import json
import os
import time
import threading
import logging
from typing import Any, Dict, Optional, Callable
from datetime import datetime

class ConfigManager:
    """配置管理器，支持热重载和动态配置更新"""

    def __init__(self, config_file: str = "config.json"):
        self.config_file = config_file
        self._config = {}
        self._last_modified = 0
        self._lock = threading.RLock()
        self._callbacks = []
        self._watch_thread = None
        self._watch_enabled = False
        self._watch_interval = 1.0  # 秒

        # 设置日志
        self.logger = logging.getLogger(__name__)

        # 初始化配置
        self.load_config()
        self.start_watching()

    def load_config(self) -> Dict[str, Any]:
        """加载配置文件"""
        try:
            with self._lock:
                if not os.path.exists(self.config_file):
                    self.logger.warning(f"配置文件 {self.config_file} 不存在，创建默认配置")
                    self._config = self._get_default_config()
                    self.save_config()
                    return self._config

                # 检查文件修改时间
                current_modified = os.path.getmtime(self.config_file)
                if current_modified <= self._last_modified:
                    return self._config

                with open(self.config_file, 'r', encoding='utf-8') as f:
                    new_config = json.load(f)

                # 验证配置
                validated_config = self._validate_config(new_config)

                # 记录配置变化
                old_config = self._config.copy()
                self._config = validated_config
                self._last_modified = current_modified

                # 触发回调
                if old_config != self._config:
                    self._trigger_callbacks(old_config, self._config)
                    self.logger.info(f"配置已重新加载，文件修改时间: {datetime.fromtimestamp(current_modified)}")

                return self._config

        except Exception as e:
            self.logger.error(f"加载配置文件失败: {e}")
            # 如果加载失败，使用默认配置
            if not self._config:
                self._config = self._get_default_config()
            return self._config

    def get(self, key: str, default: Any = None) -> Any:
        """获取配置值，支持点号分割的路径，如 'storage.upload_folder'"""
        try:
            current_config = self.load_config()  # 确保配置是最新的
            keys = key.split('.')
            value = current_config

            for k in keys:
                if isinstance(value, dict) and k in value:
                    value = value[k]
                else:
                    return default

            return value
        except Exception as e:
            self.logger.error(f"获取配置项 {key} 失败: {e}")
            return default

    def set(self, key: str, value: Any, persist: bool = True) -> bool:
        """设置配置值"""
        try:
            with self._lock:
                current_config = self.load_config()
                keys = key.split('.')
                config = current_config

                # 导航到目标位置
                for k in keys[:-1]:
                    if k not in config:
                        config[k] = {}
                    config = config[k]

                # 设置值
                old_value = config.get(keys[-1])
                config[keys[-1]] = value

                # 如果值发生变化且需要持久化
                if old_value != value and persist:
                    self.save_config()
                    self.logger.info(f"配置项 {key} 已更新: {old_value} -> {value}")

                return True

        except Exception as e:
            self.logger.error(f"设置配置项 {key} 失败: {e}")
            return False

    def update(self, updates: Dict[str, Any], persist: bool = True) -> bool:
        """批量更新配置"""
        try:
            with self._lock:
                old_config = self._config.copy()
                current_config = self.load_config()

                def deep_update(d, u):
                    for k, v in u.items():
                        if isinstance(v, dict):
                            d[k] = deep_update(d.get(k, {}), v)
                        else:
                            d[k] = v
                    return d

                deep_update(current_config, updates)
                validated_config = self._validate_config(current_config)

                self._config = validated_config

                if persist:
                    self.save_config()

                # 触发回调
                if old_config != self._config:
                    self._trigger_callbacks(old_config, self._config)
                    self.logger.info("批量配置更新完成")

                return True

        except Exception as e:
            self.logger.error(f"批量更新配置失败: {e}")
            return False

    def save_config(self) -> bool:
        """保存配置到文件"""
        try:
            with self._lock:
                # 创建备份
                if os.path.exists(self.config_file):
                    backup_file = f"{self.config_file}.backup"
                    try:
                        with open(self.config_file, 'r', encoding='utf-8') as src:
                            with open(backup_file, 'w', encoding='utf-8') as dst:
                                dst.write(src.read())
                    except Exception as e:
                        self.logger.warning(f"创建配置备份失败: {e}")

                # 保存新配置
                with open(self.config_file, 'w', encoding='utf-8') as f:
                    json.dump(self._config, f, ensure_ascii=False, indent=2)

                self._last_modified = os.path.getmtime(self.config_file)
                self.logger.info("配置文件已保存")
                return True

        except Exception as e:
            self.logger.error(f"保存配置文件失败: {e}")
            return False

    def reload(self) -> bool:
        """强制重新加载配置"""
        try:
            self._last_modified = 0  # 强制重新读取
            self.load_config()
            self.logger.info("配置已强制重新加载")
            return True
        except Exception as e:
            self.logger.error(f"强制重新加载配置失败: {e}")
            return False

    def add_callback(self, callback: Callable[[Dict[str, Any], Dict[str, Any]], None]):
        """添加配置变化回调函数"""
        if callback not in self._callbacks:
            self._callbacks.append(callback)
            self.logger.debug("添加配置变化回调函数")

    def remove_callback(self, callback: Callable[[Dict[str, Any], Dict[str, Any]], None]):
        """移除配置变化回调函数"""
        if callback in self._callbacks:
            self._callbacks.remove(callback)
            self.logger.debug("移除配置变化回调函数")

    def _trigger_callbacks(self, old_config: Dict[str, Any], new_config: Dict[str, Any]):
        """触发所有回调函数"""
        for callback in self._callbacks:
            try:
                callback(old_config, new_config)
            except Exception as e:
                self.logger.error(f"执行配置变化回调函数失败: {e}")

    def start_watching(self):
        """启动配置文件监控"""
        if not self._watch_enabled:
            self._watch_enabled = True
            self._watch_thread = threading.Thread(target=self._watch_config_file, daemon=True)
            self._watch_thread.start()
            self.logger.info("配置文件监控已启动")

    def stop_watching(self):
        """停止配置文件监控"""
        self._watch_enabled = False
        if self._watch_thread:
            self._watch_thread.join(timeout=2)
        self.logger.info("配置文件监控已停止")

    def _watch_config_file(self):
        """监控配置文件变化"""
        while self._watch_enabled:
            try:
                if os.path.exists(self.config_file):
                    current_modified = os.path.getmtime(self.config_file)
                    if current_modified > self._last_modified:
                        self.logger.info("检测到配置文件变化，重新加载...")
                        self.load_config()

                time.sleep(self._watch_interval)

            except Exception as e:
                self.logger.error(f"监控配置文件时出错: {e}")
                time.sleep(self._watch_interval)

    def _validate_config(self, config: Dict[str, Any]) -> Dict[str, Any]:
        """验证和标准化配置"""
        default_config = self._get_default_config()

        # 确保所有必要的配置项都存在
        def validate_section(section_name: str, section_data: Dict[str, Any], default_section: Dict[str, Any]):
            for key, default_value in default_section.items():
                if key not in section_data:
                    section_data[key] = default_value
                elif isinstance(default_value, dict) and isinstance(section_data[key], dict):
                    validate_section(key, section_data[key], default_value)

        # 验证各个配置段
        for section_name, default_section in default_config.items():
            if section_name not in config:
                config[section_name] = default_section
            elif isinstance(default_section, dict) and isinstance(config[section_name], dict):
                validate_section(section_name, config[section_name], default_section)

        # 验证特定配置项的值
        if config['limits']['max_file_size'] <= 0:
            config['limits']['max_file_size'] = default_config['limits']['max_file_size']

        if config['limits']['max_history_records'] <= 0:
            config['limits']['max_history_records'] = default_config['limits']['max_history_records']

        return config

    def _get_default_config(self) -> Dict[str, Any]:
        """获取默认配置 - 简化版本仅包含核心配置项"""
        return {
            "app": {
                "host": "0.0.0.0",
                "port": 5000,
                "debug": True
            },
            "storage": {
                "upload_folder": "uploads",
                "download_folder": "downloads",
                "history_file": "history.json"
            },
            "limits": {
                "max_file_size": 104857600,
                "max_history_records": 100
            }
        }

    def get_all(self) -> Dict[str, Any]:
        """获取所有配置（副本）"""
        with self._lock:
            current_config = self.load_config()
            return current_config.copy()

    def get_flask_config(self) -> Dict[str, Any]:
        """获取Flask应用相关配置"""
        return {
            'DEBUG': self.get('app.debug'),
            'MAX_CONTENT_LENGTH': self.get('limits.max_file_size'),
            'UPLOAD_FOLDER': self.get('storage.upload_folder'),
            'DOWNLOAD_FOLDER': self.get('storage.download_folder')
        }

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.stop_watching()

# 全局配置管理器实例
config_manager = ConfigManager()

# 便捷函数
def get_config(key: str, default: Any = None) -> Any:
    """获取配置值的便捷函数"""
    return config_manager.get(key, default)

def set_config(key: str, value: Any, persist: bool = True) -> bool:
    """设置配置值的便捷函数"""
    return config_manager.set(key, value, persist)

def reload_config() -> bool:
    """重新加载配置的便捷函数"""
    return config_manager.reload()