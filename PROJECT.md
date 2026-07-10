# PROJECT.md — nexbit-mobile-intellij (Android)
> Generado por el agente de planificación técnica.
> Creado: 2026-07-02 | Motivo: Documentación inicial del proyecto

## Origen
- **Tipo:** Heredado
- **Repositorio original:** `https://github.com/nexbitt/nexbit-mobile-intellij.git`
- **Archivos usados para inferencia:** `build.gradle.kts` (raíz y app), `settings.gradle.kts`, `gradle.properties`, `AndroidManifest.xml`, estructura de `app/src/main/java/` y `app/src/main/res/`, `README.md`

## Stack
- **Lenguaje:** Kotlin 2.0.21
- **Android Gradle Plugin:** 8.12.0
- **Gradle Wrapper:** 8.13
- **JDK:** OpenJDK 21
- **Android SDK:** compileSdk 35, minSdk 24, targetSdk 35
- **UI:** XML layouts (NO Jetpack Compose)
- **Tema:** Material 3 (DayNight NoActionBar)
- **Peticiones HTTP:** Retrofit 2.11 + OkHttp 4.12 + Gson
- **Imágenes:** Glide 4.16
- **Tiempo real:** Socket.IO Client 2.1.0
- **Almacenamiento seguro:** AndroidX Security Crypto (EncryptedSharedPreferences)
- **Navegación:** Navigation Component (fragments) + Intents explícitos entre Activities
- **Carousel:** ViewPager2 1.1.0
- **Pull-to-refresh:** SwipeRefreshLayout 1.1.0

## Arquitectura
- **Tipo:** App Android nativa con múltiples Activities (no single-activity)
- **Patrón:** Activities + Fragments + adapters para listas
- **Sin patrón MVVM o Clean Architecture** — lógica en Activities/Fragments con ViewModel mínimo
- **Package structure:** `com.example.nexbitmobile`
  - `api/` → Retrofit service interface, interceptors, socket manager
  - `model/` → Data classes (request/response DTOs)
  - `ui/` → Activities + Fragments
  - `ui/admin/` → Pantallas de administración
  - `ui/components/` → Componentes reutilizables (carousel, upload handler, notification toast)
  - `util/` → Utilidades (SecurePrefs, LanguageHelper)
  - Raíz → `MainActivity.kt`, `NexbitApplication.kt`

## Activities (26 declaradas en AndroidManifest.xml)
| Grupo | Activities |
|-------|-----------|
| **Auth** | SplashActivity, LoginActivity, RegistroActivity, RecoveryActivity |
| **Cliente** | ClientMainActivity, MainOrbixActivity, CatalogoActivity, CarritoActivity, MisPedidosActivity, PerfilActivity, ConfirmarPedidoActivity, ConfirmacionPagoActivity, PagoTransferenciaActivity |
| **Admin** | MainActivity (Drawer), UsuariosAdminActivity, ProductosAdminActivity, CategoriasAdminActivity, PedidosAdminActivity, ProveedorActivity, ClientesActivity, ReportesActivity, TrashOrdersActivity, RevisionPagosActivity, CheckoutManualActivity |
| **Repartidor** | EntregasActivity |
| **General** | ChatActivity, HelpActivity, ContactoActivity, TicketActivity, RepartidorDetailActivity, OrderDetailActivity, OrdersActivity, ProductDetailActivity |

## Recursos (res/)
| Directorio | Contenido |
|-----------|-----------|
| `layout/` | 120 archivos XML (activities, fragments, dialogs, items, bottom sheets) |
| `drawable/` | 159 archivos XML (backgrounds, badges, botones, iconos vectoriales) |
| `values/` | colors.xml (105 colores), strings.xml, themes.xml |
| `values-night/` | Tema oscuro |
| `menu/` | Menús de navegación (drawer, bottom nav) |
| `navigation/` | Gráfo de navegación (fragments) |
| `anim/` | Animaciones XML |
| `color/` | Selectores de color |
| `xml/` | Configuración de red y backup |

## Temas y estilos
- **Tema base:** Material 3 DayNight NoActionBar
- **Paleta:** "Orbix Studio" — colores sincronizados con frontend React
- **Drawables personalizados:** 159 archivos que replican el diseño "Antigravity" del frontend
- **Tipografía:** sans-serif-medium / Inter (descargable)

## Convenciones detectadas
- Nombres de archivos XML: `snake_case` con prefijo descriptivo (`bg_`, `ic_`, `item_`, `dialog_`, `fragment_`)
- Nombres de Activities: PascalCase con sufijo `Activity` (ej. `LoginActivity.kt`)
- Conexión a API: URL base `http://10.0.2.2:3000/api/` (emulador → host local)
- Autenticación: Token JWT almacenado en SharedPreferences encriptadas
- Navegación: Intents + Navigation Component para fragments
- Sin corrutinas organizadas — uso de Callbacks de Retrofit

## Restricciones
- minSdk 24 (Android 7.0) — no compatible con versiones anteriores
- Requiere JDK 21 para compilar
- El emulador Pixel 9 Pro presenta problemas de gráficos (pantalla negra)
- La app usa `usesCleartextTraffic=true` para desarrollo (HTTP sin TLS)
- CORS del backend debe permitir localhost:8081
- Sin Jetpack Compose — toda la UI es XML tradicional

## Estado actual
- **Funcional:** Login/Registro, catálogo de productos, carrito, vista de pedidos (admin y cliente), perfil (admin), gestión de usuarios, roles, productos, categorías, proveedores, entregas de repartidor, chat, tickets, reportes
- **En desarrollo:** Sincronización visual con frontend React (NEX-021 → NEX-033)
- **Problema conocido:** MainOrbixActivity (nuevo diseño con carrusel) puede tener datos vacíos si el backend no tiene productos seed

## Decisiones clave
- Kotlin sobre Java por modernidad y sintaxis concisa
- XML layouts sobre Jetpack Compose porque el proyecto comenzó antes de que Compose madurara
- Retrofit sobre Volley por mejor soporte de tipos y conversión Gson
- Múltiples Activities sobre Single-Activity + Navigation por simplicidad inicial
- Material 3 DayNight para soporte de tema oscuro nativo
