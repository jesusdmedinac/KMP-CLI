# Installation

`kmp-cli` is compiled as a high-performance native binary using Kotlin/Native, meaning it starts instantaneously without JVM warmup overhead.

---

## Package Managers

=== "Homebrew (macOS & Linux)"
    Install using our official tap:
    ```bash
    brew tap jesusdmedinac/tap
    brew install kmp
    ```
    To upgrade:
    ```bash
    brew update && brew upgrade kmp
    ```

=== "SDKMAN! (JVM Ecosystem)"
    If you use SDKMAN! to manage Java and Kotlin SDKs:
    ```bash
    sdk install kmp
    ```

=== "Curl One-Liner (macOS & Linux)"
    To automatically detect your CPU architecture (Apple Silicon / Intel / Linux x64) and install `kmp` into `~/.local/bin`:
    ```bash
    curl -fsSL https://raw.githubusercontent.com/jesusdmedinac/KMP-CLI/main/install.sh | bash
    ```

---

## Prebuilt Binaries (GitHub Releases)

Download precompiled standalone executables directly from the [Releases page](https://github.com/jesusdmedinac/KMP-CLI/releases):

| Platform | Architecture | Binary Asset |
| :--- | :--- | :--- |
| **macOS** | Apple Silicon (`arm64`) | `kmp-macos-arm64.tar.gz` |
| **macOS** | Intel (`x86_64`) | `kmp-macos-x64.tar.gz` |
| **Linux** | AMD64 (`x86_64`) | `kmp-linux-x64.tar.gz` |
| **Windows** | x86_64 (`mingwX64`) | `kmp-windows-x64.zip` |

Extract and place the executable in your system `$PATH` (e.g. `/usr/local/bin` or `~/.local/bin`).

---

## Building from Source

To compile `kmp-cli` directly from source:

1. Clone the repository:
   ```bash
   git clone https://github.com/jesusdmedinac/KMP-CLI.git
   cd KMP-CLI
   ```
2. Build the native release executable:
   ```bash
   ./gradlew :kmp-cli:linkReleaseExecutableNative
   ```
3. The binary will be produced at:
   ```text
   kmp-cli/build/bin/<target>/releaseExecutable/kmp.kexe
   ```
   Add a symlink to your `$PATH`:
   ```bash
   ln -s "$(pwd)/kmp-cli/build/bin/macosArm64/releaseExecutable/kmp.kexe" ~/.local/bin/kmp
   ```

---

## Verify Installation

Confirm that `kmp` is properly installed and accessible:

```bash
kmp --version
```
Output:
```text
kmp version 1.0.0
```
