#!/usr/bin/env python3
"""
APK构建脚本 - 用于Termux环境
由于aapt2等工具在ARM64 Android环境无法运行，此脚本提供手动构建指引
"""

import os
import subprocess
import sys

PROJECT_DIR = "/data/data/com.termux/files/home/ai-assistant-float"
BUILD_DIR = os.path.join(PROJECT_DIR, "app/build/outputs/apk/debug")
APK_PATH = os.path.join(BUILD_DIR, "app-debug.apk")

def print_info():
    """打印构建信息"""
    print("=" * 60)
    print("AI语音悬浮助手 - APK构建指南")
    print("=" * 60)
    print()
    print("⚠️  注意：Termux Android环境限制")
    print()
    print("当前环境的架构限制导致标准Android构建工具无法运行：")
    print("  - aapt2: 需要x86/x86_64架构")
    print("  - 当前架构: ARM64 (aarch64)")
    print()
    print("✅ 解决方案：在有完整开发环境的电脑上构建")
    print()
    print("=" * 60)
    print("构建方法：")
    print("=" * 60)
    print()
    print("方法1: 在Windows/Mac/Linux电脑上构建")
    print("  1. 将项目目录复制到电脑")
    print("  2. 确保已安装:")
    print("     - JDK 8或更高版本")
    print("     - Android SDK")
    print("     - Android Build Tools 35.0.0")
    print("  3. 配置local.properties:")
    print("     sdk.dir=<你的Android SDK路径>")
    print("  4. 运行构建命令:")
    print("     ./gradlew assembleDebug")
    print("     或")
    print("     gradle assembleDebug")
    print()
    print("方法2: 使用在线Android构建服务")
    print("  - GitHub Actions")
    print("  - GitLab CI/CD")
    print("  - 其他CI/CD平台")
    print()
    print("=" * 60)
    print("项目文件检查：")
    print("=" * 60)
    
    # 检查关键文件
    files_to_check = [
        ("AndroidManifest.xml", "app/src/main/AndroidManifest.xml"),
        ("MainActivity.java", "app/src/main/java/com/aifloat/MainActivity.java"),
        ("FloatWindowService.java", "app/src/main/java/com/aifloat/FloatWindowService.java"),
        ("build.gradle (app)", "app/build.gradle"),
        ("build.gradle (root)", "build.gradle"),
        ("settings.gradle", "settings.gradle"),
    ]
    
    all_exist = True
    for name, path in files_to_check:
        full_path = os.path.join(PROJECT_DIR, path)
        exists = os.path.exists(full_path)
        status = "✅" if exists else "❌"
        print(f"{status} {name:30s} -> {path}")
        if not exists:
            all_exist = False
    
    print()
    if all_exist:
        print("✅ 所有项目文件都已就绪！")
    else:
        print("❌ 部分文件缺失")
    
    print()
    print("=" * 60)
    print("项目特性：")
    print("=" * 60)
    print("✅ 悬浮窗权限管理")
    print("✅ 圆形AI悬浮按钮")
    print("✅ 点击开始/停止语音对话")
    print("✅ 可打断语音识别")
    print("✅ 长按弹出透明细线圆角菜单")
    print("✅ 设置页面（API密钥、模型选择）")
    print("✅ DMX API集成")
    print("✅ 支持多种AI模型")
    print()
    print("=" * 60)
    print("支持的AI模型：")
    print("=" * 60)
    print("  - qwen-flash (免费，默认)")
    print("  - qwen3-8B (免费)")
    print("  - deepseek-v3")
    print("  - hunyuan-t1-20250321")
    print("  - Doubao-1.5-pro-32k")
    print("  - glm-4")
    print()
    print("=" * 60)
    print("API配置：")
    print("=" * 60)
    print("  API密钥: sk-UsgwFkmVN540vyqSCkAXFUOSOee36ciBync1sWz8vG91Bpg0")
    print("  API端点: https://api.dmxapi.cn/v1/chat/completions")
    print("  默认模型: qwen-flash")
    print()
    print("=" * 60)

if __name__ == "__main__":
    print_info()