# Intl GUI

Intl GUI is an IntelliJ Platform plugin for managing Flutter Intl internationalization resources in a visual editor.

## Features

- Discover ARB files in `lib/l10n`
- Display translation keys in a table across languages
- Edit translation values directly in cells and save back to ARB files
- Add a new key for all language files from one dialog
- Refresh data from project files in the tool window

## Requirements

- IntelliJ Platform 2025.1+
- JDK 21
- Flutter project with ARB files under `lib/l10n`

## Usage

1. Open a Flutter project in IntelliJ IDEA.
2. Open the **Intl GUI** tool window on the right side.
3. Edit translations in the table.
4. Click **新增字段** to create a key for all language files.
5. Click **刷新** to reload ARB files from disk.

## Development

### Build plugin

```bash
./gradlew buildPlugin
```

### Run in sandbox IDE

```bash
./gradlew runIde
```

## Project Structure

- `src/main/kotlin/cn/lsmya/intl_gui` core plugin logic and UI
- `src/main/resources/META-INF/plugin.xml` plugin metadata and registrations

## License

This project is licensed under the MIT License. See the LICENSE file for details.
