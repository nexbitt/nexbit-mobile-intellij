# 🎨 estilo.md — Guía Maestra de Sincronización Frontend → Mobile

> **Propósito:** Replicar el frontend React en Android nativo (Kotlin/XML) pantalla por pantalla, con TODOS los botones, acciones y estilos idénticos.
> **Rama de trabajo:** `Nicolas` (crear si no existe).
> **Última actualización:** 2026-06-23 — Se agregó el mega-prompt unificado + gap analysis completo.

---

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  📖 LO QUE DEBES LEER PRIMERO (Quick Reference)                            ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  Si eres nuevo o retomas después de un descanso, lee esto en orden:         ║
║                                                                              ║
║  1. README.md — Secciones 1 a 10 (paleta, tipografía, componentes, etc.)    ║
║  2. Nexbit-Frontend/PROJECT.md — Stack y estructura del frontend            ║
║  3. nexbit-mobile-intellij/PROJECT.md — Stack y estructura del mobile       ║
║  4. Este archivo (estilo.md) — La guía completa que estás leyendo           ║
║  5. .agents/PROJECT.md — Contexto técnico del agente (opcional)             ║
║                                                                              ║
║  DESPUÉS DE LEER:                                                           ║
║  - Si es tu PRIMERA VEZ: usa los Prompts Individuales (sección A)           ║
║  - Si ya avanzaste: ve directo al BACKLOG y elige tu próximo issue          ║
║  - Si quieres TODO de una vez: usa el MEGA PROMPT (sección C)              ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  🔄 FLUJO DE TRABAJO DIARIO                                                 ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  INICIO DEL DÍA:                                                            ║
║    git checkout main                                                        ║
║    git pull origin main                                                     ║
║    git checkout Nicolas                                                     ║
║    git merge main                                                           ║
║                                                                              ║
║  TRABAJO:                                                                   ║
║    Opción A — Un prompt individual (elige 1 issue del backlog)              ║
║    Opción B — El mega-prompt (hace TODO lo pendiente de una vez)           ║
║                                                                              ║
║  FIN DEL DÍA:                                                               ║
║    git add [archivos específicos]                                           ║
║    git commit -m "tipo(alcance): descripción"                               ║
║    git push origin Nicolas                                                  ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  🗺️  MAPA DE NAVEGACIÓN DEL ARCHIVO                                          ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  SECCIÓN A — Prompts Individuales (Histórico, 20 issues NEX-001→020)       ║
║    → Para trabajar UN issue a la vez, uno por uno.                          ║
║    → Ya implementados (no repetir).                                         ║
║                                                                              ║
║  SECCIÓN B — Backlog Completo para Jira (NEX-001→033)                      ║
║    → Lista maestra de todos los issues, los hechos y los pendientes.        ║
║                                                                              ║
║  ═══════════════════════════════════════════════════════════════════         ║
║  ⚡ PUNTO DE INFLEXIÓN — DE AQUÍ EN ADELANTE ES TODO EN UN SOLO PROMPT    ║
║  ═══════════════════════════════════════════════════════════════════         ║
║                                                                              ║
║  SECCIÓN C — Mega Prompt Unificado                                          ║
║    → Un SOLO prompt que hace TODO el trabajo pendiente.                     ║
║    → Copia TODO desde "=== INICIO MEGA PROMPT ===" hasta "=== FIN ==="      ║
║    → Pégalo en la IA y espera a que termine todo.                          ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

# ═══════════════════════════════════════════════════════════════════════════
# 📋 SECCIÓN B — BACKLOG COMPLETO PARA JIRA (NEX-001 → NEX-033)
# ═══════════════════════════════════════════════════════════════════════════

> **33 issues totales · 20 COMPLETADOS · 13 PENDIENTES**
> Usa esto para crear los issues en Jira y trackear el progreso.

## Issues COMPLETADOS (NO repetir)

| Issue | Título | Estado | Sprint |
|-------|--------|--------|--------|
| NEX-001 | Sincronizar paleta de colores (colors.xml) | ✅ COMPLETADO | S1 |
| NEX-002 | Crear 17 drawables base (botones, inputs, cards, 10 badges) | ✅ COMPLETADO | S1 |
| NEX-003 | Rediseñar LoginActivity | ✅ COMPLETADO | S1 |
| NEX-004 | Rediseñar RegistroActivity | ✅ COMPLETADO | S1 |
| NEX-005 | Rediseñar RecoveryActivity (flujo OTP) | ✅ COMPLETADO | S1 |
| NEX-006 | Rediseñar Navigation Drawer por rol | ✅ COMPLETADO | S1 |
| NEX-007 | Rediseñar CatalogoActivity (grid 2 cols) | ✅ COMPLETADO | S1 |
| NEX-008 | Rediseñar ProductDetailActivity | ✅ COMPLETADO | S1 |
| NEX-009 | Rediseñar CarritoActivity | ✅ COMPLETADO | S1 |
| NEX-010 | Rediseñar MisPedidos + OrderDetail | ✅ COMPLETADO | S1 |
| NEX-011 | Rediseñar PerfilActivity | ✅ COMPLETADO | S1 |
| NEX-012 | Rediseñar CRUD UsuariosAdmin | ✅ COMPLETADO | S1 |
| NEX-013 | Rediseñar CRUD CategoriasAdmin | ✅ COMPLETADO | S1 |
| NEX-014 | Rediseñar CRUD ProductosAdmin | ✅ COMPLETADO | S1 |
| NEX-015 | Rediseñar gestión PedidosAdmin | ✅ COMPLETADO | S1 |
| NEX-016 | Rediseñar CRUD ProveedorActivity | ✅ COMPLETADO | S1 |
| NEX-017 | Agregar vista ClientesActivity | ✅ COMPLETADO | S1 |
| NEX-018 | Rediseñar EntregasActivity (repartidor) | ✅ COMPLETADO | S1 |
| NEX-019 | Rediseñar PedidoActivo (timeline) | ✅ COMPLETADO | S1 |
| NEX-020 | Rediseñar HistorialRepartidor | ✅ COMPLETADO | S1 |

## Issues PENDIENTES (por implementar)

| Issue | Título | Prioridad | Depende de | Archivos estimados |
|-------|--------|-----------|------------|-------------------|
| NEX-021 | Crear RolesAdminActivity — CRUD completo de roles | 🔴 ALTA | NEX-001, NEX-002 | 4 |
| NEX-022 | Crear RepartidoresAdminActivity — Gestión de repartidores | 🔴 ALTA | NEX-001, NEX-002 | 4 |
| NEX-023 | Crear ConfirmacionPedidoActivity — Subir comprobante | 🔴 ALTA | NEX-001, NEX-002, NEX-009 | 3 |
| NEX-024 | Agregar botones faltantes a CarritoActivity (vaciar, IVA, subtotal) | 🔴 ALTA | NEX-009 | 2 |
| NEX-025 | Agregar filtros, chat y timeline a PedidosAdminActivity | 🟡 MEDIA | NEX-015, NEX-031 | 4 |
| NEX-026 | Agregar Subir Comprobante y Reintentar a MisPedidosActivity | 🔴 ALTA | NEX-010 | 3 |
| NEX-027 | Rediseñar PerfilActivity con tabs (4 pestañas) y toggles | 🟡 MEDIA | NEX-011 | 4 |
| NEX-028 | Rediseñar EntregasActivity con timeline, chat y ActiveBanner | 🔴 ALTA | NEX-018, NEX-031 | 4 |
| NEX-029 | Agregar "Ver ficha técnica" a CatalogoActivity | 🟡 MEDIA | NEX-007 | 2 |
| NEX-030 | Crear PerfilRepartidorActivity | 🟡 MEDIA | NEX-001, NEX-002 | 2 |
| NEX-031 | Crear componentes UI compartidos (ChatModal, AdminToast, CartToast, AdminCheckoutModal, CustomDialog) | 🟡 MEDIA | — | 5 |
| NEX-032 | Agregar Landing Page (Inicio.jsx) para usuarios no autenticados | 🟢 BAJA | NEX-001, NEX-002 | 2 |
| NEX-033 | Agregar páginas estáticas Ayuda y Contacto | 🟢 BAJA | NEX-001, NEX-002 | 2 |

---

# ═══════════════════════════════════════════════════════════════════════════
# ⚡ PUNTO DE INFLEXIÓN
# ═══════════════════════════════════════════════════════════════════════════
#
# DE AQUÍ EN ADELANTE, TODO ES UN SOLO PROMPT.
# Ya no hay prompts individuales. Copia el MEGA PROMPT completo y pégalo.
#
# ═══════════════════════════════════════════════════════════════════════════

# 🚀 SECCIÓN C — MEGA PROMPT UNIFICADO
#
# INSTRUCCIONES:
# 1. Copia TODO desde "=== INICIO MEGA PROMPT ===" hasta "=== FIN ==="
# 2. Pégalo en la IA (usar Executor-agent o similar)
# 3. La IA leerá los archivos del frontend y mobile, y hará TODOS los cambios
# 4. Cuando termine, ejecuta los comandos git que aparecen al final
#
# IMPORTANTE: Este mega-prompt asume que los issues NEX-001 a NEX-020 ya están
# implementados. Si falta alguno, hazlo primero con los prompts individuales
# de la sección A (archivo histórico abajo).

=== INICIO MEGA PROMPT ===

# ═══════════════════════════════════════════════════════════════════════════════
# MEGA PROMPT — Sincronización Completa Frontend → Mobile
# ═══════════════════════════════════════════════════════════════════════════════
# Objetivo: Implementar TODOS los issues pendientes (NEX-021 → NEX-033)
# para que la app Android tenga EXACTAMENTE los mismos botones, acciones y
# estilos que el frontend React.
# ═══════════════════════════════════════════════════════════════════════════════

INSTRUCCIÓN PARA LA IA (Executor-agent):

Eres un experto en Android Kotlin. Vas a sincronizar COMPLETAMENTE la app
Android con el frontend web. Trabajas en la rama `Nicolas`.

## CONTEXTO GLOBAL

### Proyectos
- Frontend: C:\Users\cuent\Documents\GitHub\Nexbit-Frontend
- Mobile: C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij
- Backend: C:\Users\cuent\Documents\GitHub\Nexbit-Backend
- README.md raíz: C:\Users\cuent\Documents\GitHub\README.md

### Stack Mobile
- Kotlin 2.0.21, SDK 35, Min SDK 24
- Sin Jetpack Compose (XML layouts + Activities)
- Retrofit + OkHttp para APIs, Glide para imágenes
- DrawerLayout + NavigationView para navegación principal
- Drawables XML para backgrounds, iconos, badges

### Reglas de oro
1. NO modifiques apiService.kt, modelos de datos, AuthInterceptor.kt, NexbitApplication.kt
2. NO modifiques AndroidManifest.xml a menos que sea estrictamente necesario
3. NO borres código existente — solo agrega o modifica lo necesario
4. Usa siempre los drawables y colores existentes (bg_*.xml, colors.xml)
5. Los nombres de variables de color NO se cambian
6. Cada cambio debe ser atómico y compilable

