# 🚀 ScanForge

ScanForge is a fast, interactive CLI tool that scans your project, lets you select files or extensions, and copies their contents directly to your clipboard.

---

## ✨ Features

* ⚡ **Interactive Terminal UI**

  * Arrow key navigation
  * Multi-select with space
  * Live search with fuzzy matching

* 🎯 **Dual Selection Modes**

  * Select by **file extensions** (bulk)
  * Select individual **files**
  * Switch modes with `TAB`

* 🔍 **Smart Search**

  * Real-time filtering
  * Fuzzy matching
  * Works across extensions and file paths

* 🚫 **.gitignore Support**

  * Automatically skips ignored files/folders
  * Handles common cases like:

    * `node_modules`
    * `.git`
    * `target`, `build`

* 📋 **Clipboard Integration**

  * Copies output directly using `pbcopy`

* 🧠 **Binary Detection**

  * Skips non-text files automatically

---

## ⚙️ Installation

```bash
curl -fsSL https://raw.githubusercontent.com/MaarcusRenieroL/scan-forge/main/install.sh | bash
```

---

## 🚀 Usage

### Run interactive mode

```bash
scanforge
```

### Scan current directory

```bash
scanforge .
```

### Filter by extensions

```bash
scanforge java txt
```

### Path + extensions

```bash
scanforge . java txt
```

---

## 🎮 Controls

```text
↑ ↓    Navigate
SPACE  Select
TAB    Switch mode (Extensions / Files)
ENTER  Confirm
BACKSPACE  Delete search
```

---

## 📂 Example Output

```text
===== file: /project/src/Main.java =====
public class Main {
  public static void main(String[] args) {
    System.out.println("Hello World");
  }
}
```

---

## ⚠️ Requirements

* Java 17+
* macOS (uses `pbcopy` for clipboard)

---

## 🧠 How It Works

* Scans directory recursively
* Applies `.gitignore` rules
* Lets you select extensions/files via UI
* Streams file contents
* Copies everything to clipboard

---

## 🔮 Roadmap

* Cross-platform clipboard (Linux / Windows)
* `--clip` / `--print` modes
* Performance improvements for large repos
* Config file support
* Auto-update command

---

## 🏷️ Version

```text
v1.0.0
```

---

## 🤝 Contributing

PRs are welcome. For major changes, open an issue first.

---

## 📜 License

MIT
