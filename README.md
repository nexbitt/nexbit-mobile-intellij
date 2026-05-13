# Nexbit Mobile

Aplicación móvil desarrollada de forma reproducible y colaborativa. Este repositorio está configurado para evitar conflictos de entornos locales, asegurando un proceso de integración (onboarding) sin fricciones para cualquier desarrollador.

## 🛠 Tecnologías y Herramientas (Stack)

*   **Lenguaje:** Kotlin 2.0.21
*   **Android Gradle Plugin (AGP):** 8.12.0
*   **Gradle:** 8.13 (Vía Wrapper)
*   **Java Development Kit (JDK):** OpenJDK 21 (Corretto 21)
*   **Android SDK:** 35 (API Level)
## 📋 Prerrequisitos

Antes de iniciar, asegúrate de tener instalado:
1.  **Android Studio** (Koala o superior recomendado).
2.  **JDK 21** (Preferiblemente gestionado por el propio IDE o instalado globalmente).
3.  **Git** para el control de versiones.

## 🚀 Primeros Pasos (Setup del Entorno)

Sigue estos pasos rigurosamente para levantar el proyecto sin errores:

1.  **Clonar el repositorio:**
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd nexbitmobile
    ```

2.  **Configurar Variables Locales (`local.properties`):**
    El proyecto no incluye rutas absolutas. Debes definir tu ruta del Android SDK local.
    *   Duplica el archivo de ejemplo:
        ```bash
        # En Windows (PowerShell/CMD):
        copy local.properties.example local.properties
        # En macOS/Linux:
        cp local.properties.example local.properties
        ```
    *   Abre `local.properties` y edita la ruta `sdk.dir` para que apunte a tu SDK local.
    
    *Alternativa (Variables de Entorno Globales):* En lugar de usar `local.properties`, puedes configurar la variable de entorno `ANDROID_HOME` o `ANDROID_SDK_ROOT` a nivel de sistema operativo apuntando a tu SDK.

3.  **Abrir e instalar dependencias:**
    *   Abre Android Studio y selecciona **Open**.
    *   Navega a la carpeta del proyecto clonado y ábrelo.
    *   El IDE iniciará la sincronización (Gradle Sync). Deja que termine de descargar todas las dependencias (usará el Gradle Wrapper).

## 🏃 Cómo Ejecutar el Proyecto

Siempre utiliza el Gradle Wrapper incluido (`gradlew`) para garantizar que usas la versión exacta de Gradle esperada por el proyecto.

**Por Terminal:**
*   Para compilar el APK de desarrollo:
    ```bash
    # En macOS/Linux:
    ./gradlew assembleDebug
    
    # En Windows:
    gradlew.bat assembleDebug
    ```
*   Para ejecutar las pruebas:
    ```bash
    ./gradlew test
    ```

**Por IDE (Android Studio):**
*   Selecciona la configuración de ejecución "app" en la parte superior.
*   Haz clic en el botón de **Run** (triángulo verde) o presiona `Shift + F10`.

## 🔧 Solución de Problemas (Troubleshooting)

*   **Error: "Unsupported class file major version..." / "Java compiler version mismatch"**
    *   *Solución:* Asegúrate de que estás compilando con **JDK 21**. En Android Studio, ve a `File > Settings` (o `Android Studio > Settings` en macOS) `> Build, Execution, Deployment > Build Tools > Gradle` y asegúrate de que el **Gradle JDK** está configurado en la versión 21.
*   **Error: "SDK location not found. Define location with an ANDROID_SDK_ROOT..."**
    *   *Solución:* Te falta el archivo `local.properties` con la ruta `sdk.dir` configurada o tu variable de entorno `ANDROID_HOME` no está definida. Revisa el paso 2 de Primeros Pasos.
*   **Cambios en `local.properties` aparecen en Git:**
    *   *Solución:* Esto no debería ocurrir, ya que está en el `.gitignore`. Si ocurre, limpia el caché de git:
        ```bash
        git rm --cached local.properties
        git commit -m "chore: eliminar local.properties del tracking"
        ```

## 📐 Estructura del Proyecto

*   `/app`: Módulo principal de la aplicación que contiene el código fuente (`src/main/java`), recursos (`src/main/res`) y manifiesto.
*   `/gradle`: Contiene el wrapper de Gradle, el cual estandariza la versión de construcción.
*   `build.gradle.kts` / `settings.gradle.kts`: Archivos de configuración de compilación usando Kotlin DSL.

---
*Mantenido y desarrollado siguiendo principios de infraestructura como código y reproducibilidad de entornos.*