---

## TAREA 1 — NEX-021: Crear RolesAdminActivity (CRUD Roles)

Lee (solo contexto, NO modifiques):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Roles.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\ui\RolFormModal.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\Rol.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (endpoint getRoles, updateRol)

Modifica/Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/RolesAdminActivity.kt
- CREAR: app/src/main/res/layout/activity_roles_admin.xml
- CREAR: app/src/main/res/layout/item_rol_admin.xml
- CREAR: app/src/main/res/layout/dialog_rol.xml

Diseño exacto:
a) Header: título "Roles" + botón "+ Nuevo" (bg_btn_primary.xml pequeño)
b) Barra de búsqueda: EditText con bg_input.xml + icono lupa
c) Lista (RecyclerView item_rol_admin.xml):
   - Nombre del rol (fontWeight 600, textSize 16sp)
   - Descripción (textSize 13sp, color #6B7280)
   - Badge de estado: bg #F3F4F6 text #111827 si activo, bg #FEE2E2 text #DC2626 si inactivo
   - Acciones: botón "Editar" (ic_edit.xml, 36dp circular)
d) Dialog crear/editar (dialog_rol.xml):
   - Título: "Nuevo Rol" / "Editar Rol"
   - Campos: nombre (autouppercase), descripción (textarea), activo (CheckBox)
   - Botones: "Cancelar" (bg_btn_secondary.xml) + "Guardar" (bg_btn_primary.xml)
   - NO incluir botón Eliminar (frontend tampoco lo tiene para roles)

Registrar en AndroidManifest.xml y en MainActivity.kt (nav_roles → RolesAdminActivity).

---

## TAREA 2 — NEX-022: Crear RepartidoresAdminActivity (Gestión Repartidores)

Lee (solo contexto):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Repartidores.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\RepartidorModels.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (endpoints de repartidores)

Modifica/Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/RepartidoresAdminActivity.kt
- CREAR: app/src/main/res/layout/activity_repartidores_admin.xml
- CREAR: app/src/main/res/layout/item_repartidor_admin.xml

Diseño exacto:
a) Vista principal:
   - Header: "Repartidores"
   - Barra de búsqueda por nombre/email
   - Filtro por estado: 3 chips (Todos, Activos, Inactivos)
     - Chip inactivo: bg #F8F9FA, text #6B7280, radius 999dp
     - Chip activo: bg #111111, text white
b) Lista (item_repartidor_admin.xml):
   - Nombre (fontWeight 600)
   - Email (textSize 13sp, color #6B7280)
   - Estado: Toggle (bg negro=activo, bg #F3F4F6=inactivo, radius 999dp, min-width 90dp)
   - Pedidos activos: badge con número
   - Botón "Ver Detalle" (ic_search.xml, 36dp circular)
c) Vista detalle (al hacer clic en "Ver Detalle"):
   - Card con info del repartidor (nombre, email, teléfono)
   - Estadísticas: Entregados (verde), Cancelados (rojo), Pendientes (ámbar)
   - Sección "Pedidos asignados":
     - Lista de pedidos con ID, cliente, dirección, total, estado (badge)
     - Botón "Desasignar" (ic_trash.xml, color #EF4444)
   - Selector de pedidos para asignar: Spinner con pedidos sin asignar + botón "Asignar"

Registrar en AndroidManifest.xml y MainActivity.kt.

---

## TAREA 3 — NEX-023: Crear ConfirmacionPedidoActivity

Lee (solo contexto):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\ConfirmacionPedido.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt

Modifica/Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/ConfirmacionPedidoActivity.kt
- CREAR: app/src/main/res/layout/activity_confirmacion_pedido.xml

Diseño:
a) Mensaje de éxito: icono check verde grande + "¡Pedido creado con éxito!"
b) Datos bancarios (card estilo bg_card.xml):
   - Banco: [dato del backend]
   - Tipo de cuenta: [dato del backend]
   - Número de cuenta: [dato del backend, copiable]
   - Titular: [dato del backend]
c) Zona de subir comprobante:
   - ImageView con bg_dashed_border.xml, hint "Toca para subir comprobante"
   - Al tocar: abre selector de galería (Intent ACTION_PICK)
   - Preview de imagen seleccionada
d) Botón "Enviar Comprobante": bg_btn_primary.xml
   - Sube imagen a Cloudinary (o endpoint del backend)
   - PUT /pedidos/{id}/comprobante
e) Botón "Volver a mis pedidos": bg_btn_secondary.xml → navega a MisPedidosActivity

---

## TAREA 4 — NEX-024: Agregar botones faltantes a CarritoActivity

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Carrito.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\CarritoActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_carrito.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/CarritoActivity.kt
- MODIFICAR: app/src/main/res/layout/activity_carrito.xml

Agrega:
a) Botón "Vaciar todo el carrito" (ic_trash.xml + texto, color #EF4444)
   - Al tocar: diálogo de confirmación "¿Estás seguro de vaciar el carrito?"
b) Desglose de precios debajo de la lista:
   - "Subtotal: $X.XX" (textSize 15sp)
   - "IVA (19%): $X.XX" (textSize 15sp, color #6B7280)
   - Línea divisoria (View height 1dp, bg #E5E7EB)
   - "Total: $X.XX" (textSize 20sp, fontWeight 800, color #111827)
c) Para usuarios no autenticados: al tocar "Realizar Pedido",
   mostrar bottom sheet con "Inicia sesión para continuar" + botones
   "Iniciar Sesión" y "Registrarse"

---

## TAREA 5 — NEX-025: Agregar filtros, chat y timeline a PedidosAdminActivity

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Pedidos.jsx (sección admin)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\PedidosAdminActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_pedidos_admin.xml
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\item_pedido_admin.xml
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\dialog_pedido.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/PedidosAdminActivity.kt
- MODIFICAR: app/src/main/res/layout/activity_pedidos_admin.xml

Agrega:
a) Filtros por fecha:
   - 3 chips horizontales: "Hoy", "Última semana", "Este mes"
   - Estilo: inactivo bg #F8F9FA text #6B7280, activo bg #111111 text white
b) En el diálogo de detalle de pedido, agregar:
   - Timeline vertical de cambios de estado:
     · Cada paso: dot circular (12dp) + fecha + estado + quién cambió
     · Completado: dot verde (#22C55E)
     · Actual: dot ámbar (#F59E0B) con glow
     · Pendiente: dot gris (#E5E7EB)
   - Botón "Ver Comprobante" (si existe): abre imagen en dialog o visor
   - Botón "Aprobar Pago" (bg #16A34A, text white) → PUT pedidos/{id}/estado
   - Botón "Rechazar Pago" (bg #EF4444, text white) → dialog con motivo
   - Botón "Chatear" (abre ChatModal - ver TAREA 11)

---

## TAREA 6 — NEX-026: Agregar Subir Comprobante y Reintentar a MisPedidosActivity

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Pedidos.jsx (sección cliente)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\MisPedidosActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_mis_pedidos.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/MisPedidosActivity.kt
- MODIFICAR: app/src/main/res/layout/activity_mis_pedidos.xml

Agrega:
a) En el detalle del pedido (dialog):
   - Botón "Subir Comprobante" (solo si estado es PENDIENTE):
     · Abre selector de galería
     · Preview de imagen
     · Botón "Enviar" → PUT pedidos/{id}/comprobante
   - Botón "Reintentar" (solo si estado es RECHAZADO):
     · Abre selector de galería para subir nuevo comprobante
   - Botón "Descargar Ticket" → genera HTML y lanza PrintManager
     (usa el mismo patrón que PedidosAdminActivity ya tiene)
b) En la lista, mostrar imagen del primer producto del pedido
   (48dp x 48dp, radius 8dp, usando Glide)

---

## TAREA 7 — NEX-027: Rediseñar PerfilActivity con tabs

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Perfil.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\ui\ToggleRow.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\PerfilActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_perfil.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/PerfilActivity.kt
- MODIFICAR: app/src/main/res/layout/activity_perfil.xml

Rediseño completo:
a) 4 tabs horizontales (TabLayout o botones):
   - "Datos Personales", "Papelera", "Seguridad", "Configuración"
   - Tab activo: bg #111111, text white, radius 999dp
   - Tab inactivo: bg #F8F9FA, text #6B7280

b) Tab "Datos Personales":
   - Card de usuario con avatar (iniciales circulares)
   - Campos editables: nombre, email, teléfono, dirección
   - Botón "Editar" → pide contraseña actual (dialog)
   - Botón "Guardar Cambios" (bg_btn_primary.xml)
   - Botón "Cerrar Sesión" (bg_logout_btn.xml)

c) Tab "Papelera":
   - Lista de pedidos cancelados/borrados
   - Cada item: #ID, fecha, total, badge "CANCELADO"
   - Botón "Restaurar" → PUT pedidos/{id}/restaurar

d) Tab "Seguridad":
   - Sección "Cambiar contraseña": inputs actual + nueva + confirmar (todos con toggle ojo)
   - Botón "Actualizar Contraseña"
   - Toggle "Autenticación 2 Pasos" (demo)
   - Toggle "Notificaciones"

e) Tab "Configuración":
   - Toggle "Modo Oscuro" (demo)
   - Botón "Exportar datos" (demo)
   - Botón "Configurar preferencias" (demo)

---

## TAREA 8 — NEX-028: Rediseñar EntregasActivity con timeline y ActiveBanner

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\repartidor\InicioRepartidor.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\repartidor\PedidoActivo.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\repartidor\PedidosDisponibles.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\features\ActiveBanner.jsx
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\index.css (secciones ActiveBanner, timeline, btn-tomar)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\EntregasActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_entregas.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/EntregasActivity.kt
- MODIFICAR: app/src/main/res/layout/activity_entregas.xml

Agrega/Modifica:
a) **ActiveBanner** (visible si hay pedido activo):
   - Fondo gradiente #FBBF24 → #F59E0B, radius 12dp
   - Icono de moto (ic_truck.xml)
   - "Tienes un pedido activo" fontWeight bold, color #78350F
   - Dirección de entrega + tiempo estimado
   - Animación de pulso (alertaPulse)
   - Click → abre detalle del pedido activo

b) **Stats dashboard** arriba de la lista:
   - 4 cards pequeñas: Disponibles, En reparto, Entregados, Cancelados
   - Cada card: icono + número + label
   - Estilo: bg_card.xml, padding 12dp, textAlign center

c) **Timeline de progreso** (en detalle del pedido activo):
   - 3 pasos: ASIGNADO → EN CAMINO → ENTREGADO
   - Cada paso: dot circular 26dp
     · Completado: bg #22C55E con check blanco
     · Activo: bg #F59E0B con glow
     · Pendiente: bg #E5E7EB
   - Línea conectora entre dots

