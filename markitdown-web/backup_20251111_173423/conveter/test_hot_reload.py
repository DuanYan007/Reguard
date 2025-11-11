#!/usr/bin/env python3
"""
配置热部署测试脚本
用于验证配置管理功能和热更新是否正常工作
"""

import json
import time
import requests
import os
from datetime import datetime

class ConfigHotReloadTester:
    def __init__(self, base_url="http://localhost:5000"):
        self.base_url = base_url
        self.session = requests.Session()

    def log(self, message, level="INFO"):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}] [{level}] {message}")

    def test_api_connection(self):
        """测试API连接"""
        self.log("测试API连接...")
        try:
            response = self.session.get(f"{self.base_url}/api/config")
            if response.status_code == 200:
                self.log("API连接成功", "SUCCESS")
                return True
            else:
                self.log(f"API连接失败: {response.status_code}", "ERROR")
                return False
        except Exception as e:
            self.log(f"API连接异常: {e}", "ERROR")
            return False

    def test_get_config(self):
        """测试获取配置"""
        self.log("测试获取配置...")
        try:
            response = self.session.get(f"{self.base_url}/api/config")
            if response.status_code == 200:
                config = response.json()
                if config.get('success'):
                    self.log("获取配置成功", "SUCCESS")
                    self.log(f"当前配置项数量: {len(config.get('config', {}))}")
                    return config.get('config')
                else:
                    self.log(f"获取配置失败: {config.get('message')}", "ERROR")
                    return None
            else:
                self.log(f"获取配置HTTP错误: {response.status_code}", "ERROR")
                return None
        except Exception as e:
            self.log(f"获取配置异常: {e}", "ERROR")
            return None

    def test_update_config(self, config_updates):
        """测试更新配置"""
        self.log(f"测试更新配置: {config_updates}")
        try:
            response = self.session.put(
                f"{self.base_url}/api/config",
                json=config_updates,
                headers={'Content-Type': 'application/json'}
            )
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    self.log("更新配置成功", "SUCCESS")
                    return True
                else:
                    self.log(f"更新配置失败: {result.get('message')}", "ERROR")
                    return False
            else:
                self.log(f"更新配置HTTP错误: {response.status_code}", "ERROR")
                return False
        except Exception as e:
            self.log(f"更新配置异常: {e}", "ERROR")
            return False

    def test_reload_config(self):
        """测试重新加载配置"""
        self.log("测试重新加载配置...")
        try:
            response = self.session.post(f"{self.base_url}/api/config/reload")
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    self.log("重新加载配置成功", "SUCCESS")
                    return True
                else:
                    self.log(f"重新加载配置失败: {result.get('message')}", "ERROR")
                    return False
            else:
                self.log(f"重新加载配置HTTP错误: {response.status_code}", "ERROR")
                return False
        except Exception as e:
            self.log(f"重新加载配置异常: {e}", "ERROR")
            return False

    def test_config_file_hot_reload(self):
        """测试配置文件热重载"""
        self.log("测试配置文件热重载...")

        # 备份原始配置
        original_config = None
        config_file = "config.json"
        backup_file = "config.json.backup"

        try:
            # 读取原始配置
            if os.path.exists(config_file):
                with open(config_file, 'r', encoding='utf-8') as f:
                    original_config = json.load(f)

                # 创建备份
                with open(backup_file, 'w', encoding='utf-8') as f:
                    json.dump(original_config, f, ensure_ascii=False, indent=2)

                self.log("已创建配置文件备份")

            # 修改配置文件
            test_config = original_config.copy() if original_config else {}
            test_config.setdefault('ui', {})['page_title'] = f"测试标题_{int(time.time())}"
            test_config.setdefault('ui', {})['notification_duration'] = 8000

            with open(config_file, 'w', encoding='utf-8') as f:
                json.dump(test_config, f, ensure_ascii=False, indent=2)

            self.log("已修改配置文件，等待热重载...")

            # 等待配置管理器检测到变化
            time.sleep(3)

            # 通过API检查配置是否已更新
            current_config = self.test_get_config()
            if current_config and current_config.get('ui', {}).get('page_title') == test_config['ui']['page_title']:
                self.log("配置文件热重载成功", "SUCCESS")
                success = True
            else:
                self.log("配置文件热重载失败", "ERROR")
                success = False

            # 恢复原始配置
            if original_config:
                with open(config_file, 'w', encoding='utf-8') as f:
                    json.dump(original_config, f, ensure_ascii=False, indent=2)
                self.log("已恢复原始配置")

            # 清理备份文件
            if os.path.exists(backup_file):
                os.remove(backup_file)

            return success

        except Exception as e:
            self.log(f"配置文件热重载测试异常: {e}", "ERROR")

            # 恢复原始配置
            if original_config:
                try:
                    with open(config_file, 'w', encoding='utf-8') as f:
                        json.dump(original_config, f, ensure_ascii=False, indent=2)
                    self.log("已恢复原始配置")
                except:
                    pass

            # 清理备份文件
            if os.path.exists(backup_file):
                try:
                    os.remove(backup_file)
                except:
                    pass

            return False

    def test_supported_formats_api(self):
        """测试支持的文件格式API"""
        self.log("测试支持的文件格式API...")
        try:
            response = self.session.get(f"{self.base_url}/api/formats")
            if response.status_code == 200:
                result = response.json()
                if 'supported_formats' in result:
                    formats = result['supported_formats']
                    self.log(f"获取到 {len(formats)} 种支持格式", "SUCCESS")
                    return True
                else:
                    self.log("API返回格式不正确", "ERROR")
                    return False
            else:
                self.log(f"API请求失败: {response.status_code}", "ERROR")
                return False
        except Exception as e:
            self.log(f"API请求异常: {e}", "ERROR")
            return False

    def run_all_tests(self):
        """运行所有测试"""
        self.log("=" * 50)
        self.log("开始配置热部署功能测试")
        self.log("=" * 50)

        tests_passed = 0
        total_tests = 0

        # 测试1: API连接
        total_tests += 1
        if self.test_api_connection():
            tests_passed += 1
        print()

        # 测试2: 获取配置
        total_tests += 1
        original_config = self.test_get_config()
        if original_config:
            tests_passed += 1
        print()

        # 测试3: 更新配置
        total_tests += 1
        if original_config:
            test_updates = {
                "ui": {
                    "page_title": f"测试更新标题_{int(time.time())}",
                    "notification_duration": 6000
                }
            }
            if self.test_update_config(test_updates):
                tests_passed += 1
        print()

        # 测试4: 重新加载配置
        total_tests += 1
        if self.test_reload_config():
            tests_passed += 1
        print()

        # 测试5: 配置文件热重载
        total_tests += 1
        if self.test_config_file_hot_reload():
            tests_passed += 1
        print()

        # 测试6: 支持的格式API
        total_tests += 1
        if self.test_supported_formats_api():
            tests_passed += 1
        print()

        # 输出测试结果
        self.log("=" * 50)
        self.log(f"测试完成: {tests_passed}/{total_tests} 项测试通过")
        if tests_passed == total_tests:
            self.log("所有测试通过！配置热部署功能正常工作", "SUCCESS")
        else:
            self.log(f"有 {total_tests - tests_passed} 项测试失败", "ERROR")
        self.log("=" * 50)

        return tests_passed == total_tests

def main():
    """主函数"""
    print("MarkItDown Web 配置热部署功能测试")
    print("请确保应用已启动 (python app.py)")
    print()

    tester = ConfigHotReloadTester()

    # 交互式确认
    try:
        input("按 Enter 键开始测试，或 Ctrl+C 退出...")
    except KeyboardInterrupt:
        print("\n测试已取消")
        return

    # 运行测试
    success = tester.run_all_tests()

    # 根据测试结果设置退出码
    exit(0 if success else 1)

if __name__ == "__main__":
    main()