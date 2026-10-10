Feature: Zora Brand Identity and Multiplatform Asset Generation
  As a developer using KMP CLI and Zora KMP Starter Kits
  I want all project templates, launchers, and documentation to feature official Zora branding
  So that generated applications and the CLI ecosystem have cohesive, production-grade visual assets

  Scenario: Generate standardized multi-platform assets from Zora master icons
    Given the master Zora vector assets in "icons/zora-icon.svg" and "icons/zora-icon-dark.svg"
    And the official 5-stop Zora gradient colors from "#894CFF" to "#EC168A"
    When the multiplatform asset generation script is executed
    Then standard Android mipmap PNGs and adaptive vector drawables are generated
    And standard iOS "AppIcon.appiconset" with 1024x1024 master and Contents.json is generated
    And Web favicon and desktop icon assets are generated

  Scenario: Update Android launcher and adaptive icons across all project templates
    Given the generated Android Zora launcher assets
    When the templates in "templates/" are updated
    Then "ic_launcher_foreground.xml" contains the centered Zora vector logo
    And "ic_launcher_background.xml" matches the official Zora background
    And all mipmap density directories (mdpi to xxxhdpi) contain Zora launcher PNGs

  Scenario: Update iOS AppIcon asset catalog across all project templates
    Given the generated iOS Zora AppIcon assets
    When the templates in "templates/" are updated
    Then each iOS app bundle contains "Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
    And "Contents.json" declares the single universal 1024x1024 app icon asset

  Scenario: Integrate Zora vector drawable into Compose Multiplatform template UI
    Given the Zora vector drawable "zora_icon.xml" with official gradient stops
    When added to "composeResources/drawable/" in the templates
    Then the Compose UI "App.kt" displays the Zora brand logo
    And the brand color tokens are accessible in the Compose theme

  Scenario: Embed official Zora branding into documentation
    Given the Zora vector icons and 5-stop gradient palette
    When updating "README.md" and "docs/RFC-003-zora-kits-licensing-and-copilot.md"
    Then the project header displays the centered Zora icon supporting dark and light themes
    And the documentation details the official Zora brand color palette
