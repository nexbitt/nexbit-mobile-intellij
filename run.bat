@echo off
cd /d "%~dp0"
echo Compilando e instalando...
call gradlew.bat installDebug
echo Abriendo app...
adb shell am start -n com.example.nexbitmobile/.ui.SplashActivity
echo Listo!
