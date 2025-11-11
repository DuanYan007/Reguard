"""
文件迁移模块 - 支持配置变更时的原子性文件迁移
"""
import os
import shutil
import json
import logging
from typing import Dict, List, Optional, Tuple
from datetime import datetime

class FileMigrationResult:
    """文件迁移结果"""
    def __init__(self, success: bool = False, message: str = "", migrated_files: List[str] = None, rollback_files: List[str] = None):
        self.success = success
        self.message = message
        self.migrated_files = migrated_files or []
        self.rollback_files = rollback_files or []
        self.timestamp = datetime.now().isoformat()

class FileMigrator:
    """文件迁移器"""

    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.migration_log = []

    def migrate_storage_directories(self, old_config: Dict, new_config: Dict) -> FileMigrationResult:
        """
        迁移存储目录（只迁移发生变化的路径）

        Args:
            old_config: 旧配置
            new_config: 新配置

        Returns:
            FileMigrationResult: 迁移结果
        """
        try:
            # 获取旧的和新的存储路径
            old_upload = old_config.get('storage', {}).get('upload_folder', 'uploads')
            old_download = old_config.get('storage', {}).get('download_folder', 'downloads')
            old_history = old_config.get('storage', {}).get('history_file', 'history.json')

            new_upload = new_config.get('storage', {}).get('upload_folder', 'uploads')
            new_download = new_config.get('storage', {}).get('download_folder', 'downloads')
            new_history = new_config.get('storage', {}).get('history_file', 'history.json')

            # 处理相对路径转换为绝对路径
            if not os.path.isabs(old_upload):
                old_upload = os.path.abspath(old_upload)
            if not os.path.isabs(old_download):
                old_download = os.path.abspath(old_download)
            if not os.path.isabs(old_history):
                old_history = os.path.abspath(old_history)

            if not os.path.isabs(new_upload):
                new_upload = os.path.abspath(new_upload)
            if not os.path.isabs(new_download):
                new_download = os.path.abspath(new_download)
            if not os.path.isabs(new_history):
                new_history = os.path.abspath(new_history)

            # 检查是否有任何路径需要迁移
            path_changes = {
                'upload': old_upload != new_upload,
                'download': old_download != new_download,
                'history': old_history != new_history
            }

            if not any(path_changes.values()):
                return FileMigrationResult(success=True, message="配置未变更，无需迁移")

            self.logger.info(f"检测到路径变更，将迁移: {path_changes}")

            # 记录迁移信息，用于回滚
            migration_info = {
                'old_paths': {
                    'upload': old_upload,
                    'download': old_download,
                    'history': old_history
                },
                'new_paths': {
                    'upload': new_upload,
                    'download': new_download,
                    'history': new_history
                },
                'migrated_files': []
            }

            # 准备各个路径的迁移结果
            upload_result = {'success': True, 'migrated_files': []}
            download_result = {'success': True, 'migrated_files': []}
            history_result = {'success': True, 'migrated_files': []}

            try:
                # 1. 创建需要的新目录
                if path_changes['upload']:
                    os.makedirs(new_upload, exist_ok=True)
                if path_changes['download']:
                    os.makedirs(new_download, exist_ok=True)
                if path_changes['history']:
                    new_history_dir = os.path.dirname(new_history)
                    if new_history_dir:
                        os.makedirs(new_history_dir, exist_ok=True)

                # 2. 迁移上传目录（如果需要）
                if path_changes['upload']:
                    self.logger.info(f"迁移上传目录: {old_upload} -> {new_upload}")
                    upload_result = self._migrate_directory(old_upload, new_upload, "上传目录")
                    if not upload_result['success']:
                        return self._create_failure_result(f"上传目录迁移失败: {upload_result['error']}", migration_info)

                # 3. 迁移下载目录（如果需要）
                if path_changes['download']:
                    self.logger.info(f"迁移下载目录: {old_download} -> {new_download}")
                    download_result = self._migrate_directory(old_download, new_download, "下载目录")
                    if not download_result['success']:
                        # 回滚已完成的迁移
                        if path_changes['upload']:
                            self._rollback_directory_migration(new_upload, old_upload, upload_result['migrated_files'])
                        return self._create_failure_result(f"下载目录迁移失败: {download_result['error']}", migration_info)

                # 4. 迁移历史文件（如果需要）
                if path_changes['history']:
                    self.logger.info(f"迁移历史文件: {old_history} -> {new_history}")
                    history_result = self._migrate_file(old_history, new_history, "历史文件")
                    if not history_result['success']:
                        # 回滚前面所有迁移
                        if path_changes['upload']:
                            self._rollback_directory_migration(new_upload, old_upload, upload_result['migrated_files'])
                        if path_changes['download']:
                            self._rollback_directory_migration(new_download, old_download, download_result['migrated_files'])
                        return self._create_failure_result(f"历史文件迁移失败: {history_result['error']}", migration_info)

                # 5. 更新历史记录中的文件路径（仅当下载目录发生变化时）
                if path_changes['download']:
                    self._update_history_file_paths(new_download)

                # 记录所有迁移的文件
                all_migrated_files = (
                    upload_result['migrated_files'] +
                    download_result['migrated_files'] +
                    history_result['migrated_files']
                )
                migration_info['migrated_files'] = all_migrated_files

                # 记录成功的迁移
                self.migration_log.append({
                    'timestamp': datetime.now().isoformat(),
                    'success': True,
                    'migration_info': migration_info
                })

                # 构建详细的成功消息
                migrated_parts = []
                if path_changes['upload']:
                    migrated_parts.append("上传目录")
                if path_changes['download']:
                    migrated_parts.append("下载目录")
                if path_changes['history']:
                    migrated_parts.append("历史文件")

                migration_summary = ", ".join(migrated_parts) if migrated_parts else "无文件"

                self.logger.info(f"文件迁移成功完成，共迁移 {len(all_migrated_files)} 个文件，涉及: {migration_summary}")
                return FileMigrationResult(
                    success=True,
                    message=f"文件迁移成功，涉及: {migration_summary}",
                    migrated_files=all_migrated_files
                )

            except Exception as e:
                # 如果出现异常，尝试回滚
                self.logger.error(f"迁移过程中出现异常: {str(e)}")
                return self._create_failure_result(f"迁移过程异常: {str(e)}", migration_info)

        except Exception as e:
            self.logger.error(f"配置迁移失败: {str(e)}")
            return FileMigrationResult(success=False, message=f"配置迁移失败: {str(e)}")

    def _migrate_directory(self, src_dir: str, dst_dir: str, dir_name: str) -> Dict:
        """迁移整个目录（移动文件）"""
        try:
            if not os.path.exists(src_dir):
                return {'success': True, 'migrated_files': [], 'message': f'{dir_name}源目录不存在'}

            migrated_files = []
            removed_files = []

            # 迁移文件和子目录
            for item in os.listdir(src_dir):
                src_path = os.path.join(src_dir, item)
                dst_path = os.path.join(dst_dir, item)

                if os.path.isfile(src_path):
                    # 移动文件
                    shutil.move(src_path, dst_path)
                    migrated_files.append(dst_path)
                    removed_files.append(src_path)
                elif os.path.isdir(src_path):
                    # 递归迁移子目录
                    # 确保目标子目录存在
                    os.makedirs(dst_path, exist_ok=True)
                    sub_result = self._migrate_directory(src_path, dst_path, f"{dir_name}/{item}")
                    if not sub_result['success']:
                        # 回滚：将已移动的文件移回原位置
                        for moved_file, original_file in zip(migrated_files, removed_files):
                            if os.path.exists(moved_file):
                                shutil.move(moved_file, original_file)
                        return sub_result
                    migrated_files.extend(sub_result['migrated_files'])

            # 尝试删除空的源目录
            try:
                if os.path.exists(src_dir) and not os.listdir(src_dir):
                    os.rmdir(src_dir)
                    self.logger.info(f"已删除空的源目录: {src_dir}")
            except OSError:
                self.logger.info(f"源目录非空，保留目录: {src_dir}")

            return {
                'success': True,
                'migrated_files': migrated_files,
                'message': f'{dir_name}迁移成功'
            }

        except Exception as e:
            return {'success': False, 'error': str(e), 'migrated_files': []}

    def _migrate_file(self, src_file: str, dst_file: str, file_name: str) -> Dict:
        """迁移单个文件（移动）"""
        try:
            if not os.path.exists(src_file):
                return {'success': True, 'migrated_files': [], 'message': f'{file_name}源文件不存在'}

            # 移动文件
            shutil.move(src_file, dst_file)
            return {
                'success': True,
                'migrated_files': [dst_file],
                'message': f'{file_name}迁移成功'
            }

        except Exception as e:
            return {'success': False, 'error': str(e), 'migrated_files': []}

    def _rollback_directory_migration(self, src_dir: str, dst_dir: str, migrated_files: List[str]):
        """回滚目录迁移（将文件移回原位置）"""
        try:
            # 将已移动的文件移回原目录
            for file_path in migrated_files:
                if os.path.exists(file_path):
                    # 提取文件名
                    filename = os.path.basename(file_path)
                    original_path = os.path.join(dst_dir, filename)

                    # 确保原目录存在
                    os.makedirs(dst_dir, exist_ok=True)

                    # 将文件移回原位置
                    shutil.move(file_path, original_path)
                    self.logger.info(f"已回滚文件: {file_path} -> {original_path}")

            # 尝试删除空的目标目录
            if os.path.exists(src_dir):
                try:
                    if not os.listdir(src_dir):
                        os.rmdir(src_dir)
                        self.logger.info(f"已删除空的目标目录: {src_dir}")
                except OSError:
                    pass  # 目录非空，不删除

        except Exception as e:
            self.logger.error(f"回滚目录迁移失败: {str(e)}")

    def _update_history_file_paths(self, new_download_dir: str):
        """更新历史记录中的文件路径"""
        try:
            history_file = os.path.join(os.path.dirname(new_download_dir), 'history.json')
            if not os.path.exists(history_file):
                return

            with open(history_file, 'r', encoding='utf-8') as f:
                history = json.load(f)

            updated = False
            for record in history:
                old_path = record.get('md_file_path', '')
                if old_path and os.path.isabs(old_path):
                    # 提取文件名并构建新路径
                    filename = os.path.basename(old_path)
                    new_path = os.path.join(new_download_dir, filename)
                    record['md_file_path'] = new_path
                    record['download_url'] = f"/download-md?file_path={new_path}&filename={filename}"
                    updated = True

            if updated:
                with open(history_file, 'w', encoding='utf-8') as f:
                    json.dump(history, f, ensure_ascii=False, indent=2)
                self.logger.info("已更新历史记录中的文件路径")

        except Exception as e:
            self.logger.error(f"更新历史记录路径失败: {str(e)}")

    def _create_failure_result(self, error_message: str, migration_info: Dict) -> FileMigrationResult:
        """创建失败的迁移结果并记录日志"""
        self.logger.error(error_message)

        # 记录失败的迁移尝试
        self.migration_log.append({
            'timestamp': datetime.now().isoformat(),
            'success': False,
            'error': error_message,
            'migration_info': migration_info
        })

        return FileMigrationResult(success=False, message=error_message)

    def validate_migration_paths(self, old_config: Dict, new_config: Dict) -> Tuple[bool, str]:
        """验证迁移路径的合法性"""
        try:
            # 检查目标路径是否可写
            old_upload = old_config.get('storage', {}).get('upload_folder', 'uploads')
            old_download = old_config.get('storage', {}).get('download_folder', 'downloads')

            new_upload = new_config.get('storage', {}).get('upload_folder', 'uploads')
            new_download = new_config.get('storage', {}).get('download_folder', 'downloads')

            # 处理绝对路径
            if not os.path.isabs(new_upload):
                new_upload = os.path.abspath(new_upload)
            if not os.path.isabs(new_download):
                new_download = os.path.abspath(new_download)

            # 检查新路径的父目录是否可写
            for path in [new_upload, new_download]:
                parent_dir = os.path.dirname(path)
                if not os.path.exists(parent_dir):
                    try:
                        os.makedirs(parent_dir, exist_ok=True)
                    except Exception as e:
                        return False, f"无法创建目标目录: {parent_dir}, 错误: {str(e)}"

                # 测试写入权限
                test_file = os.path.join(parent_dir, f".markitdown_test_{datetime.now().timestamp()}")
                try:
                    with open(test_file, 'w') as f:
                        f.write("test")
                    os.remove(test_file)
                except Exception as e:
                    return False, f"目标路径无写入权限: {parent_dir}, 错误: {str(e)}"

            # 检查是否会发生循环迁移
            if (os.path.abspath(old_upload) == os.path.abspath(new_download) or
                os.path.abspath(old_download) == os.path.abspath(new_upload)):
                return False, "不能将上传目录和下载目录设置为对方的路径"

            return True, "路径验证通过"

        except Exception as e:
            return False, f"路径验证失败: {str(e)}"

# 全局文件迁移器实例
file_migrator = FileMigrator()