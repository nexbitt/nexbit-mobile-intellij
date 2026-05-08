# Guía de Transición: Nexbit Web a Nexbit Mobile

Este documento sirve como puente técnico para el desarrollo de la aplicación móvil de Nexbit, asegurando que la lógica de negocio y las estructuras de datos sean consistentes entre el Backend (Express/MySQL) y el Frontend Mobile (Android Kotlin).

---

## 🏗️ 1. Estructura de Proyectos

### 📂 Backend (`Nexbit-Backend`)
Usa una arquitectura **MVC** con **Prisma ORM** para la base de datos.
- `backend/server.js`: Punto de entrada del servidor.
- `backend/app.js`: Configuración de Express, CORS y registro de rutas.
- `backend/routes/`: Definición de endpoints API (REST).
- `backend/models/`: Lógica de acceso a datos (Prisma).
- `db/`: Scripts SQL (`schema.sql`) que definen la estructura exacta de MySQL.

### 📂 Frontend Web (`Nexbit-Frontend`)
Basado en **React + Vite**.
- `src/api.js`: Configuración de Axios con `withCredentials: true`.
- `src/services/authService.js`: Manejo de sesión.
- **Nota Importante:** La web usa **Cookies HttpOnly** para el JWT. En Mobile (Android), deberemos manejar el almacenamiento del Token manualmente (preferiblemente `EncryptedSharedPreferences`).

---

## 🗄️ 2. Estructura de la Base de Datos (Clave para Modelos)

Para evitar errores de sintaxis y ortografía en Kotlin, usa estos nombres exactos de campos:

### Tabla: `usuarios`
- `id_usuario` (INT, PK)
- `rol_id` (INT)
- `nombre` (VARCHAR 100)
- `email` (VARCHAR 100)
- `password` (VARCHAR 255 - Hashed)
- `tipo_documento`, `numero_documento`
- `telefono`, `direccion`
- `activo` (BOOLEAN/TINYINT)

### Tabla: `productos`
- `id_producto` (INT, PK)
- `categoria_id` (INT)
- `nombre` (VARCHAR 150)
- `descripcion` (TEXT)
- `imagen_url` (VARCHAR 500)
- `precio_venta` (DECIMAL 12,2)
- `stock_actual` (INT)

---

## 🌐 3. Endpoints Principales (API REST)

Base URL sugerida para desarrollo: `http://10.0.2.2:3000/api` (IP local para el emulador Android).

### Autenticación (`/usuarios`)
- **POST** `/login`: Recibe `{ email, password }`. Devuelve los datos del usuario.
- **POST** `/logout`: Limpia la sesión.
- **GET** `/me`: Obtiene el perfil del usuario autenticado (requiere token).

### Catálogo
- **GET** `/productos`: Lista todos los productos disponibles.
- **GET** `/categorias`: Categorías para filtros.

### Pedidos y Carrito
- **GET** `/carrito`: Obtiene el carrito del usuario.
- **POST** `/pedidos`: Crea una nueva orden.

---

## 🔐 4. Seguridad y Conexión Mobile

1. **Gestión de Tokens:** A diferencia de la web, la App Android debe capturar el token del header (si el backend se cambia para enviarlo en el body/header) o manejar la persistencia de la cookie. 
   - *Recomendación:* Asegurarse de que el Backend permita autenticación por Header `Authorization: Bearer <TOKEN>` para facilitar el desarrollo mobile.
2. **CORS:** El archivo `app.js` ya incluye `http://localhost:8081` (React Native) y permite orígenes nulos. Para Android nativo, asegúrate de que el servidor acepte la IP del dispositivo físico o emulador.
3. **Tipado:** Usa `Double` o `BigDecimal` en Kotlin para los precios (`precio_venta`), ya que en DB son `DECIMAL`.

---

## 📝 5. Notas de Desarrollo
- El backend usa **bcrypt** para contraseñas.
- Los booleanos en la DB se guardan como `0/1`. El modelo del backend tiene una función `normalizeActivo()` para manejar esto; asegúrate de enviar `true/false` reales desde la App.
- **Ortografía:** Revisa siempre que los campos de tus clases `data class` en Kotlin coincidan exactamente con `schema.sql`.

---
*Documentación generada para el equipo de desarrollo mobile - Nexbit.*
