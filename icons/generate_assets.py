#!/usr/bin/env python3
"""
Zora Multiplatform Asset Generator
Generates Android, iOS, Web, Desktop and Compose assets from master Zora icons.
"""

import os
import sys
import shutil
import subprocess
from pathlib import Path

# Paths
BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent
OUTPUT_DIR = BASE_DIR / "generated"

SVG_TRANSPARENT = BASE_DIR / "zora-icon.svg"
SVG_DARK = BASE_DIR / "zora-icon-dark.svg"
PNG_DARK = BASE_DIR / "zora-icon-dark.png"
PNG_WHITE = BASE_DIR / "zora-icon-white.png"
PNG_TRANSPARENT = BASE_DIR / "zora-icon.png"

# Zora Brand Colors
ZORA_PURPLE = "#894CFF"
ZORA_VIOLET = "#A833FD"
ZORA_MAGENTA = "#CD00EB"
ZORA_FUCHSIA = "#E200C0"
ZORA_PINK = "#EC168A"
ZORA_DARK_BG = "#000000"
ZORA_LIGHT_BG = "#FFFFFF"

GRADIENT_STOPS_XML = """        <item android:offset="0.0" android:color="#FF894CFF" />
        <item android:offset="0.25" android:color="#FFA833FD" />
        <item android:offset="0.5" android:color="#FFCD00EB" />
        <item android:offset="0.75" android:color="#FFE200C0" />
        <item android:offset="1.0" android:color="#FFEC168A" />"""