d) **Botones de acción** en pedido activo:
   - "Iniciar ruta" (bg #F59E0B, text white)
   - "Marcar como entregado" (bg #16A34A, text white)
   - "Reportar problema" (bg #FEF3C7, text #92400E)
   - "Chatear" (abre ChatModal)
   - "Cancelar entrega" (bg #FEF2F2, text #DC2626)

---

## TAREA 9 — NEX-029: Agregar "Ver ficha técnica" a CatalogoActivity

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Productos.jsx (sección storefront)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\CatalogoActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\item_producto.xml

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/CatalogoActivity.kt
- MODIFICAR: app/src/main/res/layout/item_producto.xml

Agrega:
a) En item_producto.xml, agregar botón "Ver ficha técnica":
   - Texto pequeño (textSize 11sp, color #6B7280, underline)
   - Al tocar: abre dialog o navega a ProductDetailActivity con pestaña "Ficha Técnica"
b) En ProductDetailActivity, agregar sección "Ficha Técnica":
   - ID del producto
   - Código interno (si existe)
   - Proveedor
   - Categoría
   - Stock mínimo
   - Fecha de creación

---

## TAREA 10 — NEX-030: Crear PerfilRepartidorActivity

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\repartidor\PerfilRepartidor.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\PerfilPruebaActivity.kt

Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/PerfilRepartidorActivity.kt
- CREAR: app/src/main/res/layout/activity_perfil_repartidor.xml

Diseño:
a) Card con foto de perfil (avatar con iniciales)
b) Campos editables: nombre, teléfono, email (solo lectura)
c) Botón "Guardar Cambios" (bg_btn_primary.xml)
d) Toggles: Modo Oscuro, Notificaciones, Autenticación 2 Pasos
e) Botón "Cerrar Sesión"
f) Toolbar con flecha de retroceso

Registrar en AndroidManifest.xml y en el NavigationView de MainActivity
(grupo de repartidor: nav_perfil_repartidor → PerfilRepartidorActivity).

---

## TAREA 11 — NEX-031: Crear componentes UI compartidos

### 11a — ChatModal

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\features\ChatModal.jsx
- C:\Users\cuent\Documentos\GitHub\nexbit-mobile-intellij\app\src\main\res\layout

Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/ChatModalDialog.kt
- CREAR: app/src/main/res/layout/dialog_chat.xml

Diseño:
- Dialog fullscreen (o bottom sheet)
- Header: "Chat con [nombre]" + botón cerrar
- Lista de mensajes (RecyclerView, scroll hacia abajo automático)
  · Mensaje propio: bg #111111, text white, alineado derecha
  · Mensaje otro: bg #F3F4F6, text #111827, alineado izquierda
- Input: EditText multiline + botón enviar (icono Send)
- Socket: enviar mensaje via POST a endpoint de chat

### 11b — AdminToast (Notificaciones toast para admin)

Crea:
- CREAR: app/src/main/res/layout/toast_admin_notification.xml
- Agregar lógica en MainOrbixActivity o MainActivity
- Diseño: card flotante abajo-derecha con icono, título, descripción, botón cerrar
- Auto-descarte a los 5 segundos
- Tipos: "Nuevo Pedido", "Nuevo Comprobante", "Info"

### 11c — CartToast

Crea:
- Agregar en CarritoActivity y CatalogoActivity
- Diseño: Snackbar personalizado con icono producto, nombre, "agregado al carrito"
- Botón "Ver Carrito" → navega a CarritoActivity

### 11d — AdminCheckoutModal

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\ui\AdminCheckoutModal.jsx

Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/AdminCheckoutDialog.kt
- CREAR: app/src/main/res/layout/dialog_admin_checkout.xml

Diseño:
- Buscador de clientes (autocomplete)
- Buscador de productos (dropdown, indicador de stock)
- Lista de productos agregados (imagen, nombre, cantidad, precio, botón eliminar)
- Campo dirección de entrega
- Campo notas (textarea)
- Resumen: subtotal, IVA 19%, total
- Botones: "Cancelar" + "Crear Pedido"

### 11e — CustomDialog unificado

Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/CustomDialog.kt
- Unificar todos los diálogos de confirmación existentes para usar este
- Tipos: SUCCESS (icono check verde), ERROR (icono X rojo),
  CONFIRM (icono alerta ámbar), VALIDATION (icono info azul)
- Botones: "Cancelar" / "Confirmar" / "Entendido" según tipo
- Animación: fadeIn + slideUp

---

## TAREA 12 — NEX-032: Crear Landing Page (MainOrbixActivity mejorada)

Lee:
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Inicio.jsx (versión pública)
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.12: Hero Section)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\MainOrbixActivity.kt

Modifica:
- MODIFICAR: app/src/main/java/com/example/nexbitmobile/ui/MainOrbixActivity.kt
- (O crear una nueva LandingActivity.kt)

Diseño (para usuarios NO autenticados o cliente):
a) Hero section:
   - Fondo oscuro (#0A0A0A)
   - Logo Nexbit grande + tagline
   - "Crea tu cuenta gratis" (bg white, text black, radius 999dp)
   - "Ya tengo cuenta" (bg transparent, border white, text white)
b) Stats: 4 cards (Productos, Pedidos, Clientes, Categorías)
c) Si está autenticado como admin: mantener el carrusel y dashboard actual

---

## TAREA 13 — NEX-033: Crear páginas estáticas Ayuda y Contacto

Crea:
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/AyudaActivity.kt
- CREAR: app/src/main/res/layout/activity_ayuda.xml
- CREAR: app/src/main/java/com/example/nexbitmobile/ui/ContactoActivity.kt
- CREAR: app/src/main/res/layout/activity_contacto.xml

Diseño:
a) AyudaActivity:
   - Toolbar con título "Centro de Ayuda" + retroceso
   - Lista de preguntas frecuentes (expandible: cada item tiene título + contenido oculto)
   - Estilo: cards con bg_card.xml, padding 16dp, marginBottom 8dp

b) ContactoActivity:
   - Toolbar con título "Contacto" + retroceso
   - Card con: dirección, teléfono, email, horario de atención
   - Iconos para cada dato

Registrar ambas en AndroidManifest.xml y en el NavigationView.

---

## TAREA FINAL — Registrar todas las Activities nuevas en la navegación

1. Agregar todas las nuevas Activities al AndroidManifest.xml
2. Agregar items de menú en nav_menu.xml (o en NavigationView programáticamente):
   - nav_roles → RolesAdminActivity (grupo admin)
   - nav_repartidores_admin → RepartidoresAdminActivity (grupo admin)
   - nav_perfil_repartidor → PerfilRepartidorActivity (grupo repartidor)
   - nav_ayuda → AyudaActivity (todos los roles)
   - nav_contacto → ContactoActivity (todos los roles)
3. NO modificar los IDs de items existentes

---

## CHECKLIST DE COMMITS

Después de que la IA termine TODO, ejecuta estos comandos en orden:

```bash
# Commit 1: Roles CRUD
git add app/src/main/java/com/example/nexbitmobile/ui/RolesAdminActivity.kt
git add app/src/main/res/layout/activity_roles_admin.xml
git add app/src/main/res/layout/item_rol_admin.xml
git add app/src/main/res/layout/dialog_rol.xml
git commit -m "feat(mobile-roles): crear CRUD completo de roles con búsqueda y diálogo modal"

# Commit 2: Repartidores Admin
git add app/src/main/java/com/example/nexbitmobile/ui/RepartidoresAdminActivity.kt
git add app/src/main/res/layout/activity_repartidores_admin.xml
git add app/src/main/res/layout/item_repartidor_admin.xml
git commit -m "feat(mobile-repartidores-admin): crear gestión de repartidores con asignación/desasignación"

# Commit 3: ConfirmacionPedido
git add app/src/main/java/com/example/nexbitmobile/ui/ConfirmacionPedidoActivity.kt
git add app/src/main/res/layout/activity_confirmacion_pedido.xml
git commit -m "feat(mobile-confirmacion): crear pantalla de confirmación con subida de comprobante"

# Commit 4: Mejoras Carrito
git add app/src/main/java/com/example/nexbitmobile/ui/CarritoActivity.kt
git add app/src/main/res/layout/activity_carrito.xml
git commit -m "feat(mobile-carrito): agregar vaciar carrito, desglose IVA y login prompt"

# Commit 5: Mejoras PedidosAdmin
git add app/src/main/java/com/example/nexbitmobile/ui/PedidosAdminActivity.kt
git add app/src/main/res/layout/activity_pedidos_admin.xml
git commit -m "feat(mobile-pedidos-admin): agregar filtros fecha, timeline y aprobar/rechazar pago"

# Commit 6: Mejoras MisPedidos
git add app/src/main/java/com/example/nexbitmobile/ui/MisPedidosActivity.kt
git add app/src/main/res/layout/activity_mis_pedidos.xml
git commit -m "feat(mobile-mispedidos): agregar subir comprobante, reintentar y descargar ticket"

# Commit 7: Perfil rediseñado
git add app/src/main/java/com/example/nexbitmobile/ui/PerfilActivity.kt
git add app/src/main/res/layout/activity_perfil.xml
git commit -m "feat(mobile-perfil): rediseñar con 4 tabs, toggles y papelera"

# Commit 8: Entregas mejorado
git add app/src/main/java/com/example/nexbitmobile/ui/EntregasActivity.kt
git add app/src/main/res/layout/activity_entregas.xml
git commit -m "feat(mobile-entregas): agregar ActiveBanner, stats, timeline y botones de acción"

# Commit 9: Ficha técnica
git add app/src/main/java/com/example/nexbitmobile/ui/CatalogoActivity.kt
git add app/src/main/java/com/example/nexbitmobile/ui/ProductDetailActivity.kt
git add app/src/main/res/layout/item_producto.xml
git commit -m "feat(mobile-catalogo): agregar botón ver ficha técnica y sección de info técnica"

# Commit 10: Perfil Repartidor
git add app/src/main/java/com/example/nexbitmobile/ui/PerfilRepartidorActivity.kt
git add app/src/main/res/layout/activity_perfil_repartidor.xml
git commit -m "feat(mobile-perfil-repartidor): crear perfil de repartidor con toggles"

# Commit 11: Componentes UI compartidos
git add app/src/main/java/com/example/nexbitmobile/ui/ChatModalDialog.kt
git add app/src/main/java/com/example/nexbitmobile/ui/CustomDialog.kt
git add app/src/main/java/com/example/nexbitmobile/ui/AdminCheckoutDialog.kt
git add app/src/main/res/layout/dialog_chat.xml
git add app/src/main/res/layout/dialog_admin_checkout.xml
git add app/src/main/res/layout/toast_admin_notification.xml
git commit -m "feat(mobile-ui): crear componentes compartidos (ChatModal, CustomDialog, AdminCheckout, toasts)"

# Commit 12: Landing, Ayuda, Contacto
git add app/src/main/java/com/example/nexbitmobile/ui/AyudaActivity.kt
git add app/src/main/java/com/example/nexbitmobile/ui/ContactoActivity.kt
git add app/src/main/res/layout/activity_ayuda.xml
git add app/src/main/res/layout/activity_contacto.xml
git commit -m "feat(mobile-pages): agregar landing, ayuda y contacto"

# Commit 13: Registro de navegación
git add app/src/main/AndroidManifest.xml
git add app/src/main/res/menu/nav_menu.xml
git add app/src/main/java/com/example/nexbitmobile/MainActivity.kt
git commit -m "feat(mobile-navigation): registrar todas las nuevas activities y items de menú"

# Push final
git push origin Nicolas
```

