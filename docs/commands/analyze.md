# `kmp analyze`

Audit project dependencies, version catalogs (`gradle/libs.versions.toml`), and library compatibility.

---

## Usage

```bash
kmp analyze dependencies [path] [options]
```

## Options

- `path`: Project path to analyze. Defaults to `.`.
- `--json`: Output dependency matrix as structured JSON.

---

## Dependency Replacement Matrix

When analyzing legacy or incomplete KMP projects, `kmp analyze` cross-references declared packages with official, certified multiplatform replacements from the Kotlin Foundation ecosystem:

| Category | Legacy Coordinate | Recommended KMP Equivalent | Target Platforms |
| :--- | :--- | :--- | :--- |
| **Networking** | Retrofit / OkHttp / Axios | Ktor Client (`io.ktor:ktor-client-core`) | All targets |
| **Persistence** | Room (Android-only) / SQLite | Room KMP (`androidx.room:room-runtime`) / SQLDelight | Android, iOS, Desktop |
| **Preferences** | SharedPreferences / AsyncStorage | Multiplatform-Settings (`com.russhwolf:multiplatform-settings`) | All targets |
| **Images** | Glide / Picasso | Coil 3 (`io.coil-kt.coil3:coil-compose`) | Android, iOS, Desktop, Web |
| **Server-Driven UI** | Bespoke JSON parsers | `json-to-compose` (`io.github.jesusdmedinac:json-to-compose`) | Android, iOS, Desktop, Web |
