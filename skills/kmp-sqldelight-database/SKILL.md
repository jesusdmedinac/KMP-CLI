---
name: kmp-sqldelight-database
description: Multiplatform SQL persistence, reactive Flow queries, and target-specific driver setup using SQLDelight 2+.
version: 1.0.0
author: jesusdmedinac
tags:
  - database
  - sql
  - sqlite
  - persistence
  - sqldelight
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - sqldelight
  - sqlite multiplatform
  - database migration
compatibility:
  kotlin: ">=2.0.0"
  gradle: ">=8.5"
---

# SQLDelight Multiplatform Database Persistence

This guide covers building typesafe, persistent SQLite storage across Android, iOS, Desktop, and Web using SQLDelight 2+.

## 1. Driver Factory Pattern

The database schema and queries are defined in `.sq` files inside `commonMain/sqldelight/`. Because SQLite drivers are platform-native, declare a driver factory in `commonMain`:

```kotlin
// commonMain
expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(driver)
}
```

## 2. Platform Driver Configurations

- **Android**: `AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")`
- **iOS / macOS**: `NativeSqliteDriver(AppDatabase.Schema, "app.db")`
- **Desktop (JVM)**: `JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)` or `JdbcSqliteDriver("jdbc:sqlite:app.db").also { AppDatabase.Schema.create(it) }`
- **Wasm**: `WebWorkerDriver` / WebAssembly SQLite driver.

## 3. Reactive Queries with Kotlinx Coroutines

Use the Coroutines extension to observe database queries as reactive `Flow`:

```kotlin
val itemsFlow: Flow<List<ItemEntity>> = database.itemQueries
    .selectAll()
    .asFlow()
    .mapToList(Dispatchers.Default)
```

## 4. Schema Migrations

Never modify existing `.sq` files in production without creating a numbered migration file:
- Location: `src/commonMain/sqldelight/com/example/1.sqm`
- Ensure tests verify migrations using `AppDatabase.Schema.migrate(driver, 1, 2)`.
