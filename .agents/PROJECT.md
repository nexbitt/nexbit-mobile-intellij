# PROJECT.md
> Generado por el agente de planificación técnica.
> Creado: 2026-06-23 | Actualizado: 2026-06-23 | Motivo: Descubrimiento inicial

## Origen
- **Tipo:** Heredado (monorepo con 3 proyectos en desarrollo activo)
- **Archivos usados para inferencia:** `README.md`, `estilo.md`, `Nexbit-Frontend/PROJECT.md`, `nexbit-mobile-intellij/PROJECT.md`, `Nexbit-Frontend/src/App.jsx`, `nexbit-mobile-intellij/app/src/main/java/com/example/nexbitmobile/ui/` (35+ activities)

## Stack
- **Frontend:** React 19 + Vite 8 + CSS vanilla (tema "Antigravity")
- **Backend:** Express 5 + Prisma + MySQL
- **Mobile:** Android nativo (Kotlin 2.0.21, SDK 35, XML layouts, sin Jetpack Compose)
- **Tiempo real:** Socket.IO (backend + frontend, NO implementado en mobile)
- **Imágenes:** Cloudinary (backend), Glide (mobile), Axios (frontend)

## Mapa del monorepo
- `/Nexbit-Frontend/` → SPA React 19 (panel admin, tienda, repartidor)
- `/Nexbit-Backend/` → API Express + Prisma/MySQL
- `/nexbit-mobile-intellij/` → App Android nativa (Kotlin)
- `/estilo.md` → Guía de estilos y prompts para sincronizar Frontend → Mobile

## Estado actual de sincronización
- **Completado (20 issues NEX-001→020):** Colores, drawables, auth, navegación, CRUDs principales, catálogo, carrito, pedidos
- **Pendiente (13+ issues):** Roles CRUD, Repartidores CRUD, ConfirmacionPedido, botones faltantes en varias pantallas, Chat, reportes completos, landing page, perfil con tabs, perfil repartidor, páginas estáticas

## Brecha principal
El mobile tiene 22 Activities pero faltan botones/acciones clave para igualar el frontend:
- 6 pantallas completas faltan (nuevas Activities)
- 15+ acciones/botones faltan en Activities existentes
- 5 componentes UI compartidos faltan (ChatModal, toasts, checkout modal, dialog unificado)
