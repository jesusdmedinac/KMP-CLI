---
name: kotlin-data-room-kmp
description: Multiplatform SQLite persistence with Room KMP (androidx.room 2.7.0+), Coroutines Flows, and schema migrations across Android and iOS.
version: 1.0.0
author: jesusdmedinac
tags:
  - room
  - database
  - sqlite
  - persistence
  - kmp
  - flow
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - room kmp
  - androidx room
  - multiplatform sqlite
  - room database
  - local persistence
compatibility:
  kotlin: ">=2.0.0"
  room: ">=2.7.0"
---

# Multiplatform SQLite Persistence with Room KMP

As of `androidx.room` 2.7.0+, Google officially supports Room on Kotlin Multiplatform (Android, iOS, Desktop, and Wasm) using SQLite and KSP.

---

## 1. Multiplatform Dependencies

Configure dependencies in `module.yaml` (Kotlin Toolchain) or `build.gradle.kts`:

### `module.yaml`
```yaml
dependencies:
  - androidx.room:room-runtime:2.7.0-alpha13
  - androidx.sqlite:sqlite-bundled:2.5.0-alpha13
```

### `libs.versions.toml`
```toml
[versions]
room = "2.7.0-alpha13"
sqlite = "2.5.0-alpha13"

[libraries]
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
```

---

## 2. Entities & DAOs in `commonMain`

Define data entities and reactive DAOs in shared Kotlin code:

```kotlin
package com.example.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val vin: String,
    val make: String,
    val model: String,
    val year: Int,
    val mileageKm: Double,
    val healthScore: Int,
)

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE vin = :vin")
    suspend fun getVehicleByVin(vin: String): VehicleEntity?

    @Query("SELECT * FROM vehicles ORDER BY year DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE vin = :vin")
    suspend fun deleteVehicle(vin: String)
}
```

---

## 3. Database Declaration

In `commonMain`, declare the `@Database` abstract class and constructor:

```kotlin
package com.example.app.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [VehicleEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
}

// The Room compiler generates the implementation
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

---

## 4. Platform Database Builders (`expect`/`actual`)

Instantiate `RoomDatabase.Builder` using platform-specific file locations:

### Common Declaration (`commonMain`)
```kotlin
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createDatabase(): AppDatabase {
    return getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()
}
```

### Android (`androidMain`)
```kotlin
import android.content.Context
import androidx.room.Room

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = applicationContext // Injected via Koin or Application
    val dbFile = appContext.getDatabasePath("app.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
```

### iOS (`iosMain`)
```kotlin
import androidx.room.Room
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbFilePath = "${documentDirectory?.path}/app.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = { AppDatabaseConstructor.initialize() }
    )
}
```

---

## 5. Testing with In-Memory Database

In unit tests (`commonTest`), create an in-memory SQLite database without touching the disk:

```kotlin
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun createInMemoryDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}
```