---

## VALIDACIÓN FINAL

Después de implementar TODO, verifica:

1. ✅ Compila sin errores (Build → Make Project)
2. ✅ Login → registro → recuperación funcionan
3. ✅ Navigation Drawer muestra opciones según rol
4. ✅ Catálogo muestra productos con ficha técnica y botón "+"
5. ✅ Carrito tiene vaciar, IVA y login prompt
6. ✅ Pedidos admin tiene filtros, timeline y chat
7. ✅ Pedidos cliente permite subir comprobante y reintentar
8. ✅ Perfil tiene 4 tabs funcionales
9. ✅ Roles CRUD funciona (crear, editar, buscar)
10. ✅ Repartidores admin permite asignar/desasignar
11. ✅ Confirmación de pedido permite subir comprobante
12. ✅ Entregas tiene ActiveBanner, timeline, stats
13. ✅ Perfil repartidor existe y funciona
14. ✅ ChatModal, CustomDialog, AdminToast se muestran correctamente
15. ✅ Ayuda y Contacto son accesibles desde el menú

=== FIN ===

---

# ══════════════════════════════════════════════════════════════════
# 🗂️ SECCIÓN A — ARCHIVO HISTÓRICO (Prompts Individuales)
# ══════════════════════════════════════════════════════════════════
#
# Los prompts individuales de NEX-001 a NEX-020 se mantienen abajo
# como referencia histórica. Ya fueron implementados.
# NO USAR para nuevo trabajo — usar el MEGA PROMPT de la sección C.
#
# ══════════════════════════════════════════════════════════════════

# ══════════════════════════════════════════════════════════════════
# BLOQUE 0 — ARCHIVOS GLOBALES (hacer UNA SOLA VEZ al inicio)
# ══════════════════════════════════════════════════════════════════

## ────────────────────────────────────────────────────────────────
## 00. Sincronizar paleta de colores (colors.xml)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Eres un experto en Android Kotlin. Vas a sincronizar los colores de la app Android con el frontend web.

PASO 1 — Lee estos archivos (solo contexto, no los modifiques):
- C:\Users\cuent\Documents\GitHub\README.md (sección 1: Paleta de colores, sección 2: Tipografía)
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\index.css (líneas 32-49, las variables :root)

PASO 2 — Modifica este archivo:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\values\colors.xml

PASO 3 — Mapeo exacto (valor web → valor Android):
  --bg-dark: #fafbfc   →  bg_page: #F8F9FA (ya existe, actualizar)
  --card-bg: #ffffff   →  bg_surface: #FFFFFF, bg_card: #FFFFFF
  --primary: #111111   →  primary: #1A1A1A (ya existe, actualizar)
  --primary-hover: #333333 → primary_light: #333333
  --text-main: #111827 →  text_main: #1A1A1A (actualizar)
  --text-muted: #6b7280 →  text_secondary: #6C757D (actualizar)
  --border: #e5e7eb    →  divider: #E9ECEF, card_border: #E5E7EB
  --input-bg: #f9fafb  →  input_bg: #F1F3F5 (actualizar)
  --danger: #e02424    →  error_text: #EF4444 (actualizar)
  --shadow-soft: rgba(0,0,0,0.08) → shadow_tint: #1A1A1A

IMPORTANTE:
- NO borres ningún color existente
- NO cambies los nombres de las variables de color
- Solo actualiza los valores hex
- Los valores del frontend están en el README.md sección 1

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/res/values/colors.xml
git commit -m "style(mobile-colors): sincronizar paleta con frontend web (--primary:#111111, --text-main:#111827, --border:#E5E7EB)"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-001):
Título: Sincronizar paleta de colores Android con frontend web
Descripción: Se actualizaron los valores hex de colors.xml para que coincidan con las variables CSS del tema "Antigravity" del frontend React. Los cambios incluyen: primary (#111111), bg_page (#FAFBFC), text_main (#111827), border (#E5E7EB), input_bg (#F9FAFB), divider (#E9ECEF), error_text (#E02424) y shadow_tint. No se modificaron nombres de variables ni se eliminaron colores existentes.
Archivos tocados: 1 (colors.xml)
Riesgo: BAJO (solo cambios de color, no afecta lógica)

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 01. Crear drawables base (botones, inputs, cards, 10 badges)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Crea los drawables XML base para la app Android basados en el frontend web.

PASO 1 — Lee estos archivos (contexto, no modifiques):
- C:\Users\cuent\Documents\GitHub\README.md (sección 4: Componentes críticos, sección 4.8: Badges de estado)
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\index.css (líneas 99-117 para botones, 167-183 para inputs, 186-227 para tabla, 228-237 para badges de rol, 253-266 para toggle, 303-336 para modal, 3647-3716 para badges FSM)

PASO 2 — Crea estos archivos en C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\drawable\:

# Botones
bg_btn_primary.xml:
  <shape> con <solid android:color="#111111">, <corners android:radius="999dp">
  Versión <selector> con estado pressed -> #333333

bg_btn_secondary.xml:
  <shape> con <stroke android:color="#D1D5DB" android:width="1dp">, <solid android:color="@android:color/transparent">, <corners android:radius="999dp">

bg_btn_danger.xml:
  <shape> con <solid android:color="#EF4444">, <corners android:radius="999dp">

# Inputs
bg_input.xml:
  <shape> con <solid android:color="#F9FAFB">, <stroke android:color="#E5E7EB" android:width="1dp">, <corners android:radius="6dp">

bg_input_focused.xml:
  <shape> con <solid android:color="#FFFFFF">, <stroke android:color="#111111" android:width="2dp">, <corners android:radius="6dp">

# Cards
bg_card.xml:
  <shape> con <solid android:color="#FFFFFF">, <stroke android:color="#E5E7EB" android:width="1dp">, <corners android:radius="16dp">

# Badges de estado (10 badges - colores exactos del frontend)
- bg_badge_pendiente.xml:  bg #FEF3C7, text #92400E
- bg_badge_confirmado.xml: bg #E0F2FE, text #0369A1
- bg_badge_revision.xml:   bg #FFEDD5, text #C2410C
- bg_badge_aprobado.xml:   bg #D1FAE5, text #065F46
- bg_badge_rechazado.xml:  bg #FEE2E2, text #DC2626
- bg_badge_asignado.xml:   bg #DBEAFE, text #1D4ED8
- bg_badge_camino.xml:     bg #EDE9FE, text #6D28D9
- bg_badge_entregado.xml:  bg #CCFBF1, text #0F766E
- bg_badge_cancelado.xml:  bg #FFE4E6, text #BE123C
- bg_badge_disponible.xml: bg #CFFAFE, text #0891B2

Cada badge: <shape> bg + <corners radius="999dp">. El texto se define en el layout.

IMPORTANTE:
- Cada drawable debe ser un archivo independiente
- Usa los colores de colors.xml (NO valores hardcodeados si ya existen como color)
- Los radios son en dp: pill=999dp, card=16dp, input=6dp

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/res/drawable/bg_btn_primary.xml app/src/main/res/drawable/bg_btn_secondary.xml app/src/main/res/drawable/bg_btn_danger.xml app/src/main/res/drawable/bg_input.xml app/src/main/res/drawable/bg_input_focused.xml app/src/main/res/drawable/bg_card.xml app/src/main/res/drawable/bg_badge_*.xml
git commit -m "feat(mobile-drawables): crear 17 drawables base (botones, inputs, cards, 10 badges de estado)"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-002):
Título: Crear drawables base del sistema de diseño
Descripción: Se crearon 17 drawables XML que replican el sistema de diseño del frontend web: 3 botones (primary pill negro, secondary outline, danger rojo), 2 inputs (normal y focus con borde negro), 1 card (blanca con borde gris y radius 16dp) y 10 badges de estado con colores semánticos exactos (PENDIENTE, CONFIRMADO, EN_REVISION, APROBADO, RECHAZADO, ASIGNADO, EN_CAMINO, ENTREGADO, CANCELADO, DISPONIBLE). Todos usan <shape> y <selector> para estados.
Archivos tocados: 17 drawables nuevos
Riesgo: BAJO (solo recursos visuales, no afecta código)

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BLOQUE 1 — AUTENTICACIÓN (3 pantallas)
# ══════════════════════════════════════════════════════════════════

## ────────────────────────────────────────────────────────────────
## 02. LoginActivity — Pantalla de inicio de sesión
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Vas a rediseñar la pantalla de Login en Android para que se vea exactamente como el frontend web.

PASO 1 — Lee estos archivos (solo contexto, NO los modifiques):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Login.jsx
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.6: Inputs, sección 4.1: Botón primario)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (solo el endpoint login)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\LoginRequest.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\LoginResponse.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\AuthInterceptor.kt

PASO 2 — Modifica estos archivos:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\LoginActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_login.xml

PASO 3 — Diseño exacto (activity_login.xml):
  a) Fondo: bg_page (#F8F9FA o #FAFBFC)
  b) Contenedor centrado: LinearLayout vertical con gravity center, padding 24dp
  c) Logo "Nexbit": TextView, font "sans-serif-medium", textSize 28sp, textStyle bold, color #111827, letterSpacing -0.04em (android:letterSpacing)
  d) Subtítulo (opcional): textSize 14sp, color #6B7280
  e) Campo email: EditText con background="@drawable/bg_input", altura 48dp (por diseño), padding 12dp, radius 6dp, hint "Correo electrónico"
  f) Campo password: EditText con bg_input.xml, hint "Contraseña", incluir ImageButton al final (ic_eye_on/ic_eye_off) para toggle visibility
  g) Botón "Iniciar sesión": background="@drawable/bg_btn_primary", textColor white, textSize 16sp, textStyle bold, height 48dp, width match_parent, marginTop 16dp
  h) Link "¿Olvidaste tu contraseña?": TextView, textColor #6B7280, gravity center, onClick -> RecoveryActivity
  i) Link "Crear cuenta": TextView, textColor #111827, textStyle bold, gravity center, onClick -> RegistroActivity

