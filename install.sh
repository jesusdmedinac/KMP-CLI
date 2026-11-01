#!/usr/bin/env bash
set -e

# ==============================================================================
# KMP CLI Unified Installer
# Supported: macOS (Apple Silicon / Intel), Linux (x86_64)
# ==============================================================================

REPO="jesusdmedinac/KMP-CLI"
INSTALL_DIR="${KMP_INSTALL_DIR:-$HOME/.local/bin}"

echo "🚀 Installing KMP CLI..."

OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH="$(uname -m)"

case "$OS" in
  darwin)
    case "$ARCH" in
      arm64) TARGET="macos-arm64" ;;
      x86_64) TARGET="macos-x64" ;;
      *) echo "❌ Unsupported macOS architecture: $ARCH" && exit 1 ;;
    esac
    ;;
  linux)
    case "$ARCH" in
      x86_64|amd64) TARGET="linux-x64" ;;
      *) echo "❌ Unsupported Linux architecture: $ARCH" && exit 1 ;;
    esac
    ;;
  *)
    echo "❌ Unsupported operating system: $OS"
    exit 1
    ;;
esac

echo "✓ Detected platform: $OS ($TARGET)"

LATEST_RELEASE_URL="https://api.github.com/repos/$REPO/releases/latest"
TAG=$(curl -sSL "$LATEST_RELEASE_URL" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$TAG" ]; then
  TAG="v1.0.0"
fi

DOWNLOAD_URL="https://github.com/$REPO/releases/download/$TAG/kmp-$TARGET.tar.gz"
TMP_DIR=$(mktemp -d)

echo "⬇️ Downloading KMP CLI ($TAG)..."
if curl -fsSL "$DOWNLOAD_URL" -o "$TMP_DIR/kmp.tar.gz"; then
  tar -xzf "$TMP_DIR/kmp.tar.gz" -C "$TMP_DIR"
  mkdir -p "$INSTALL_DIR"
  mv "$TMP_DIR/kmp" "$INSTALL_DIR/kmp"
  chmod +x "$INSTALL_DIR/kmp"
  rm -rf "$TMP_DIR"
else
  echo "⚠️ Release asset not found for $TAG. Attempting fallback or building from local source."
  rm -rf "$TMP_DIR"
  exit 1
fi

echo ""
echo "🎉 KMP CLI successfully installed to: $INSTALL_DIR/kmp"
echo ""

if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
  echo "👉 Add $INSTALL_DIR to your PATH by adding this line to your ~/.zshrc or ~/.bashrc:"
  echo "   export PATH=\"\$PATH:$INSTALL_DIR\""
  echo ""
fi

echo "Run 'kmp doctor' to verify your multiplatform development environment!"
