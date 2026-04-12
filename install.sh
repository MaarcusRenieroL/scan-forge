#!/bin/bash

set -e

INSTALL_DIR="$HOME/.scanforge"
BIN_DIR="$HOME/bin"
JAR_URL="https://github.com/MaarcusRenieroL/scan-forge/releases/download/v1.0.0/scanforge.jar"

echo "Installing scanforge..."

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"

curl -L "$JAR_URL" -o "$INSTALL_DIR/scanforge.jar"

cat << 'EOF' > "$BIN_DIR/scanforge"
#!/bin/bash
java -jar "$HOME/.scanforge/scanforge.jar" "$@"
EOF

chmod +x "$BIN_DIR/scanforge"

if ! echo "$PATH" | grep -q "$HOME/bin"; then
  echo 'export PATH="$HOME/bin:$PATH"' >> "$HOME/.zshrc"
  echo "Run: source ~/.zshrc"
fi

echo "Done. Run: scanforge"