PASO 4 — NO CAMBIAR bajo ninguna circunstancia:
  ✗ apiService.kt (los endpoints)
  ✗ AuthInterceptor.kt
  ✗ LoginRequest.kt, LoginResponse.kt
  ✗ NexbitApplication.kt
  ✗ MainActivity.kt
  ✗ AndroidManifest.xml (a menos que la Activity no esté declarada)

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/LoginActivity.kt app/src/main/res/layout/activity_login.xml
git commit -m "feat(mobile-login): rediseñar pantalla de login con inputs pill, botón negro y links de recuperación/registro"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-003):
Título: Rediseñar LoginActivity con estilos del frontend
Descripción: Se rediseñó la pantalla de inicio de sesión para que coincida visualmente con el frontend React. Cambios aplicados: fondo bg_page (#FAFBFC), logo "Nexbit" centrado con fontWeight 800, campo email y contraseña con bg_input.xml (radius 6dp, borde #E5E7EB), botón "Iniciar sesión" con bg_btn_primary.xml (negro pill #111111, texto blanco), toggle de visibilidad de contraseña con ic_eye_on/ic_eye_off, link de recuperación de contraseña a RecoveryActivity y link de registro a RegistroActivity. No se modificó la lógica de autenticación ni los modelos.
Archivos tocados: 2 (LoginActivity.kt, activity_login.xml)
Riesgo: BAJO (solo cambios de UI, lógica intacta)
Dependencias: NEX-001, NEX-002 (colores y drawables)

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 03. RegistroActivity — Pantalla de registro
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar la pantalla de registro de Android para que coincida con el frontend web.

PASO 1 — Lee (solo contexto, NO modifiques):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Register.jsx
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.6: Inputs, sección 4.1: Botón primario)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (endpoint registerUsuario)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\Usuario.kt (modelo)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\LoginRequest.kt (o el request de registro)

PASO 2 — Modifica:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\RegistroActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_registro.xml

PASO 3 — Diseño exacto:
  a) Mismo estilo que Login: fondo bg_page, contenedor centrado
  b) Título "Crear cuenta": textSize 24sp, textStyle bold, color #111827
  c) Campos (todos con bg_input.xml, altura 48dp, radius 6dp):
     - Nombre completo
     - Correo electrónico
     - Teléfono (opcional)
     - Dirección (opcional)
     - Contraseña (con toggle ojo)
     - Confirmar contraseña (con toggle ojo)
  d) Botón "Registrarse": bg_btn_primary.xml, igual que login
  e) Link "¿Ya tienes cuenta? Inicia sesión": textColor #6B7280, onClick -> LoginActivity

PASO 4 — NO CAMBIAR:
  ✗ apiService.kt
  ✗ AuthInterceptor.kt
  ✗ Modelos (Usuario.kt, LoginRequest.kt, etc.)
  ✗ AndroidManifest.xml

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/RegistroActivity.kt app/src/main/res/layout/activity_registro.xml
git commit -m "feat(mobile-registro): rediseñar registro con formulario completo (nombre, email, teléfono, dirección, contraseña)"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-004):
Título: Rediseñar RegistroActivity con estilos del frontend
Descripción: Se rediseñó la pantalla de registro manteniendo la misma identidad visual que Login (fondo bg_page, inputs con bg_input.xml, botón primary negro pill). Se agregaron campos: nombre completo, correo electrónico, teléfono, dirección, contraseña y confirmación de contraseña. Toggle de visibilidad en campos de contraseña. Link de navegación a LoginActivity.
Archivos tocados: 2 (RegistroActivity.kt, activity_registro.xml)
Riesgo: BAJO
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 04. RecoveryActivity — Recuperación de contraseña (OTP)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el flujo de recuperación de contraseña con 3 pasos (email → OTP → nueva contraseña).

PASO 1 — Lee (contexto, NO modifiques):
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\ForgotPassword.jsx
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.6: Inputs)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (endpoints: recoverPassword, verifyOtp, resetPassword)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\RecoveryModels.kt

PASO 2 — Modifica:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\RecoveryActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_recovery.xml

PASO 3 — Diseño (3 pasos visibles uno a la vez):
  Paso 1 — Solicitar código:
    - Input email con bg_input.xml
    - Botón "Enviar código" con bg_btn_primary.xml
  Paso 2 — Verificar OTP:
    - 6 campos OTP individuales (uno al lado del otro), cada uno bg_recovery_otp_box.xml (ya existe)
    - Botón "Verificar" con bg_btn_primary.xml
  Paso 3 — Nueva contraseña:
    - Input nueva contraseña con toggle ojo
    - Input confirmar contraseña con toggle ojo
    - Botón "Restablecer" con bg_btn_primary.xml
  - Flecha de retroceso (ic_arrow_left.xml) arriba a la izquierda

PASO 4 — NO CAMBIAR:
  ✗ apiService.kt, RecoveryModels.kt, AuthInterceptor.kt
  ✗ Los drawables bg_recovery_* ya existentes (no tocarlos)

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/RecoveryActivity.kt app/src/main/res/layout/activity_recovery.xml
git commit -m "feat(mobile-recovery): rediseñar flujo de recuperación con 3 pasos (email, OTP, nueva contraseña)"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-005):
Título: Rediseñar RecoveryActivity con flujo OTP de 3 pasos
Descripción: Se rediseñó el flujo completo de recuperación de contraseña manteniendo el diseño visual del frontend: paso 1 (ingresar email + botón enviar), paso 2 (6 campos OTP con bg_recovery_otp_box.xml + botón verificar), paso 3 (nueva contraseña + confirmar + botón restablecer). Se agregó flecha de retroceso y navegación entre pasos. No se modificaron los endpoints ni los modelos de recovery.
Archivos tocados: 2 (RecoveryActivity.kt, activity_recovery.xml)
Riesgo: MEDIO (flujo de 3 pasos, probar cada transición)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BLOQUE 2 — NAVEGACIÓN PRINCIPAL
# ══════════════════════════════════════════════════════════════════

## ────────────────────────────────────────────────────────────────
## 05. MainActivity — Drawer de navegación con menú por rol
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el Navigation Drawer de MainActivity para que se vea exactamente como la AdminSidebar y RepartidorLayout del frontend.

PASO 1 — Lee (contexto, NO modifiques):
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.10: Sidebar Admin)
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\App.jsx (líneas 66-145: AdminLayout y RepartidorLayout)
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\components\features\AdminSidebar.jsx
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\MainActivity.kt (la actual, para no romper la lógica de navegación)

PASO 2 — Modifica:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\MainActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_main.xml
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\menu\nav_menu.xml (crear si no existe)

PASO 3 — Diseño exacto del NavigationView:
  a) Header (nav_header.xml):
     - Fondo: #111111 (negro, como la AdminSidebar)
     - Logo "Nexbit": textColor white, textSize 18sp, textStyle bold
     - Nombre del usuario: textColor rgba(255,255,255,0.7)
     - Rol: textColor rgba(255,255,255,0.4), textSize 12sp
  b) Grupos de menú (usar menu groups con android:checkableBehavior="single"):
     Admin (visible solo si rolId == 1):
       - Inicio, Usuarios, Roles, Categorías, Productos, Pedidos, Proveedores, Repartidores
     Cliente (visible solo si rolId == 2):
       - Inicio, Catálogo, Carrito, Mis Pedidos, Perfil
     Repartidor (visible solo si rolId == 4):
       - Inicio, Disponibles, En Reparto, Historial, Perfil
  c) Items:
     - Inactivos: textColor rgba(255,255,255,0.55), iconTint igual
     - Activos (checked): backgroundColor #FFFFFF, textColor #111111, iconTint #111111
  d) Footer: botón "Cerrar sesión" con icono ic_logout, textColor rgba(255,255,255,0.4)

PASO 4 — NO CAMBIAR:
  ✗ La lógica de navegación (startActivity con Intents)
  ✗ Los IDs de los items del menú (nav_inicio, nav_usuarios, etc.)
  ✗ La lógica de SharedPreferences (lectura de rolId, userId, token)
  ✗ El método onCreate, solo modificar la UI del drawer

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/MainActivity.kt app/src/main/res/layout/activity_main.xml app/src/main/res/menu/nav_menu.xml
git commit -m "feat(mobile-drawer): rediseñar NavigationView con estilo AdminSidebar (fondo negro, items con hover blanco, menú por rol)"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-006):
Título: Rediseñar Navigation Drawer con menú contextual por rol
Descripción: Se rediseñó el Navigation Drawer de MainActivity para replicar la AdminSidebar del frontend (fondo #111111, logo Nexbit en blanco, items inactivos en gris claro y items activos en blanco con fondo negro invertido). El menú se organiza en 3 grupos (Admin, Cliente, Repartidor) y se muestra según el rolId almacenado en SharedPreferences. Se agregó header con nombre de usuario y rol, y footer con botón de cerrar sesión. No se modificó la lógica de navegación ni los IDs de los items del menú.
Archivos tocados: 3 (MainActivity.kt, activity_main.xml, nav_menu.xml)
Riesgo: MEDIO (afecta navegación principal, probar que todos los items sigan funcionando)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BLOQUE 3 — CLIENTE / TIENDA (6 pantallas)
# ══════════════════════════════════════════════════════════════════

## ────────────────────────────────────────────────────────────────
## 06. CatalogoActivity — Catálogo de productos
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el catálogo de productos con cards estilo frontend.

PASO 1 — Lee (contexto, NO modifiques):
- C:\Users\cuent\Documents\GitHub\README.md (sección 4.4: Cards, sección 4.11: TopBar)
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\pages\Productos.jsx (versión "usuario")
- C:\Users\cuent\Documents\GitHub\Nexbit-Frontend\src\index.css (líneas 593-665: product-horizontal-card)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\api\apiService.kt (endpoints getProductosPublico, getProductos)
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\model\Producto.kt

PASO 2 — Modifica:
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\java\com\example\nexbitmobile\ui\CatalogoActivity.kt
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\activity_catalogo.xml
- C:\Users\cuent\Documents\GitHub\nexbit-mobile-intellij\app\src\main\res\layout\item_producto.xml

PASO 3 — Diseño exacto:
  a) TopBar:
     - Logo "Nexbit" izquierda, fontWeight bold
     - Icono de búsqueda (ic_search.xml) derecha
     - Icono de carrito (ic_cart.xml) con badge rojo (cantidad de items)
  b) Grid de productos (RecyclerView con GridLayoutManager(2)):
     Cada card (item_producto.xml):
       - Fondo: bg_card.xml (#FFFFFF, borde #E5E7EB, radius 16dp, elevation 4dp)
       - Imagen: ImageView con Glide y ic_placeholder, aspectRatio 1:1, scaleType centerCrop
       - Categoría: TextView pequeño, bg #F1F3F5, textColor #6B7280, radius 999dp, padding 4dp 10dp
       - Nombre: textSize 14sp, fontWeight bold, color #111827, lines 2
       - Precio: textSize 20sp, fontWeight 800, color #111827
       - Botón "+" circular (opcional): bg_btn_primary.xml pequeño, 36dp
  c) Estado vacío: ImageView + "No hay productos disponibles" + botón "Recargar"
  d) Al hacer clic → ProductDetailActivity

