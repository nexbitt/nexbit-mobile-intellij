# Nexbit Mobile

Aplicación Android nativa para el sistema de comercio electrónico Nexbit / RematesPaisa. Desarrollada en Kotlin con conexión al backend Express mediante Retrofit y Socket.IO.

## Stack

| Categoría | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin 2.0.21 |
| Android SDK | API 35 (compile), API 24 (mínimo) |
| Gradle | 8.13 (Gradle Wrapper) |
| AGP | 8.12.0 |
| JDK | 21 |
| HTTP | Retrofit 2.11 + OkHttp 4.12 |
| Tiempo real | Socket.IO Client 2.1.0 |
| Imágenes | Glide 4.16 |
| Navegación | Navigation Fragment/UI KTX 2.7.7 |
| Seguridad | Security Crypto 1.1.0-alpha06 |
| UI | Material Design 3, ConstraintLayout, RecyclerView, ViewPager2 |

## Requisitos Previos

- **Android Studio** (Koala o superior)
- **JDK 21** (configurado en Android Studio: `Settings > Build > Gradle > Gradle JDK`)
- **Android SDK** 35 (instalar desde SDK Manager)
- **Git**
- El **backend corriendo** en `http://10.0.2.2:3000` (el emulador Android usa `10.0.2.2` para acceder al `localhost` del host)

## Instalación y Ejecución

1. **Clona el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd nexbit-mobile-intellij
   ```

2. **Configura el SDK local:**
   El proyecto necesita saber dónde está tu Android SDK. Crea o edita `local.properties` en la raíz del proyecto:
   ```properties
   sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
   ```
   > En macOS/Linux sería algo como: `sdk.dir=/home/usuario/Android/Sdk`

3. **Abre el proyecto en Android Studio:**
   - `File > Open` y selecciona la carpeta del proyecto.
   - Espera a que Gradle sincronice y descargue las dependencias (puede tomar varios minutos la primera vez).

4. **Compila el APK de depuración:**
   ```bash
   # Windows:
   gradlew.bat assembleDebug

   # macOS/Linux:
   ./gradlew assembleDebug
   ```
   > El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`

5. **Ejecuta en un emulador o dispositivo físico:**
   - Asegúrate de que el backend esté corriendo en `http://localhost:3000`.
   - Conecta un dispositivo USB con depuración USB habilitada, o inicia un emulador desde AVD Manager.
   - En Android Studio, selecciona la configuración `app` y presiona `Run` (o `Shift+F10`).

## Scripts Disponibles

| Comando | Descripción |
|---------|------------|
| `gradlew.bat assembleDebug` | Compila APK de depuración |
| `gradlew.bat assembleRelease` | Compila APK de release (firmado) |
| `gradlew.bat test` | Ejecuta tests unitarios |
| `gradlew.bat clean` | Limpia la compilación |

## Credenciales de Prueba

| Rol | Correo | Contraseña |
|-----|--------|-----------|
| Administrador | admin@remate.com | admin123 |
| Cliente | cliente@ejemplo.com | admin123 |
| Repartidor | repartidor1@ejemplo.com | admin123 |

## Arquitectura

```
nexbit-mobile-intellij/
├── app/
│   ├── build.gradle.kts          # Dependencias y config del módulo
│   └── src/main/
│       ├── AndroidManifest.xml   # Actividades y permisos
│       ├── java/com/example/nexbitmobile/
│       │   ├── MainActivity.kt
│       │   ├── NexbitApplication.kt
│       │   ├── api/              # Retrofit, Socket.IO, interceptors
│       │   ├── model/            # Data classes (Producto, Pedido, Usuario, etc.)
│       │   ├── ui/               # Activities, Fragments, Adapters
│       │   └── util/             # SecurePrefs, LanguageHelper
│       └── res/                  # Layouts, drawables, strings, temas
├── build.gradle.kts              # Config global de Gradle
├── settings.gradle.kts           # Módulos y repositorios
├── gradle.properties             # Propiedades de compilación
├── local.properties              # Ruta del SDK local (no versionado)
└── gradle/wrapper/
    └── gradle-wrapper.properties # Versión de Gradle (8.13)
```

## API — Conexión al Backend

La app se conecta al backend Express en `http://10.0.2.2:3000` (dentro del emulador Android). Si usas un dispositivo físico, cambia la IP en `apiClient.kt` y `SocketManager.kt` por la IP local de tu computadora.

Endpoints principales usados:
- `POST /api/v1/usuarios/login` — autenticación
- `GET /api/v1/productos/publico` — catálogo de productos
- `GET /api/v1/categorias` — lista de categorías
- `GET /api/v1/pedidos/mis-pedidos` — pedidos del usuario
- `POST /api/v1/pedidos` — crear pedido
- `GET /api/v1/carrito` — carrito de compras

## Solución de Problemas

| Error | Causa | Solución |
|-------|-------|----------|
| `SDK location not found` | Falta `local.properties` | Crear archivo con `sdk.dir` apuntando al SDK |
| `Unsupported class file major version` | JDK incorrecto | Configurar JDK 21 en Android Studio |
| `Connection refused` en API | Backend no iniciado | Ejecutar `npm run dev` en el backend |
| `EADDRINUSE :::3000` | Puerto ocupado | Matar proceso anterior en puerto 3000 |
| `Cannot access field connected: Boolean` | Socket.IO API change | Usar `socket?.connected()` (método) en vez de `socket?.connected` (campo privado) |
| `Cannot resolve symbol R` | Build corrompido | `Build > Clean Project` luego `Rebuild` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | App ya instalada con otra firma | Desinstalar la app del dispositivo/emulador |

## Repositorios Relacionados

- [Backend](https://github.com/equiposeis84/backend)
- [Frontend](https://github.com/equiposeis84/frontend)
