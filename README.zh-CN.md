# Intl GUI

Intl GUI 是一个 IntelliJ Platform 插件，用于以可视化方式管理 Flutter Intl国际化资源。

## 功能特性

- 自动识别 `lib/l10n` 目录下的 ARB 文件
- 按语言列展示所有翻译 Key
- 直接在表格单元格中编辑翻译并保存到 ARB 文件
- 通过统一弹窗为所有语言文件新增同一个 Key
- 在工具窗口中一键刷新项目中的国际化数据

## 环境要求

- IntelliJ Platform 2025.1 及以上
- JDK 21
- 目标 Flutter 项目使用 `lib/l10n` 目录存放 ARB 文件

## 使用说明

1. 在 IntelliJ IDEA 中打开 Flutter 项目。
2. 打开右侧的 **Intl GUI** 工具窗口。
3. 在表格中直接编辑翻译内容。
4. 点击 **新增字段**，为所有语言文件添加新字段。
5. 点击 **刷新**，重新加载磁盘中的 ARB 文件。

## 开发说明

### 构建插件

```bash
./gradlew buildPlugin
```

### 在沙盒 IDE 中运行

```bash
./gradlew runIde
```

## 项目结构

- `src/main/kotlin/cn/lsmya/intl_gui`：插件核心逻辑与界面
- `src/main/resources/META-INF/plugin.xml`：插件元信息与扩展点注册

## 许可证

本项目采用 MIT License，详见根目录 LICENSE 文件。
