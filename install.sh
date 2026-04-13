#!/bin/bash

set -e

INSTALL_DIR="$HOME/.scanforge"
BIN_DIR="$HOME/bin"
JAR_URL="https://github.com/MaarcusRenieroL/scan-forge/releases/download/v1.0.0/scanforge.jar"

echo "🚀 Installing scanforge..."

# Check Java
if ! command -v java >/dev/null 2>&1; then
  echo "❌ Java is not installed. Please install Java 17+"
  exit 1
fi

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"

echo "⬇️ Downloading latest version..."
curl -fsSL "$JAR_URL" -o "$INSTALL_DIR/scanforge.jar"

echo "⚙️ Creating CLI command..."
cat << 'EOF' > "$BIN_DIR/scanforge"
#!/bin/bash
java -jar "$HOME/.scanforge/scanforge.jar" "$@"
EOF

chmod +x "$BIN_DIR/scanforge"

# Detect shell config
SHELL_RC=""
if [ -n "$ZSH_VERSION" ]; then
  SHELL_RC="$HOME/.zshrc"
elif [ -n "$BASH_VERSION" ]; then
  SHELL_RC="$HOME/.bashrc"
else
  SHELL_RC="$HOME/.profile"
fi

# Add PATH only if missing
if ! echo "$PATH" | grep -q "$HOME/bin"; then
  echo "🔧 Adding ~/bin to PATH..."
  echo 'export PATH="$HOME/bin:$PATH"' >> "$SHELL_RC"
  echo "⚠️ Restart terminal or run: source $SHELL_RC"
fi

echo ""
echo "✅ Installation complete!"
echo "👉 Run: scanforge"