PASO 4 — NO CAMBIAR:
  ✗ apiService.kt, Producto.kt, CarritoItem.kt
  ✗ NexbitApplication.kt (configuración de Glide)
  ✗ La lógica de carga de productos

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/CatalogoActivity.kt app/src/main/res/layout/activity_catalogo.xml app/src/main/res/layout/item_producto.xml
git commit -m "feat(mobile-catalogo): rediseñar catálogo con grid 2 columnas, cards con precio y badge de categoría"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-007):
Título: Rediseñar CatalogoActivity con grid de productos
Descripción: Se rediseñó el catálogo de productos con un grid de 2 columnas (RecyclerView + GridLayoutManager). Cada card de producto incluye: imagen con Glide y placeholder, badge de categoría (bg #F1F3F5, text #6B7280, radius 999dp), nombre del producto (fontWeight bold, 2 líneas máximo), precio (fontWeight 800, tamaño 20sp, color #111827). TopBar con logo, búsqueda y carrito con badge de cantidad. Estado vacío con opción de recargar. Al hacer clic navega a ProductDetailActivity.
Archivos tocados: 3 (CatalogoActivity.kt, activity_catalogo.xml, item_producto.xml)
Riesgo: BAJO
Dependencias: NEX-001, NEX-002, NEX-006

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 07. ProductDetailActivity — Detalle de producto
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el detalle de producto.

PASO 1 — Lee:
- README.md (sección 4.6: Inputs, 4.1: Botones)
- Nexbit-Frontend\src\index.css (líneas 753-825: pricing-block, btn-add-red)
- apiService.kt (endpoint de carrito: addToCarrito)
- model\Producto.kt, model\CarritoItem.kt

PASO 2 — Modifica:
- ...\ui\ProductDetailActivity.kt
- ...\layout\activity_product_detail.xml

PASO 3 — Diseño:
  a) Imagen grande (alto 300dp, scaleType centerCrop, Glide)
  b) Nombre: textSize 22sp, fontWeight 800, color #111827
  c) Descripción: textSize 14sp, color #6B7280, lineSpacingExtra 6dp
  d) Precio: textSize 28sp, fontWeight 800, color #111827, marginTop 16dp
  e) Badge de stock: si stock > 0 → bg #D1FAE5 text #065F46 "En stock"
     si stock <= 0 → bg #FEE2E2 text #DC2626 "Agotado"
  f) Selector de cantidad: botón "-" | TextView cantidad | botón "+"
     bg_qty_control.xml (ya existe) o crear uno similar
  g) Botón "Agregar al carrito": bg_btn_primary.xml, match_parent, height 52dp
  h) Flecha de retroceso en toolbar

PASO 4 — NO CAMBIAR: apiService.kt, modelos, lógica de carrito

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/ProductDetailActivity.kt app/src/main/res/layout/activity_product_detail.xml
git commit -m "feat(mobile-product-detail): rediseñar detalle de producto con imagen, precio, stock y selector de cantidad"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-008):
Título: Rediseñar ProductDetailActivity
Descripción: Pantalla de detalle de producto con imagen grande (300dp, Glide), nombre, descripción, precio destacado (28sp, fontWeight 800), badge de disponibilidad (verde/rojo), selector de cantidad con botones +/-, y botón "Agregar al carrito" primary negro pill. Toolbar con retroceso.
Archivos tocados: 2
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 08. CarritoActivity — Carrito de compras
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el carrito de compras.

PASO 1 — Lee:
- README.md (sección 4.4: Cards)
- Nexbit-Frontend\src\pages\Carrito.jsx
- Nexbit-Frontend\src\index.css (líneas 832-1069: cart-layout)
- apiService.kt (endpoints: getCarrito, removeFromCarrito, updateCarritoItem, clearCarrito)
- model\CarritoItem.kt, model\Producto.kt

PASO 2 — Modifica:
- ...\ui\CarritoActivity.kt
- ...\layout\activity_carrito.xml
- ...\layout\item_carrito.xml

PASO 3 — Diseño:
  a) Lista de items (RecyclerView):
     - Imagen pequeña 56dp x 72dp, radius 8dp, bg #F9FAFB
     - Nombre: fontWeight bold, textSize 15sp
     - Precio unitario: textSize 14sp, fontWeight 600
     - Cantidad: botón - | cantidad | botón + (estilo bg_qty_control)
     - Subtotal: fontWeight bold, textSize 15sp
     - Botón eliminar: ic_trash.xml, color #EF4444
  b) Resumen abajo (o panel lateral):
     - Línea divisoria gruesa (#E5E7EB)
     - Subtotal, impuesto (si aplica), total
     - Total: fontWeight 800, textSize 20sp
     - Botón "Proceder al pago": bg_btn_primary.xml, match_parent, height 52dp, fontWeight 800
  c) Estado vacío: ic_cart grande + "Tu carrito está vacío" + botón "Explorar productos"

PASO 4 — NO CAMBIAR: apiService.kt, modelos de carrito, lógica de checkout

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/CarritoActivity.kt app/src/main/res/layout/activity_carrito.xml app/src/main/res/layout/item_carrito.xml
git commit -m "feat(mobile-carrito): rediseñar carrito con lista de items, selector cantidad, resumen de total y checkout"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-009):
Título: Rediseñar CarritoActivity con resumen de compra
Descripción: Se rediseñó el carrito de compras con lista de productos (imagen, nombre, precio, control de cantidad +/-, subtotal, botón eliminar), panel de resumen (subtotal, impuesto, total destacado) y botón "Proceder al pago" primary negro. Estado vacío con navegación al catálogo.
Archivos tocados: 3
Riesgo: BAJO
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 09. MisPedidosActivity + OrderDetailActivity — Pedidos del cliente
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar la lista de pedidos y el detalle de pedido.

PASO 1 — Lee:
- README.md (sección 4.8: Badges de estado)
- Nexbit-Frontend\src\pages\Pedidos.jsx (versión cliente)
- Nexbit-Frontend\src\index.css (líneas 1914-2040: orders-grid, order-card)
- Nexbit-Frontend\src\constants\orderStatuses.js (STATUS_COLORS con los 10 estados)
- apiService.kt (endpoints: getMisPedidos, getPedido, cancelarPedido)
- model\Pedido.kt

PASO 2 — Modifica:
- ...\ui\MisPedidosActivity.kt + activity_mis_pedidos.xml
- ...\ui\OrderDetailActivity.kt + activity_order_detail.xml
- ...\layout\item_pedido_cliente.xml

PASO 3 — Diseño MisPedidos:
  a) Título "Mis Pedidos" + filtro por estado (chips: Todos, Pendientes, Entregados, Cancelados)
     Cada chip: bg #F8F9FA, text #6B7280, radius 999dp
     Chip activo: bg #111111, text white
  b) Lista de pedidos (RecyclerView):
     Cada card (item_pedido_cliente.xml):
       - bg_card.xml como fondo
       - "#12345" fontWeight 800, textSize 16sp
       - Fecha: textSize 13sp, color #6B7280
       - Badge de estado: bg_badge_[estado].xml, textSize 11sp, fontWeight bold
       - Total: fontWeight 800, textSize 18sp
       - Flecha > a la derecha

PASO 4 — Diseño OrderDetail:
  a) Info del pedido: ID, fecha, dirección de entrega
  b) Badge de estado grande
  c) Lista de productos (nombre, cantidad, precio)
  d) Total general
  e) Botón "Cancelar pedido" (solo si estado es PENDIENTE o CONFIRMADO): bg #FEE2E2, text #DC2626
  f) Timeline de estados (opcional)

PASO 5 — NO CAMBIAR: apiService.kt, Pedido.kt, lógica de cancelación

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/MisPedidosActivity.kt app/src/main/java/com/example/nexbitmobile/ui/OrderDetailActivity.kt app/src/main/res/layout/activity_mis_pedidos.xml app/src/main/res/layout/activity_order_detail.xml app/src/main/res/layout/item_pedido_cliente.xml
git commit -m "feat(mobile-pedidos-cliente): rediseñar lista y detalle de pedidos con badges de estado y timeline"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-010):
Título: Rediseñar pantallas de pedidos del cliente
Descripción: Se rediseñaron MisPedidosActivity (lista con cards de pedido, filtros por estado con chips, badge de estado con colores semánticos, total destacado) y OrderDetailActivity (info del pedido, lista de productos, total, botón cancelar con confirmación, timeline de estados opcional).
Archivos tocados: 5
Riesgo: MEDIO (flujo de cancelación, probar)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 10. PerfilActivity / ClientProfileActivity — Perfil de usuario
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el perfil del cliente.

PASO 1 — Lee:
- README.md (sección 4.6: Inputs)
- Nexbit-Frontend\src\pages\Perfil.jsx
- Nexbit-Frontend\src\index.css (líneas 4912-5000: perfil-layout)
- apiService.kt (endpoint getMe, updateUsuario)
- model\Usuario.kt

PASO 2 — Modifica:
- ...\ui\PerfilActivity.kt + activity_perfil.xml
- ...\ui\ClientProfileActivity.kt (si existe)

PASO 3 — Diseño:
  a) Tabs laterales (o pestañas horizontales en mobile):
     - Información personal, Direcciones, Cambiar contraseña, Eliminar cuenta
  b) Card con campos de solo lectura o editables:
     - Cada campo: label (textSize 11sp, fontWeight bold, color #6B7280, uppercase)
     - Valor: textSize 15sp, color #111827
  c) Botón "Editar" que convierte campos de solo lectura a EditText con bg_input.xml
  d) Botón "Guardar cambios": bg_btn_primary.xml
  e) Sección "Cambiar contraseña": inputs actual + nueva + confirmar, todos con toggle ojo

PASO 4 — NO CAMBIAR: apiService.kt, Usuario.kt

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/PerfilActivity.kt app/src/main/res/layout/activity_perfil.xml
git commit -m "feat(mobile-perfil): rediseñar perfil con tabs, campos editables y cambio de contraseña"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-011):
Título: Rediseñar PerfilActivity con diseño de tarjetas y tabs
Descripción: Pantalla de perfil con tabs para información personal, direcciones, cambio de contraseña y eliminación de cuenta. Campos en formato label/value con opción de edición inline. Botón guardar primary. Inputs de contraseña con toggle de visibilidad.
Archivos tocados: 2
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BLOQUE 4 — ADMINISTRADOR (6 pantallas CRUD)
# ══════════════════════════════════════════════════════════════════

NOTA: Los 6 CRUD de admin siguen el MISMO patrón de diseño:
  Header (título + botón "Nuevo")
  Barra de búsqueda (opcional)
  RecyclerView con items
  FAB o botón flotante para agregar
  Dialog para crear/editar
  Swipe o botón para eliminar con confirmación

