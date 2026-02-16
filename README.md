# AI语音悬浮助手

桌面可打断语音识别对话AI悬浮按钮应用。

## 功能特性

- ✅ 悬浮窗显示圆形AI图标
- ✅ 点击开始/停止语音对话
- ✅ 长按弹出透明细线圆角长方形菜单
- ✅ 菜单包含设置和关闭选项
- ✅ 语音识别支持可打断
- ✅ 集成DMX API调用
- ✅ 支持多种AI模型选择
- ✅ 首次打开引导用户设置悬浮窗权限

## 支持的模型

- qwen-flash（阿里通义千问极速响应，免费）
- qwen3-8B（阿里通义千问8B，免费）
- deepseek-v3（DeepSeek V3）
- hunyuan-t1-20250321（腾讯混元）
- Doubao-1.5-pro-32k（字节豆包）
- glm-4（智谱GLM-4）

## 环境要求

- Android SDK 26+
- Java 8+
- Gradle 8.2+

## 构建方法

```bash
cd ai-assistant-float
./gradlew assembleDebug
```

生成的APK位于：`app/build/outputs/apk/debug/app-debug.apk`

## 权限说明

- SYSTEM_ALERT_WINDOW：悬浮窗权限
- RECORD_AUDIO：录音权限
- INTERNET：网络访问权限

## 使用说明

1. 首次启动应用，会引导用户授予悬浮窗和录音权限
2. 授予权限后，点击"启动AI助手"按钮
3. 悬浮窗会在屏幕上显示圆形AI图标
4. **单击**悬浮按钮：开始/停止语音对话
5. **长按**悬浮按钮：弹出菜单（设置/关闭）
6. 点击"设置"可以配置API密钥和选择模型

## 配置说明

默认API密钥已预配置，可在设置中修改：
- API密钥：sk-UsgwFkmVN540vyqSCkAXFUOSOee36ciBync1sWz8vG91Bpg0
- 默认模型：qwen-flash

## 项目结构

```
ai-assistant-float/
├── app/
│   ├── src/main/
│   │   ├── java/com/aifloat/
│   │   │   ├── MainActivity.java          # 主Activity
│   │   │   ├── GuideActivity.java         # 权限引导页
│   │   │   ├── SettingsActivity.java      # 设置页面
│   │   │   └── FloatWindowService.java    # 悬浮窗服务
│   │   ├── res/
│   │   │   ├── layout/                    # 布局文件
│   │   │   ├── values/                    # 资源文件
│   │   │   └── drawable/                  # 可绘制资源
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 技术栈

- Android SDK
- Java
- SpeechRecognizer（语音识别）
- OkHttp（HTTP请求）
- Material Design（UI组件）

## 注意事项

- 需要授予悬浮窗和录音权限才能正常使用
- 语音识别需要网络连接
- API调用需要有效的API密钥
- 首次使用建议在设置中确认API密钥正确性