def run_cmd(cmd, cwd=None):
    res = subprocess.run(cmd, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if res.returncode != 0:
        print(f"Error running: {' '.join(cmd)}\n{res.stderr}", file=sys.stderr)
        sys.exit(res.returncode)
    return res.stdout


def create_master_pngs():
    master_dir = OUTPUT_DIR / "master"
    master_dir.mkdir(parents=True, exist_ok=True)

    cropped_1200 = master_dir / "cropped_1200.png"
    master_square = master_dir / "master_square_1024.png"
    master_round = master_dir / "master_round_1024.png"

    # Crop the exact 1200x1200 square icon from master zora-icon-dark.png (removing outer margins)
    run_cmd([
        "convert", str(PNG_DARK),
        "-crop", "1200x1200+128+18", "+repage",
        str(cropped_1200)
    ])

    # Resize to master 1024x1024 square keeping full 32-bit sRGB color
    run_cmd([
        "convert", str(cropped_1200),
        "-resize", "1024x1024",
        str(master_square)
    ])

    # Master 1024x1024 circle
    run_cmd([
        "convert", str(master_square),
        "(", "-size", "1024x1024", "xc:none", "-fill", "white", "-draw", "circle 512,512 512,0", ")",
        "-alpha", "set", "-compose", "DstIn", "-composite",
        str(master_round)
    ])

    return master_square, master_round


def generate_android_assets(master_square, master_round):
    print("Generating Android assets...")
    android_dir = OUTPUT_DIR / "android"
    android_dir.mkdir(parents=True, exist_ok=True)

    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder_name, size in densities.items():
        folder = android_dir / folder_name
        folder.mkdir(parents=True, exist_ok=True)
        # Normal square/rounded
        run_cmd(["convert", str(master_square), "-resize", f"{size}x{size}", str(folder / "ic_launcher.png")])
        # Round icon
        run_cmd(["convert", str(master_round), "-resize", f"{size}x{size}", str(folder / "ic_launcher_round.png")])

    # Google Play Store 512x512
    run_cmd(["convert", str(master_square), "-resize", "512x512", str(android_dir / "play_store_512.png")])

    # Drawable Background XML
    drawable_dir = android_dir / "drawable"
    drawable_dir.mkdir(parents=True, exist_ok=True)
    (drawable_dir / "ic_launcher_background.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{ZORA_DARK_BG}"
        android:pathData="M0,0h108v108h-108z" />
</vector>
""")

    # Drawable Foreground VectorDrawable XML
    drawable_v24_dir = android_dir / "drawable-v24"
    drawable_v24_dir.mkdir(parents=True, exist_ok=True)
    (drawable_v24_dir / "ic_launcher_foreground.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.04118"
        android:scaleY="0.04118"
        android:translateX="24"
        android:translateY="28.551">
        <path
            android:pathData="M728,631L1184.4,1084.75H271.605L728,631Z">
            <aapt:attr name="android:fillColor">
                <gradient
                    android:startX="201"
                    android:startY="631"
                    android:endX="1255"
                    android:endY="631"
                    android:type="linear">
{GRADIENT_STOPS_XML}
                </gradient>
            </aapt:attr>
        </path>
        <path
            android:pathData="M1198.07,627.899L1198.01,1075.15L750.823,627.963L1198.07,627.899Z">
            <aapt:attr name="android:fillColor">
                <gradient
                    android:startX="939.888"
                    android:startY="369.718"
                    android:endX="1456.25"
                    android:endY="886.081"
                    android:type="linear">
{GRADIENT_STOPS_XML}
                </gradient>
            </aapt:attr>
        </path>
        <path
            android:pathData="M728.251,605L271.856,151.25H1184.65L728.251,605Z">
            <aapt:attr name="android:fillColor">
                <gradient
                    android:startX="1255.25"
                    android:startY="605"
                    android:endX="201.251"
                    android:endY="605"
                    android:type="linear">
{GRADIENT_STOPS_XML}
                </gradient>
            </aapt:attr>
        </path>
        <path
            android:pathData="M258.181,608.101L258.245,160.853L705.429,608.037L258.181,608.101Z">
            <aapt:attr name="android:fillColor">
                <gradient
                    android:startX="516.363"
                    android:startY="866.282"
                    android:endX="0"
                    android:endY="349.919"
                    android:type="linear">
{GRADIENT_STOPS_XML}
                </gradient>
            </aapt:attr>
        </path>
    </group>
</vector>
""")

    # Adaptive Icon definitions in mipmap-anydpi-v26
    anydpi_dir = android_dir / "mipmap-anydpi-v26"
    anydpi_dir.mkdir(parents=True, exist_ok=True)
    adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""
    (anydpi_dir / "ic_launcher.xml").write_text(adaptive_xml)
    (anydpi_dir / "ic_launcher_round.xml").write_text(adaptive_xml)

    # Values colors.xml
    values_dir = android_dir / "values"
    values_dir.mkdir(parents=True, exist_ok=True)
    (values_dir / "colors.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="zora_purple">{ZORA_PURPLE}</color>
    <color name="zora_violet">{ZORA_VIOLET}</color>
    <color name="zora_magenta">{ZORA_MAGENTA}</color>
    <color name="zora_fuchsia">{ZORA_FUCHSIA}</color>
    <color name="zora_pink">{ZORA_PINK}</color>
    <color name="zora_dark_background">{ZORA_DARK_BG}</color>
    <color name="zora_light_background">{ZORA_LIGHT_BG}</color>
</resources>
""")


def generate_ios_assets(master_square):
    print("Generating iOS assets...")
    ios_dir = OUTPUT_DIR / "ios" / "AppIcon.appiconset"
    ios_dir.mkdir(parents=True, exist_ok=True)

    # Single universal 1024x1024 for modern Xcode
    run_cmd(["convert", str(master_square), "-resize", "1024x1024", str(ios_dir / "app-icon-1024.png")])

    contents_json = """{
  "images" : [
    {
      "filename" : "app-icon-1024.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "dark"
        }
      ],
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "tinted"
        }
      ],
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""
    (ios_dir / "Contents.json").write_text(contents_json)


def generate_web_and_desktop_assets(master_square):
    print("Generating Web and Desktop assets...")
    web_dir = OUTPUT_DIR / "web"
    web_dir.mkdir(parents=True, exist_ok=True)

    desktop_dir = OUTPUT_DIR / "desktop"
    desktop_dir.mkdir(parents=True, exist_ok=True)

    # Favicon formats
    run_cmd(["convert", str(master_square), "-resize", "16x16", str(web_dir / "favicon-16.png")])
    run_cmd(["convert", str(master_square), "-resize", "32x32", str(web_dir / "favicon-32.png")])
    run_cmd(["convert", str(master_square), "-resize", "48x48", str(web_dir / "favicon-48.png")])
    run_cmd(["convert", str(master_square), "-resize", "180x180", str(web_dir / "apple-touch-icon.png")])
    run_cmd(["convert", str(master_square), "-resize", "192x192", str(web_dir / "icon-192.png")])
    run_cmd(["convert", str(master_square), "-resize", "512x512", str(web_dir / "icon-512.png")])

    # Multi-resolution favicon.ico
    run_cmd([
        "convert",
        str(web_dir / "favicon-16.png"),
        str(web_dir / "favicon-32.png"),
        str(web_dir / "favicon-48.png"),
        str(web_dir / "favicon.ico")
    ])

    # Copy SVG favicon
    shutil.copy(SVG_TRANSPARENT, web_dir / "favicon.svg")

    # Windows icon.ico (including 256x256)
    run_cmd(["convert", str(master_square), "-resize", "256x256", str(desktop_dir / "icon-256.png")])
    run_cmd([
        "convert",
        str(web_dir / "favicon-16.png"),
        str(web_dir / "favicon-32.png"),
        str(web_dir / "favicon-48.png"),
        str(desktop_dir / "icon-256.png"),
        str(desktop_dir / "icon.ico")
    ])

    # macOS icon.icns via iconutil
    iconset_dir = desktop_dir / "icon.iconset"
    iconset_dir.mkdir(parents=True, exist_ok=True)
    iconset_specs = [
        ("icon_16x16.png", 16),
        ("icon_16x16@2x.png", 32),
        ("icon_32x32.png", 32),
        ("icon_32x32@2x.png", 64),
        ("icon_128x128.png", 128),
        ("icon_128x128@2x.png", 256),
        ("icon_256x256.png", 256),
        ("icon_256x256@2x.png", 512),
        ("icon_512x512.png", 512),
        ("icon_512x512@2x.png", 1024),
    ]
    for filename, px in iconset_specs:
        run_cmd(["convert", str(master_square), "-resize", f"{px}x{px}", str(iconset_dir / filename)])

    if shutil.which("iconutil"):
        run_cmd(["iconutil", "-c", "icns", str(iconset_dir), "-o", str(desktop_dir / "icon.icns")])
        shutil.rmtree(iconset_dir, ignore_errors=True)


def generate_compose_assets():
    print("Generating Compose Multiplatform assets...")
    compose_dir = OUTPUT_DIR / "compose"
    compose_dir.mkdir(parents=True, exist_ok=True)

    # VectorDrawable for Compose Resources
    zora_icon_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="1457dp"
    android:height="1236dp"
    android:viewportWidth="1457"
    android:viewportHeight="1236">
    <path
        android:pathData="M728,631L1184.4,1084.75H271.605L728,631Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="201"
                android:startY="631"
                android:endX="1255"
                android:endY="631"
                android:type="linear">
{GRADIENT_STOPS_XML}
            </gradient>
        </aapt:attr>
    </path>
    <path
        android:pathData="M1198.07,627.899L1198.01,1075.15L750.823,627.963L1198.07,627.899Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="939.888"
                android:startY="369.718"
                android:endX="1456.25"
                android:endY="886.081"
                android:type="linear">
{GRADIENT_STOPS_XML}
            </gradient>
        </aapt:attr>
    </path>
    <path
        android:pathData="M728.251,605L271.856,151.25H1184.65L728.251,605Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="1255.25"
                android:startY="605"
                android:endX="201.251"
                android:endY="605"
                android:type="linear">
{GRADIENT_STOPS_XML}
            </gradient>
        </aapt:attr>
    </path>
    <path
        android:pathData="M258.181,608.101L258.245,160.853L705.429,608.037L258.181,608.101Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="516.363"
                android:startY="866.282"
                android:endX="0"
                android:endY="349.919"
                android:type="linear">
{GRADIENT_STOPS_XML}
            </gradient>
        </aapt:attr>
    </path>
</vector>
"""
    (compose_dir / "zora_icon.xml").write_text(zora_icon_xml)

    # Compose Theme Tokens
    zora_colors_kt = f"""package com.jesusdmedinac.zora.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ZoraColors {{
    val Purple = Color(0xFF894CFF)
    val Violet = Color(0xFFA833FD)
    val Magenta = Color(0xFFCD00EB)
    val Fuchsia = Color(0xFFE200C0)
    val Pink = Color(0xFFEC168A)

    val DarkBackground = Color(0xFF000000)
    val LightBackground = Color(0xFFFFFFFF)

    val BrandGradient = Brush.linearGradient(
        colors = listOf(Purple, Violet, Magenta, Fuchsia, Pink)
    )
}}
"""
    (compose_dir / "ZoraColors.kt").write_text(zora_colors_kt)


def apply_assets_to_templates():
    print("Applying generated assets across all project templates...")
    android_gen = OUTPUT_DIR / "android"
    ios_gen = OUTPUT_DIR / "ios" / "AppIcon.appiconset"
    compose_gen = OUTPUT_DIR / "compose"

    # Targets for Android res
    android_targets = [
        PROJECT_ROOT / "templates/fullstack/app/androidApp/src/main/res",
        PROJECT_ROOT / "templates/shared-ui/androidApp/src/main/res",
        PROJECT_ROOT / "templates/native-ui/androidApp/src/main/res",
        PROJECT_ROOT / "templates/toolchain-shared-ui/androidApp/res",
        PROJECT_ROOT / "templates/toolchain-native-ui/androidApp/res",
    ]

    for target in android_targets:
        if not target.exists():
            continue
        print(f"  -> Updating Android res in {target.relative_to(PROJECT_ROOT)}")
        # Copy mipmap folders
        for folder in ["mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi", "mipmap-anydpi-v26"]:
            src = android_gen / folder
            dst = target / folder
            dst.mkdir(parents=True, exist_ok=True)
            for f in src.glob("*"):
                shutil.copy(f, dst / f.name)

        # Copy drawables
        dst_drawable = target / "drawable"
        dst_drawable.mkdir(parents=True, exist_ok=True)
        shutil.copy(android_gen / "drawable/ic_launcher_background.xml", dst_drawable / "ic_launcher_background.xml")

        dst_drawable_v24 = target / "drawable-v24"
        dst_drawable_v24.mkdir(parents=True, exist_ok=True)
        shutil.copy(android_gen / "drawable-v24/ic_launcher_foreground.xml", dst_drawable_v24 / "ic_launcher_foreground.xml")

    # Targets for iOS xcassets
    ios_targets = [
        PROJECT_ROOT / "templates/fullstack/app/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset",
        PROJECT_ROOT / "templates/shared-ui/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset",
        PROJECT_ROOT / "templates/compose-multiplatform/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset",
    ]

    for target in ios_targets:
        if not target.parent.exists():
            continue
        print(f"  -> Updating iOS AppIcon in {target.relative_to(PROJECT_ROOT)}")
        target.mkdir(parents=True, exist_ok=True)
        shutil.copy(ios_gen / "app-icon-1024.png", target / "app-icon-1024.png")
        shutil.copy(ios_gen / "Contents.json", target / "Contents.json")

    # Targets for Compose Resources
    compose_drawable_targets = [
        PROJECT_ROOT / "templates/fullstack/app/shared/src/commonMain/composeResources/drawable",
        PROJECT_ROOT / "templates/shared-ui/shared/src/commonMain/composeResources/drawable",
    ]

    for target in compose_drawable_targets:
        target.mkdir(parents=True, exist_ok=True)
        print(f"  -> Copying zora_icon.xml to {target.relative_to(PROJECT_ROOT)}")
        shutil.copy(compose_gen / "zora_icon.xml", target / "zora_icon.xml")


def main():
    apply_flag = "--apply-templates" in sys.argv
    master_square, master_round = create_master_pngs()
    generate_android_assets(master_square, master_round)
    generate_ios_assets(master_square)
    generate_web_and_desktop_assets(master_square)
    generate_compose_assets()
    print("All multi-platform assets successfully generated in:", OUTPUT_DIR)

    if apply_flag:
        apply_assets_to_templates()
        print("Templates updated with Zora branding!")


if __name__ == "__main__":
    main()