Por eso los prompts son más cortos — solo cambio los campos específicos.

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 11. UsuariosAdminActivity — CRUD de usuarios (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el CRUD de usuarios del panel admin.

PASO 1 — Lee:
- README.md (sección 4.5: Tablas, 4.9: Toggle, 4.7: Modales)
- Nexbit-Frontend\src\pages\Usuarios.jsx
- Nexbit-Frontend\src\index.css (líneas 186-227: styled-table, 240-266: status-toggle, 269-300: acciones)
- apiService.kt (endpoints CRUD de usuarios)
- model\Usuario.kt, model\Rol.kt

PASO 2 — Modifica:
- ...\ui\UsuariosAdminActivity.kt + activity_usuarios_admin.xml
- ...\layout\item_usuario_admin.xml
- ...\layout\dialog_usuario.xml

PASO 3 — Diseño:
  a) Header: título "Usuarios" + botón "+ Nuevo" (bg_btn_primary.xml pequeño)
  b) Barra de búsqueda (opcional): EditText con bg_input.xml e icono de lupa
  c) Lista de usuarios (item_usuario_admin.xml):
     - Nombre (fontWeight 600)
     - Email (textSize 13sp, color #6B7280)
     - Rol: badge (bg #F3F4F6, text #111827, radius 999dp, padding 4dp 12dp)
     - Estado: Toggle (status-toggle: bg negro=activo, bg #F3F4F6=inactivo, radius 999dp)
     - Acciones: 2 botones circulares (36dp, radius 50%, bg #F9FAFB, border #E5E7EB)
       · Editar (ic_edit.xml) → hover bg #111827 text white
       · Eliminar (ic_trash.xml) → hover bg #EF4444 text white
  d) Dialog crear/editar (dialog_usuario.xml):
     - Título: "Nuevo usuario" / "Editar usuario"
     - Campos: nombre, email, teléfono, dirección, rol (Spinner), activo (CheckBox)
     - Botones: "Cancelar" (bg_btn_secondary.xml) + "Guardar" (bg_btn_primary.xml)
     - Estilo modal: bg white, radius 16dp, padding 24dp

PASO 4 — NO CAMBIAR:
  ✗ apiService.kt, Usuario.kt, Rol.kt
  ✗ Lógica de creación/actualización/eliminación de usuarios

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/UsuariosAdminActivity.kt app/src/main/res/layout/activity_usuarios_admin.xml app/src/main/res/layout/item_usuario_admin.xml app/src/main/res/layout/dialog_usuario.xml
git commit -m "feat(mobile-admin-usuarios): rediseñar CRUD con tabla, toggle activo/inactivo y diálogo modal"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-012):
Título: Rediseñar CRUD de usuarios del panel admin
Descripción: Se rediseñó el CRUD de usuarios con lista (nombre, email, rol badge, toggle activo/inactivo, botones editar/eliminar), barra de búsqueda, diálogo modal para crear/editar (campos: nombre, email, teléfono, dirección, rol spinner, activo checkbox). Botones de acción circulares con hover effects. Toggle de estado con estilo frontend (negro=activo, gris=inactivo, radius 999dp).
Archivos tocados: 4
Riesgo: BAJO
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 12. CategoriasAdminActivity — CRUD de categorías (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el CRUD de categorías. MISMO PATRÓN que UsuariosAdmin.

PASO 1 — Lee:
- Nexbit-Frontend\src\pages\Categorias.jsx
- apiService.kt (endpoints CRUD de categorías)
- model\Categoria.kt, model\CategoriaRequest.kt

PASO 2 — Modifica:
- ...\ui\CategoriasAdminActivity.kt + activity_categorias_admin.xml
- ...\layout\item_categoria_admin.xml
- ...\layout\dialog_categoria.xml

PASO 3 — Diseño (mismo patrón que usuarios):
  - Header: "Categorías" + "+ Nueva"
  - Lista con: nombre, descripción, acciones (editar/eliminar)
  - Dialog: nombre, descripción, activo

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/CategoriasAdminActivity.kt app/src/main/res/layout/activity_categorias_admin.xml app/src/main/res/layout/item_categoria_admin.xml app/src/main/res/layout/dialog_categoria.xml
git commit -m "feat(mobile-admin-categorias): rediseñar CRUD de categorías con tabla y diálogo modal"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-013):
Título: Rediseñar CRUD de categorías
Descripción: Mismo patrón que usuarios: lista con nombre, descripción, acciones editar/eliminar. Diálogo modal con campos nombre y descripción.
Archivos tocados: 4
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 13. ProductosAdminActivity — CRUD de productos (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el CRUD de productos del panel admin.

PASO 1 — Lee:
- README.md (sección 4.5: Tablas)
- Nexbit-Frontend\src\pages\Productos.jsx (versión admin)
- apiService.kt (endpoints CRUD de productos + Multipart para imagen)
- model\Producto.kt, model\Categoria.kt, model\Proveedor.kt

PASO 2 — Modifica:
- ...\ui\ProductosAdminActivity.kt + activity_productos_admin.xml
- ...\layout\item_producto_admin.xml
- ...\layout\dialog_producto.xml

PASO 3 — Diseño:
  a) Header: "Productos" + "+ Nuevo"
  b) Lista con columnas:
     - Imagen pequeña (48dp, radius 8dp)
     - Nombre (fontWeight 600)
     - Categoría (badge #F1F3F5)
     - Precio venta (fontWeight 700, formato moneda)
     - Stock (si stock <= stock_minimo: textColor #DC2626, bg #FEE2E2)
     - Estado: toggle igual que usuarios
     - Acciones: editar/eliminar
  c) Dialog producto:
     - Nombre, descripción, categoría (Spinner), proveedor (Spinner)
     - Precio compra, precio venta (EditText con inputType numberDecimal)
     - Stock actual, stock mínimo (EditText con inputType number)
     - Subir imagen (ImageView + botón "Seleccionar imagen")
     - CheckBox "Activo"

PASO 4 — NO CAMBIAR: apiService.kt, modelos, lógica Multipart de imágenes

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/ProductosAdminActivity.kt app/src/main/res/layout/activity_productos_admin.xml app/src/main/res/layout/item_producto_admin.xml app/src/main/res/layout/dialog_producto.xml
git commit -m "feat(mobile-admin-productos): rediseñar CRUD con imagen, precios, stock y selector de categoría/proveedor"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-014):
Título: Rediseñar CRUD de productos del panel admin
Descripción: CRUD con lista que incluye imagen miniatura, nombre, categoría (badge), precio venta, stock (alerta rojo si stock bajo), toggle activo/inactivo. Diálogo modal con todos los campos: nombre, descripción, categoría spinner, proveedor spinner, precio compra, precio venta, stock actual, stock mínimo, selector de imagen, checkbox activo. Soporte para subida de imágenes Multipart.
Archivos tocados: 4
Riesgo: MEDIO (subida de imágenes, probar Multipart)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 14. PedidosAdminActivity — Gestión de pedidos (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar la gestión de pedidos del admin.

PASO 1 — Lee:
- README.md (sección 4.8: Badges de estado)
- Nexbit-Frontend\src\pages\Pedidos.jsx (versión admin)
- Nexbit-Frontend\src\index.css (líneas 3648-3716: badge-fsm)
- Nexbit-Frontend\src\constants\orderStatuses.js (STATUS_COLORS, FSM_STATUS)
- apiService.kt (endpoints de pedidos)
- model\Pedido.kt

PASO 2 — Modifica:
- ...\ui\PedidosAdminActivity.kt + activity_pedidos_admin.xml
- ...\layout\item_pedido_admin.xml
- ...\layout\dialog_pedido.xml

PASO 3 — Diseño:
  a) Filtros por estado (chips horizontales):
     - Todos, Pendientes, En revisión, Aprobados, En camino, Entregados, Cancelados
     - Cada chip: bg #F3F4F6, text #6B7280, radius 999dp
     - Chip activo: bg #111111, text white
  b) Lista de pedidos (item_pedido_admin.xml):
     - #ID (fontWeight 800)
     - Cliente (textSize 13sp)
     - Fecha (textSize 12sp, color #6B7280)
     - Total (fontWeight 700)
     - Estado: bg_badge_[estado].xml
     - Acciones: botón "Ver" + "Asignar" + "Cambiar estado"
  c) Dialog detalle:
     - Info completa del pedido (cliente, dirección, productos, total)
     - Timeline de seguimiento (lista de cambios de estado con fecha y quién cambió)
     - Selector de nuevo estado (Spinner con los estados disponibles)
     - Campo de nota interna (EditText multiline con bg_input.xml)
     - Botón "Actualizar estado": bg_btn_primary.xml
  d) Modal asignar repartidor:
     - Spinner con lista de repartidores disponibles
     - Botón "Asignar"

PASO 4 — NO CAMBIAR: apiService.kt, Pedido.kt, lógica de estados

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/PedidosAdminActivity.kt app/src/main/res/layout/activity_pedidos_admin.xml app/src/main/res/layout/item_pedido_admin.xml app/src/main/res/layout/dialog_pedido.xml
git commit -m "feat(mobile-admin-pedidos): rediseñar gestión con filtros, timeline y asignación de repartidor"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-015):
Título: Rediseñar gestión de pedidos del admin
Descripción: Lista de pedidos con filtros por estado (chips horizontales), cada card muestra #ID, cliente, fecha, total y badge de estado con colores semánticos. Diálogo de detalle con timeline de seguimiento, selector de nuevo estado, campo de nota interna. Modal de asignación de repartidor con spinner. Los 10 badges de estado usan los colores exactos del frontend.
Archivos tocados: 4
Riesgo: MEDIO (máquina de estados, probar transiciones)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 15. ProveedorActivity — CRUD de proveedores (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

PASO 1 — Lee:
- Nexbit-Frontend\src\pages\Proveedores.jsx
- apiService.kt (endpoints CRUD de proveedores)
- model\Proveedor.kt, model\ProveedorResponse.kt

PASO 2 — Modifica:
- ...\ui\ProveedorActivity.kt + activity_proveedor.xml
- ...\layout\item_proveedor.xml
- ...\layout\dialog_proveedor.xml

PASO 3 — Mismo patrón CRUD:
  Campos: NIT, Nombre, Teléfono, Correo, Dirección, Activo

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/ProveedorActivity.kt app/src/main/res/layout/activity_proveedor.xml app/src/main/res/layout/item_proveedor.xml app/src/main/res/layout/dialog_proveedor.xml
git commit -m "feat(mobile-admin-proveedores): rediseñar CRUD con NIT, nombre, teléfono, correo y dirección"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-016):
Título: Rediseñar CRUD de proveedores
Descripción: Mismo patrón que categorías. Campos: NIT, nombre, teléfono, correo, dirección, activo.
Archivos tocados: 4
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 16. ClientesActivity — Vista de clientes (Admin)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

PASO 1 — Lee:
- Nexbit-Frontend\src\pages\Usuarios.jsx (filtrado por rol Cliente)
- apiService.kt (endpoint getUsuarios)

PASO 2 — Modifica:
- ...\ui\ClientesActivity.kt + activity_clientes.xml
- ...\layout\item_cliente.xml

