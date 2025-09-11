#!/bin/bash

# =============================================================================
# SPENDI BACKEND - DEVELOPMENT START SCRIPT
# =============================================================================
# Скрипт для быстрого запуска приложения в режиме разработки
# =============================================================================

echo "🚀 Starting Spendi Backend..."
echo "📁 Working directory: $(pwd)"
echo ""

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не найден. Установите Java 21 или выше."
    exit 1
fi

# Проверяем наличие Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не найден. Установите Apache Maven."
    exit 1
fi

# Проверяем версию Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "⚠️  Внимание: Рекомендуется Java 21+. Текущая версия: $JAVA_VERSION"
fi

echo "☕ Java version: $(java -version 2>&1 | head -n 1)"
echo "📦 Maven version: $(mvn -version | head -n 1)"
echo ""

# Компилируем и запускаем приложение
echo "🔨 Компиляция и запуск приложения..."
echo "📝 Логи приложения:"
echo "----------------------------------------"

# Запускаем через Maven exec plugin
mvn compile exec:java

echo ""
echo "✅ Приложение завершено"
