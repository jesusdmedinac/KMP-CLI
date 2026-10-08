Feature: Zora KMP Kits Hub, Commercial Licensing, and AI Copilot
  As a Kotlin Multiplatform developer or enterprise engineering team
  I want to discover, evaluate, and scaffold production-grade Zora KMP Starter Kits directly from KMP-CLI
  So that I can accelerate project setup by 80+ hours, seamlessly acquire commercial licenses, customize enterprise templates, and leverage an intelligent AI copilot

  Background:
    Given a clean working environment with KMP-CLI installed

  Scenario: Discover and inspect available Zora KMP Starter Kits in the CLI
    When the developer executes "kmp kit list"
    Then the terminal should display the Zora Kits catalog
    And the catalog should enumerate "zora-community" with tier "COMMUNITY" and price "$0"
    And the catalog should enumerate "zora-basic" with tier "BASIC"
    And the catalog should enumerate "zora-premium" with tier "PREMIUM"
    And each entry should display key features and licensing status

  Scenario: Scaffold a free open-source project using Zora Community Kit
    When the developer executes "kmp kit create zora-community --name MyFreeApp --package com.example.free"
    Then the project directory "MyFreeApp" should be created without requiring a license key
    And the project should contain Kotlin Toolchain declarative configuration "project.yaml" and "app/module.yaml"
    And the project should configure Compose Multiplatform, Koin, and Ktor client

  Scenario: Prompt developer with concierge acquisition screen when selecting paid kit without license
    Given no active commercial license is configured in "~/.kmp/license.json"
    When the developer executes "kmp kit create zora-premium --name MyEnterpriseApp"
    Then the CLI should intercept the request and prevent unauthorized generation
    And the terminal should display the Zora KMP tier comparison matrix
    And it should display the time savings and features (Auth, RevenueCat, Push, SKIE, CI/CD)
    And it should provide actionable purchase links to "https://jesusdmedinac.com" and WhatsApp Concierge
    And when executed with "--json", it should emit a structured "PAYMENT_REQUIRED" error payload

  Scenario: Activate a valid commercial license key
    Given a valid non-transferable license key for "zora-premium"
    When the developer executes "kmp license activate <LICENSE_KEY>"
    Then the license should be cryptographically verified
    And saved locally in "~/.kmp/license.json"
    And the CLI should display the activated tier, licensee name, and unlocked capabilities

  Scenario: Scaffold a project using Zora Basic or Premium kit with valid license
    Given a valid active license for "zora-premium" in "~/.kmp/license.json"
    When the developer executes "kmp kit create zora-premium --name MyPaidApp --package com.example.paid"
    Then the project directory "MyPaidApp" should be created successfully
    And the project should contain native authentication skeletons for Google and Apple
    And the project should contain RevenueCat multiplatform billing configuration
    And the project should include Touchlab SKIE bridging configuration
    And GitHub Actions CI/CD workflows for Android AAB and iOS IPA should be generated

  Scenario: Customize a Zora kit configuration for licensed developers
    Given a valid active license in "~/.kmp/license.json"
    When the developer executes "kmp kit customize --kit zora-premium --name CustomEnterpriseKit"
    Then the developer should be able to toggle architectural modules and dependencies
    And the customized template recipe should be saved for reusable organization scaffolding

  Scenario: Execute KMP Copilot architectural guidance for licensed developers
    Given a valid active license in "~/.kmp/license.json"
    When the developer executes "kmp copilot check"
    Then KMP Copilot should analyze the project architecture
    And provide proactive multiplatform recommendations, coroutine dispatcher safety checks, and Swift interop guidance