PASO 3 — Diseño:
  Misma tabla que UsuariosAdmin pero SOLO lectura (sin botones editar/eliminar).
  Columnas: Nombre, Email, Teléfono, Fecha registro.

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/ClientesActivity.kt app/src/main/res/layout/activity_clientes.xml app/src/main/res/layout/item_cliente.xml
git commit -m "feat(mobile-admin-clientes): agregar vista de clientes (solo lectura) con tabla"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-017):
Título: Agregar vista de clientes en panel admin
Descripción: Vista de solo lectura con lista de clientes (nombre, email, teléfono, fecha registro). Mismo diseño de tabla que usuarios pero sin acciones de editar/eliminar.
Archivos tocados: 3
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BLOQUE 5 — REPARTIDOR (3 pantallas)
# ══════════════════════════════════════════════════════════════════

## ────────────────────────────────────────────────────────────────
## 17. EntregasActivity — Inicio del repartidor
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el panel de inicio del repartidor.

PASO 1 — Lee:
- README.md (sección 4.12: ActiveBanner)
- Nexbit-Frontend\src\pages\repartidor\InicioRepartidor.jsx
- Nexbit-Frontend\src\components\features\ActiveBanner.jsx
- Nexbit-Frontend\src\index.css (líneas 3773-3854: active-banner, 3966-4127: orders-feed, order-card, btn-tomar)
- Nexbit-Frontend\src\pages\repartidor\componentes\TarjetaPedido.jsx
- apiService.kt (endpoints de repartidor: getPedidosSinAsignar, asignarPedido)
- model\Pedido.kt, model\RepartidorModels.kt

PASO 2 — Modifica:
- ...\ui\EntregasActivity.kt + activity_entregas.xml
- ...\layout\item_entrega.xml

PASO 3 — Diseño:
  a) ActiveBanner (si tiene pedido activo):
     - Fondo gradiente #FBBF24 → #F59E0B, radius 12dp
     - Icono de moto (ic_truck.xml)
     - "Tienes un pedido activo" fontWeight bold, color #78350F
     - Dirección + ETA
     - Animación de pulso (alertaPulse)
  b) Lista de pedidos disponibles:
     Cada card:
       - bg_card.xml, borde lateral izquierdo verde (#22C55E) si disponible
       - #ID fontWeight 800
       - Cliente, dirección, total (fontWeight 700)
       - Lista de productos (nombres + cantidades)
       - Botón "Tomar pedido": bg #16A34A, text white, fontWeight bold, match_parent
  c) Estado vacío: icono + "No hay pedidos disponibles"

PASO 4 — NO CAMBIAR: apiService.kt, modelos, lógica de Socket.IO ni asignación

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/EntregasActivity.kt app/src/main/res/layout/activity_entregas.xml app/src/main/res/layout/item_entrega.xml
git commit -m "feat(mobile-repartidor-inicio): rediseñar panel con ActiveBanner y lista de pedidos disponibles con botón tomar"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-018):
Título: Rediseñar panel de inicio del repartidor
Descripción: Pantalla principal del repartidor con ActiveBanner (fondo gradiente amarillo, icono moto, info del pedido activo con animación de pulso) y lista de pedidos disponibles (cards con borde verde, info del cliente, dirección, total, productos, botón "Tomar pedido" verde). Estado vacío cuando no hay pedidos.
Archivos tocados: 3
Riesgo: BAJO
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 18. PedidoActivo — Detalle del pedido en curso (Repartidor)
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar la pantalla de pedido activo del repartidor con timeline.

PASO 1 — Lee:
- README.md (timeline en sección 4.9-4.12)
- Nexbit-Frontend\src\pages\repartidor\PedidoActivo.jsx
- Nexbit-Frontend\src\pages\repartidor\componentes\ListaPedidos.jsx
- Nexbit-Frontend\src\pages\repartidor\componentes\ModalDetallePedido.jsx
- Nexbit-Frontend\src\index.css (líneas 4190-4257: delivery-progress, progress-steps, 4260-4451: delivery-card, btn-cta)
- apiService.kt (endpoint cambiarEstadoPedido)

PASO 2 — Modifica:
- ...\ui\PruebasActivity.kt (o la que maneje el pedido activo)
- ...\layout\activity_pruebas.xml

PASO 3 — Diseño:
  a) ProgressBar/Timeline con 4 pasos:
     - ASIGNADO → EN CAMINO → ENTREGADO
     - Cada paso: dot circular (26dp, border-radius 50%)
       · Completado: bg #22C55E
       · Activo: bg #F59E0B con glow (box-shadow)
       · Pendiente: bg #E5E7EB
  b) Card datos del pedido:
     - Cliente, dirección, teléfono, notas de entrega
  c) Lista de productos del pedido (imagen, nombre, cantidad, precio)
  d) Total del pedido (fontWeight 800)
  e) Botones de acción:
     - "Iniciar ruta" (bg #F59E0B, text white) → cambia a EN CAMINO
     - "Marcar como entregado" (bg #16A34A, text white) → cambia a ENTREGADO
     - "Reportar problema" (bg #FEF3C7, text #92400E)
     - "Cancelar entrega" (bg #FEF2F2, text #DC2626)

PASO 4 — NO CAMBIAR: apiService.kt, lógica de cambio de estado

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/PruebasActivity.kt app/src/main/res/layout/activity_pruebas.xml
git commit -m "feat(mobile-repartidor-activo): rediseñar pedido activo con timeline de 4 pasos y botones de acción"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-019):
Título: Rediseñar pantalla de pedido activo del repartidor
Descripción: Timeline visual con 4 pasos (Asignado → En Camino → Entregado) con dots de colores (verde completado, naranja activo con glow, gris pendiente). Card con datos del cliente y pedido, lista de productos, total. Botones contextuales: Iniciar ruta (naranja), Marcar entregado (verde), Reportar problema (amarillo), Cancelar entrega (rojo claro).
Archivos tocados: 2
Riesgo: MEDIO (cambios de estado, probar transiciones)
Dependencias: NEX-001, NEX-002

─────────────────────────────────────────────────────────────────

## ────────────────────────────────────────────────────────────────
## 19. HistorialRepartidor — Historial de entregas
## ────────────────────────────────────────────────────────────────

=== PROMPT ===

INSTRUCCIÓN PARA LA IA:
Rediseñar el historial de entregas del repartidor.

PASO 1 — Lee:
- Nexbit-Frontend\src\pages\repartidor\HistorialRepartidor.jsx
- Nexbit-Frontend\src\index.css (líneas 4529-4656: historial-stats, chip, historial-card)
- apiService.kt (endpoint para historial del repartidor)

PASO 2 — Modifica:
- ...\ui\PerfilPruebaActivity.kt (o la que maneje historial)
- ...\layout\activity_perfil_prueba.xml

PASO 3 — Diseño:
  a) Estadísticas arriba: "Entregados: X" (verde) + "Cancelados: Y" (rojo)
  b) Chips de filtro: "Hoy", "Esta semana", "Este mes"
     - Inactivo: bg #F8F9FA, text #6B7280, radius 999dp
     - Activo: bg #111111, text white
  c) Lista de entregas:
     Cada card:
       - borde izquierdo: verde (#22C55E) si entregado, rojo (#EF4444) si cancelado
       - Icono de estado
       - Dirección, fecha, total
       - Nombre del cliente

PASO 4 — NO CAMBIAR: apiService.kt, lógica de historial

=== FIN ===

▶ COMANDO GIT:
git add app/src/main/java/com/example/nexbitmobile/ui/PerfilPruebaActivity.kt app/src/main/res/layout/activity_perfil_prueba.xml
git commit -m "feat(mobile-repartidor-historial): rediseñar historial con chips de filtro y cards con borde de estado"

▶ DESCRIPCIÓN PARA JIRA (Issue: NEX-020):
Título: Rediseñar historial de entregas del repartidor
Descripción: Pantalla de historial con estadísticas (entregados/cancelados), chips de filtro temporal (hoy, semana, mes), y lista de entregas con cards de borde izquierdo coloreado (verde=entregado, rojo=cancelado), icono de estado, dirección, fecha, total y nombre del cliente.
Archivos tocados: 2
Riesgo: BAJO

─────────────────────────────────────────────────────────────────

# ══════════════════════════════════════════════════════════════════
# BACKLOG HISTÓRICO (solo NEX-001→020)
# ══════════════════════════════════════════════════════════════════
#
# ⚠️  Este backlog está DESACTUALIZADO.
#     El backlog completo y actualizado (NEX-001→033) está en la SECCIÓN B.
#     Este se mantiene solo como referencia histórica.
#

| Issue | Título | Archivos tocados | Riesgo | Depende de |
|-------|--------|-----------------|--------|------------|
| NEX-001 | Sincronizar paleta de colores | 1 | BAJO | — |
| NEX-002 | Crear drawables base (17 archivos) | 17 | BAJO | NEX-001 |
| NEX-003 | Rediseñar LoginActivity | 2 | BAJO | NEX-001, NEX-002 |
| NEX-004 | Rediseñar RegistroActivity | 2 | BAJO | NEX-001, NEX-002 |
| NEX-005 | Rediseñar RecoveryActivity (flujo OTP) | 2 | MEDIO | NEX-001, NEX-002 |
| NEX-006 | Rediseñar Navigation Drawer por rol | 3 | MEDIO | NEX-001, NEX-002 |
| NEX-007 | Rediseñar CatalogoActivity (grid 2 cols) | 3 | BAJO | NEX-001, NEX-002 |
| NEX-008 | Rediseñar ProductDetailActivity | 2 | BAJO | NEX-001, NEX-002 |
| NEX-009 | Rediseñar CarritoActivity | 3 | BAJO | NEX-001, NEX-002 |
| NEX-010 | Rediseñar MisPedidos + OrderDetail | 5 | MEDIO | NEX-001, NEX-002 |
| NEX-011 | Rediseñar PerfilActivity | 2 | BAJO | NEX-001, NEX-002 |
| NEX-012 | Rediseñar CRUD UsuariosAdmin | 4 | BAJO | NEX-001, NEX-002 |
| NEX-013 | Rediseñar CRUD CategoriasAdmin | 4 | BAJO | NEX-001, NEX-002 |
| NEX-014 | Rediseñar CRUD ProductosAdmin | 4 | MEDIO | NEX-001, NEX-002 |
| NEX-015 | Rediseñar gestión PedidosAdmin | 4 | MEDIO | NEX-001, NEX-002 |
| NEX-016 | Rediseñar CRUD ProveedorActivity | 4 | BAJO | NEX-001, NEX-002 |
| NEX-017 | Agregar vista ClientesActivity | 3 | BAJO | NEX-001, NEX-002 |
| NEX-018 | Rediseñar EntregasActivity (repartidor) | 3 | BAJO | NEX-001, NEX-002 |
| NEX-019 | Rediseñar PedidoActivo (timeline) | 2 | MEDIO | NEX-001, NEX-002 |
| NEX-020 | Rediseñar HistorialRepartidor | 2 | BAJO | NEX-001, NEX-002 |

```
╔══════════════════════════════════════════════════════════════════╗
║  TOTAL: 20 issues · ~50 archivos tocados                       ║
║  Riesgo BAJO: 14 · Riesgo MEDIO: 6 · Riesgo ALTO: 0           ║
╚══════════════════════════════════════════════════════════════════╝
```
