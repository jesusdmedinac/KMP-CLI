Feature: Cross-Platform Modernization & Migration Analysis
  As a mobile engineer, tech lead, or AI agent evaluating legacy cross-platform codebases
  I want KMP CLI to inspect React Native, Flutter, and hybrid repositories
  So that I can audit native bridge dependencies, evaluate migration feasibility, and execute phased migrations to Dual Native, Native UI + Shared KMP Core, or 100% Compose Multiplatform

  Scenario: Audit React Native codebase and generate dependency replacement matrix
    Given an existing React Native repository with "package.json", iOS CocoaPods, and Android Gradle modules
    When the developer or AI agent runs "kmp migrate audit"
    Then the engine must detect the React Native framework version and architecture (Old Architecture bridge vs New Architecture TurboModules)
    And the engine must identify all third-party npm packages requiring native bridges (e.g. storage, networking, navigation, native device APIs)
    And the engine must map each legacy dependency to its recommended KMP or native equivalent (e.g. AsyncStorage -> Multiplatform-Settings, axios -> Ktor, Redux/Zustand -> MVI/StateFlow)

  Scenario: Audit Flutter codebase and generate dependency replacement matrix
    Given an existing Flutter repository with "pubspec.yaml" and platform folders
    When the developer or AI agent runs "kmp migrate audit"
    Then the engine must detect the Flutter SDK constraints and platform targets
    And the engine must catalog third-party pub plugins and platform channels (MethodChannel / Pigeon)
    And the engine must map each Flutter dependency to its recommended KMP or native equivalent (e.g. sqflite -> SQLDelight, dio/http -> Ktor, bloc/riverpod -> Flow/Store)

  Scenario: Smart redirect from kmp analyze when detecting legacy cross-platform codebases
    Given an existing React Native or Flutter codebase without Kotlin build descriptors
    When the developer executes "kmp analyze" on that directory
    Then the CLI must recognize the legacy framework signature
    And the CLI must display an informative recommendation advising the use of "kmp migrate audit"
    And when the "--json" flag is passed, the CLI must return a structured payload indicating legacy detection and suggested next command

  Scenario: Recommend architectural modernization pathway
    Given a parsed cross-platform dependency and complexity analysis
    When the feasibility engine evaluates project characteristics, platform dependencies, and team constraints
    Then it must output a recommendation across the three canonical pathways:
      | Pathway                     | Criteria                                                                    | Recommendation Rationale                                                    |
      | Dual Native                 | Heavy OS-specific UI, complex AR/camera, platform design divergence         | 100% SwiftUI + 100% Jetpack Compose for uncompromised native experience     |
      | Native UI + Shared KMP Core | Rich existing design systems, platform-specific UX, shared domain & data    | Native UI with unified Kotlin Multiplatform business logic and offline state|
      | 100% Compose Multiplatform  | High UI consistency requirement, velocity focus, unified multiplatform team | Single declarative UI + shared multiplatform codebase across all targets    |

  Scenario: Generate phased Strangler Fig migration roadmap
    Given a modernization audit report for a legacy application
    When the migration roadmap generator is invoked via "kmp migrate plan"
    Then it must provide a multi-phase incremental plan:
      | Phase   | Name                    | Objectives                                                                    |
      | Phase 1 | Foundation & Core Bridge| Setup shared KMP module, export CocoaPods/SPM & Gradle AAR, unify network/auth|
      | Phase 2 | Domain & Data Strangler | Migrate offline database, repositories, and state stores to KMP               |
      | Phase 3 | UI & Screen Incremental | Replace legacy screens incrementally with native UI or Compose Multiplatform  |
      | Phase 4 | Legacy Shell Deletion   | Remove JS/Dart runtime engine, delete bridge modules, finalize native binaries|

  Scenario: Provide canonical migration skills in KMP Skills Hub
    Given a developer using the KMP Skills Hub
    When they search for cross-platform migration guides using "kmp skills find migration"
    Then the registry must include canonical skills:
      | Skill ID                         | Description                                                                 |
      | kmp-migrate-from-react-native    | Strangler migration patterns, bridge removal, JS state to Kotlin StateFlow  |
      | kmp-migrate-from-flutter         | Flutter Widget & BLoC migration to Compose Multiplatform & Coroutines       |
      | kmp-shared-core-bridge           | Architecture and packaging of a Shared KMP Core for existing native shells  |

  Scenario: Scaffold Shared KMP Core bridge template
    Given a developer starting an incremental migration
    When they run "kmp create --template kmp-shared-core-bridge"
    Then the CLI must generate a lightweight KMP library configured for ingestion by existing native/cross-platform shells
    And the template must configure SPM/CocoaPods export for iOS and Maven Local/AAR for Android
    And the template must include sample cross-platform coroutine dispatchers and Swift-friendly interfaces

  Scenario: Output migration audit in dual formats for humans and AI agents
    Given an audit execution of a cross-platform codebase
    When the "kmp migrate audit" command executes
    Then with standard invocation it must render a rich Mordant terminal UI with risk indicators and compatibility score
    And with the "--json" flag it must produce strict, machine-readable JSON for AI agent orchestration
