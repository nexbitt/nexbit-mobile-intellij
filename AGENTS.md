# Nexbit Mobile — AGENTS.md

## Architecture
- No Jetpack Navigation, ViewModel, LiveData, Compose, Data Binding, or DI.
- All screens extend `AppCompatActivity`; navigation is manual `startActivity(Intent)`.
- API calls are made directly in Activities via `enqueue()` with anonymous callbacks.
- State held in Activity fields — rotation resets it.
- Session in `SharedPreferences("app")` — userId, token, rolId, userName, etc. No encryption.
- `LoginActivity` is the launcher. On start, it checks for an existing token and auto-navigates to `MainActivity` via `GET /usuarios/me`.

## API layer
- Base URL: `http://10.0.2.2:3000/api/` (emulator localhost)
- Retrofit + OkHttp + `AuthInterceptor` auto-injects `Authorization: Bearer <token>` from prefs.
- Do NOT pass `@Header("Authorization")` on API methods — the interceptor handles it.
- On 401, interceptor clears prefs and redirects to `LoginActivity`.
- Cleartext HTTP allowed (`usesCleartextTraffic="true"` + `network_security_config.xml`).
- 30s connect/read timeouts configured in `ApiClient`.

## Build
```
gradlew.bat assembleDebug
gradlew.bat test
```
- Gradle 8.13 wrapper, AGP 8.12.0, Kotlin 2.0.21.
- JDK 21 required (`jvmTarget = "21"`, `JavaVersion.VERSION_21`).
- `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`.
- Copy `local.properties.example` → `local.properties` and set `sdk.dir`.

## Tests
- Stubs only — no real unit or instrumented tests.

## Key conventions
- DB fields are `snake_case` (`id_usuario`, `precio_venta`). Kotlin `data class` fields must match exactly.
- Prices are `Double` (maps to DB `DECIMAL`).
- Role constants: `ROL_ADMIN = 1`, `ROL_CLIENTE = 2`, `ROL_REPARTIDOR = 4` (in `MainActivity.kt` companion).
- Drawer menu groups shown/hidden by role in `MainActivity.kt`.
- Theme: Material3 DayNight, `primary = #111111`, light background.

## Orphaned resources (safe to delete)
- `activity_login2.xml`, `layout.xml`, `clientes.xml` — not referenced by any Activity.

## Active source tree
- Only `app/` — `nexbit-mobile-intellij/` was merged and removed.
