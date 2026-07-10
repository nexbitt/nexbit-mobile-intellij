@echo off
cd /d "%~dp0"
echo ==========================================
echo  Nexbit Mobile - Clean Build
echo ==========================================
echo.
echo 1. Limpiando build anterior...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al limpiar.
    exit /b %errorlevel%
)
echo.
echo 2. Descargando dependencias...
call gradlew.bat --refresh-dependencies
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al descargar dependencias.
    exit /b %errorlevel%
)
echo.
echo 3. Compilando APK de debug...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar.
    exit /b %errorlevel%
)
echo.
echo ==========================================
echo  BUILD EXITOSO
echo ==========================================
echo  APK: app\build\outputs\apk\debug\app-debug.apk
echo.
echo  Para instalar en dispositivo/emulador:
echo     gradlew.bat installDebug
echo.
pause
