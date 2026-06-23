# PROJECT.md
> Generado por el agente de planificación técnica.
> Creado: 2026-06-23 | Actualizado: 2026-06-23 | Motivo: Descubrimiento inicial

## Origen
- **Tipo:** Heredado (app Android nativa, en desarrollo activo)
- **Archivos usados para inferencia:** `build.gradle.kts` (raíz), `app/build.gradle.kts`, `settings.gradle.kts`, `app/src/main/java/com/example/nexbitmobile/MainActivity.kt`, `apiService.kt`, `apiClient.kt`, `NexbitApplication.kt`, `res/values/colors.xml`, `res/values/themes.xml`, estructura de layouts y drawables

## Stack
- **Lenguaje principal:** Kotlin 2.0.21
- **Framework:** Android SDK nativo (sin Jetpack Compose)
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 24 (Android 7.0)
- **Compile SDK:** 35
- **Java:** Java 21 (sourceCompatibility/targetCompatibility)
- **Build System:** Gradle con Kotlin DSL (Gradle 8.12)
- **UI:** XML Layouts (actividades con `setContentView`), sin Jetpack Compose
- **Iconos:** Drawables vectoriales XML (35+ iconos personalizados)
- **Fondos/BG:** Drawables XML para inputs, botones, badges, estados (40+ archivos `bg_*.xml`)

### Dependencias principales
| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| `androidx.core:core-ktx` | 1.12.0 | Extensiones Kotlin para Android |
| `androidx.appcompat:appcompat` | 1.7.0 | Compatibilidad hacia atrás |
| `com.google.android.material:material` | 1.12.0 | Material 3 Design |
| `androidx.activity:activity-ktx` | 1.9.3 | Activity KTX |
| `androidx.constraintlayout:constraintlayout` | 2.2.0 | Layouts flexibles |
| `com.squareup.retrofit2:retrofit` | 2.11.0 | Cliente HTTP |
| `com.squareup.retrofit2:converter-gson` | 2.11.0 | Serialización JSON |
| `com.squareup.okhttp3:okhttp` | 4.12.0 | HTTP Client |
| `com.squareup.okhttp3:logging-interceptor` | 4.12.0 | Logging HTTP |
| `com.github.bumptech.glide:glide` | 4.16.0 | Carga de imágenes |
| `androidx.cardview:cardview` | 1.0.0 | Cards UI |
| `androidx.recyclerview:recyclerview` | 1.3.2 | Listas eficientes |
| `androidx.viewpager2:viewpager2` | 1.1.0 | Carrusel/páginas |

## Mapa de responsabilidades
- `app/src/main/java/com/example/nexbitmobile/` → Código fuente Kotlin
  - `MainActivity.kt` → Actividad principal con Navigation Drawer. Menú dinámico por rol (Admin/Cliente/Repartidor).
  - `NexbitApplication.kt` → Application class. Inicializa Glide con caché de imágenes.
  - `api/` → Capa de red: `apiClient.kt` (Retrofit + OkHttp), `apiService.kt` (interfaz con 40+ endpoints), `AuthInterceptor.kt`.
  - `model/` → Modelos de datos (14 clases: Usuario, Producto, Pedido, CarritoItem, Categoria, Rol, Proveedor, LoginRequest/Response, etc.).
  - `ui/` → Activities de UI (35+ archivos):
    - **Auth:** LoginActivity, RegistroActivity, RecoveryActivity, SplashActivity
    - **Admin:** UsuariosAdminActivity, RolesAdminActivity, CategoriasAdminActivity, ProductosAdminActivity, PedidosAdminActivity, ProveedorActivity, ClientesActivity
    - **Cliente/Store:** CatalogoActivity, CarritoActivity, ProductDetailActivity, MisPedidosActivity, OrderDetailActivity, PerfilActivity, ClientProfileActivity
    - **Repartidor:** EntregasActivity, PruebasActivity, PerfilPruebaActivity
    - **Adapters:** 12 adapters para RecyclerViews
