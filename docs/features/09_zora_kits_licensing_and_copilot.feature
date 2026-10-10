Feature: Zora KMP Kits Hub, Commercial Licensing, and AI Copilot
  As a Kotlin Multiplatform developer or enterprise engineering team
  I want to discover, evaluate, and scaffold production-grade Zora KMP Starter Kits directly from KMP-CLI
  So that I can accelerate project setup by 80+ hours, enforce non-transferable commercial licensing, download verified remote templates, and protect intellectual property via buyer watermarking

  Background:
    Given a clean working environment with KMP-CLI installed

  Scenario: Discover and inspect available Zora KMP Starter Kits in the CLI
    When the developer executes "kmp kit list"
    Then the terminal should display the Zora Kits catalog table
    And the catalog should enumerate "zora-community" with tier "COMMUNITY", price "$0", and status "UNLOCKED"
    And the catalog should enumerate "zora-basic" with tier "BASIC", price "$129-$149", and license requirement
    And the catalog should enumerate "zora-premium" with tier "PREMIUM", price "$299-$349", and license requirement
    And each entry should display key architectural features and local licensing status

  Scenario: Scaffold a free open-source project using Zora Community Kit without license
    When the developer executes "kmp kit create zora-community --name MyFreeApp --package com.example.free"
    Then the project directory "MyFreeApp" should be created locally without requiring a license key
    And the project should contain Kotlin Toolchain declarative configuration "project.yaml" and "app/module.yaml"
    And the project should configure Compose Multiplatform, Koin, Navigation Compose KMP, and Ktor client

  Scenario: Prompt developer with concierge acquisition screen when selecting paid kit without license
    Given no active commercial license is configured in "~/.kmp/license.json"
    When the developer executes "kmp kit create zora-basic --name MyPaidApp"
    Then the CLI should intercept the request and prevent unauthorized generation
    And the terminal should display the Zora KMP tier comparison matrix
    And it should display the time savings (80+ hours) and feature capabilities
    And it should provide actionable purchase links to "https://jesusdmedinac.com" and WhatsApp Concierge
    And when executed with "--json", it should emit a structured "PAYMENT_REQUIRED" error payload with code 402

  Scenario: Activate a commercial license key with hardware fingerprinting and two-seat limit
    Given a valid non-transferable license key for tier "PREMIUM"
    When the developer executes "kmp license activate <LICENSE_KEY>"
    Then the CLI should extract the unique host machine fingerprint (IOPlatformUUID or machine-id)
    And it should verify the license token and active device count against the licensing authority
    And if the active devices count is within the 2-seat limit, it should record the activation
    And save the cryptographically signed license payload locally in "~/.kmp/license.json"
    And the CLI should display the activated tier, licensee name, machine fingerprint, and active seats (e.g., "1 of 2")

  Scenario: Reject license activation when seat limit is exceeded
    Given a valid license key that already has 2 active device registrations
    When the developer executes "kmp license activate <LICENSE_KEY>" on a 3rd distinct machine
    Then the CLI should reject activation with "SEAT_LIMIT_EXCEEDED"
    And display instructions to deactivate a previously registered device

  Scenario: Scaffold Zora Basic kit with valid license, remote delivery, and buyer watermarking
    Given a valid active license for tier "BASIC" in "~/.kmp/license.json"
    When the developer executes "kmp kit create zora-basic --name MyBasicApp --package com.example.basic"
    Then the CLI should authenticate the license against the secure storage endpoint (Cloudflare R2)
    And download the encrypted "zora-basic" template package on demand
    And unpack the project configuring Room KMP offline persistence, Zora Material 3 UI Kit, and Onboarding/Settings screens
    And inject a non-removable cryptographic buyer watermark with licensee name, email, and license ID into the generated project metadata

  Scenario: Scaffold Zora Premium Enterprise kit with valid license, remote delivery, and buyer watermarking
    Given a valid active license for tier "PREMIUM" in "~/.kmp/license.json"
    When the developer executes "kmp kit create zora-premium --name MyEnterpriseApp --package com.example.enterprise"
    Then the CLI should authenticate the license against the secure storage endpoint (Cloudflare R2)
    And download the encrypted "zora-premium" template package on demand
    And unpack the project configuring Native Auth (Google/Apple), RevenueCat billing, SKIE Swift bridge, and GitHub Actions CI/CD
    And inject a non-removable cryptographic buyer watermark with licensee name, email, and license ID into the generated project metadata

  Scenario: Inspect local license status and seat allocations
    Given an active commercial license in "~/.kmp/license.json"
    When the developer executes "kmp license status"
    Then the terminal should display the active tier, licensee name, email, machine fingerprint, and seat allocation

  Scenario: Deactivate license on current machine to release seat allocation
    Given an active commercial license in "~/.kmp/license.json"
    When the developer executes "kmp license logout"
    Then the CLI should notify the licensing authority to unregister the current machine fingerprint
    And remove "~/.kmp/license.json" from the local filesystem
    And report that the seat has been successfully released

