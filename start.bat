@echo off
REM =============================================================================
REM SPENDI BACKEND - DEVELOPMENT START SCRIPT (Windows)
REM =============================================================================
REM Скрипт для быстрого запуска приложения в режиме разработки
REM =============================================================================

echo 🚀 Starting Spendi Backend...
echo 📁 Working directory: %cd%
echo.

REM Проверяем наличие Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java не найден. Установите Java 21 или выше.
    pause
    exit /b 1
)

REM Проверяем наличие Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven не найден. Установите Apache Maven.
    pause
    exit /b 1
)

echo ☕ Java version:
java -version 2>&1 | findstr "version"
echo 📦 Maven version:
mvn -version 2>&1 | findstr "Apache Maven"
echo.

REM Компилируем и запускаем приложение
echo 🔨 Компиляция и запуск приложения...
echo 📝 Логи приложения:
echo ----------------------------------------

REM Запускаем через Maven exec plugin
mvn compile exec:java

echo.
echo ✅ Приложение завершено
pause
