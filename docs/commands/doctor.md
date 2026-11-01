# `kmp doctor`

Diagnose your development workstation environment, identify missing toolchains, and verify readiness for Kotlin Multiplatform development across Android, iOS, Desktop, and Web.

---

## Usage

```bash
kmp doctor [options]
```

## Options

- `--json`: Output machine-readable JSON representation of the diagnostic tree. Essential for AI agents and CI/CD pipelines.

---

## What It Checks

1. **Java Development Kit (JDK)**:
   - Verifies `$JAVA_HOME` configuration.
   - Ensures JDK version is 17 or higher (21 recommended).
2. **Android Toolchain**:
   - Locates `$ANDROID_HOME` or `$ANDROID_SDK_ROOT`.
   - Audits installed build-tools, platforms (API 34/35), and SDK manager.
3. **Apple iOS & macOS Toolchain**:
   - Inspects Xcode installation and active developer directory (`xcode-select`).
   - Validates command-line developer tools.
   - Checks CocoaPods installation and version compatibility.
4. **Web & Wasm Toolchain**:
   - Checks Node.js runtime and version.
   - Audits WebAssembly compatibility.
5. **Declarative Kotlin Toolchain 0.12+**:
   - Checks for standalone Amper / toolchain runner availability.

---

## Example Outputs

### Human-Readable Terminal Mode

```text
🔍 Kotlin Multiplatform Environment Diagnostics

Java Development Kit (JDK):
  ✓ Java Home: /Users/dev/.sdkman/candidates/java/21.0.10-amzn
  ✓ Version: 21.0.10 (Amazon.com Inc.)

Android Ecosystem:
  ✓ Android SDK: /Users/dev/Library/Android/sdk
  ✓ Target SDKs: android-34, android-35
  ✓ Build Tools: 35.0.0

Apple Ecosystem:
  ✓ Xcode: 16.2 (Build 16C5032a)
  ✓ CocoaPods: 1.16.2

Web & Wasm:
  ✓ Node.js: v22.14.0

Conclusion:
  🚀 Ready to build Kotlin Multiplatform projects!
```

### JSON Mode (`--json`)

```json
{
  "status": "HEALTHY",
  "checks": [
    {
      "name": "JDK",
      "status": "PASS",
      "version": "21.0.10",
      "path": "/Users/dev/.sdkman/candidates/java/21.0.10-amzn"
    },
    {
      "name": "Android SDK",
      "status": "PASS",
      "version": "35.0.0",
      "path": "/Users/dev/Library/Android/sdk"
    },
    {
      "name": "Xcode",
      "status": "PASS",
      "version": "16.2",
      "path": "/Applications/Xcode.app"
    }
  ]
}
```