- `app/src/main/res/layout/` → ~85 layouts XML (activities, fragments, dialogs, items, carousel pages, expanded panels)
- `app/src/main/res/drawable/` → ~80 recursos drawable XML (iconos vectoriales + backgrounds con estado)
- `app/src/main/res/values/` → `colors.xml` (96 colores), `themes.xml` (Material 3 personalizado)

## Arquitectura de la App

### Navegación
- **DrawerLayout** con NavigationView en MainActivity.
- Menú dinámico según rol (`rolId` guardado en SharedPreferences):
  - **Admin:** gestión completa (usuarios, roles, categorías, productos, pedidos, proveedores, clientes, entregas)
  - **Cliente:** catálogo, carrito, mis pedidos, perfil
  - **Repartidor:** entregas, perfil
- Cada sección abre una nueva Activity (no hay fragments como navegación principal).

### Red
- **Base URL:** `http://10.0.2.2:3000/api/` (emulador Android → host local)
- **Retrofit** con **OkHttp** + **AuthInterceptor** (inyecta token JWT en headers)
- **Gson** para serialización
- Timeouts: 30s connect/read

### Imágenes
- **Glide** con caché completa (disk + memory), placeholder `ic_placeholder` y error handler.

## Sistema de Colores (Paleta "Orbix Studio")
| Token | Color | Uso |
|-------|-------|-----|
| `primary` | `#1A1A1A` | Botones principales, texto, acentos |
| `primary_dark` | `#000000` | Status bar |
| `primary_light` | `#333333` | Hover/active states |
| `bg_page` | `#F8F9FA` | Fondo de pantalla |
| `bg_surface` / `card_bg` | `#FFFFFF` | Cards, superficies |
| `text_main` | `#1A1A1A` | Texto principal |
| `text_secondary` | `#6C757D` | Texto secundario |
| `success` | `#10B981` | Estados exitosos |
| `warning` | `#F59E0B` | Advertencias |
| `info` | `#3B82F6` | Información |
| `error_text` | `#EF4444` | Errores |
| `input_bg` | `#F1F3F5` | Fondos de input |
| `divider` | `#E9ECEF` | Líneas divisorias |

### Temas
- **Base:** `Theme.Material3.DayNight.NoActionBar`
- **ColorPrimary:** `@color/primary` (`#1A1A1A`)
- **ColorSecondary:** `@color/success` (`#10B981`)
- **StatusBar:** `primary_dark` (negro)
- **NavigationBar:** `bg_surface` (blanco, light icons)

## Estado actual
- **Implementado:** Login/registro, CRUDs completos (usuarios, roles, categorías, productos, proveedores), catálogo, carrito, pedidos (cliente y admin), perfil, recuperación de contraseña (OTP), gestión de repartidores.
- **UI:** Más de 40 actividades con sus layouts XML. Sistema completo de drawables para estados (focused, error, disabled, active).
- **Navegación:** Drawer con menú contextual por rol.

## Decisiones clave
- **Sin Jetpack Compose:** UI construida completamente con XML layouts y Activities tradicionales.
- **Retrofit + OkHttp:** Comunicación con el backend Express en `localhost:3000`.
- **SharedPreferences:** Almacenamiento de sesión (userId, rolId, token).
- **DrawerLayout:** Navegación principal con menú lateral dinámico.
- **Glide:** Manejo de imágenes con caché y placeholders.
- **AuthInterceptor:** Inyección automática de token JWT en todas las peticiones.

## Ambigüedades pendientes
- No se encontraron archivos de Jetpack Compose (todo es XML tradicional).
- No se detectaron pruebas unitarias ni de instrumentación.
- `AuthInterceptor.kt` no fue leído — probablemente lee el token de SharedPreferences y lo inyecta.
- No se detectó manejo de Socket.IO en mobile (el backend lo soporta).
- El flujo de navegación entre Activities puede beneficiarse de un singleton de sesión.
