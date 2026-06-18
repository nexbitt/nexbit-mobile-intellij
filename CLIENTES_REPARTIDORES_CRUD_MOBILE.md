# CRUDs Administrativos de Clientes y Repartidores - Mobile

## Resumen de cambios

Se implementaron pantallas inline de administración de Clientes (rol_id=2) y Repartidores (rol_id=4) dentro de `MainOrbixActivity`/`AdminScreens`, replicando la funcionalidad del frontend web.

## Archivos modificados

| Archivo | Acción |
|---------|--------|
| `app/src/main/java/.../api/apiService.kt` | MODIFICADO - 3 endpoints agregados: getRepartidores, toggleActivoRepartidor, desasignarPedido |
| `app/src/main/java/.../model/RepartidorModels.kt` | MODIFICADO - Agregado data class RepartidorListado |
| `app/src/main/java/.../ui/SplashActivity.kt` | MODIFICADO - Ruteo corregido: rol 3→4 |
| `app/src/main/java/.../ui/LoginActivity.kt` | MODIFICADO - Ruteo corregido: rol 3→4 |
| `app/src/main/java/.../ui/AdminScreens.kt` | MODIFICADO - Agregado showClientes() + CRUD completo; reescrito showRepartidores() |
| `app/src/main/java/.../ui/MainOrbixActivity.kt` | MODIFICADO - Agregado ítem "Clientes" al menú + ruteo en showInlineScreen |
| `app/src/main/java/.../ui/RepartidorAdminAdapter.kt` | CREADO - Adaptador para lista de repartidores con toggle activo y detalle |
| `app/src/main/java/.../ui/ClienteAdapter.kt` | MODIFICADO - Agregado soporte para botón editar |
| `res/layout/inline_clientes_admin.xml` | CREADO - Layout con SearchBar, RecyclerView, FAB |
| `res/layout/dialog_cliente.xml` | CREADO - Diálogo crear/editar cliente con todos los campos |
| `res/layout/dialog_repartidor.xml` | CREADO - Diálogo crear/editar repartidor |
| `res/layout/item_repartidor_admin.xml` | CREADO - Card para cada repartidor con badge estado + botones |
| `res/layout/inline_repartidores_admin.xml` | MODIFICADO - Agregados SearchBar, RecyclerView, FAB |
| `res/layout/item_cliente.xml` | MODIFICADO - Agregado btnEditCliente |

## Funcionalidades implementadas

### Clientes
- Lista con búsqueda por nombre, email o documento
- Crear cliente con tipo documento, email, contraseña, teléfono, dirección
- Editar cliente (precargado, password opcional)
- Eliminar cliente con confirmación

### Repartidores
- Lista con búsqueda por nombre, email o teléfono
- Crear/Editar repartidor con toggle activo
- Toggle activo/inactivo directo desde lista
- Vista de detalle inline con:
  - Información del repartidor
  - Botón toggle activo
  - Selector de pedidos sin asignar + botón asignar
  - Lista de pedidos asignados con botón desasignar
- Asignar/Desasignar pedidos desde detalle

### Correcciones
- Ruteo de roles: EntregasActivity ahora se lanza con rol_id=4 (repartidor) en lugar de rol_id=3
