#!/bin/bash

# =============================================================================
# SPENDI BACKEND - QUICK DEVELOPMENT COMMANDS
# =============================================================================
# Набор команд для быстрой разработки
# =============================================================================

case "$1" in
    "start" | "run")
        echo "🚀 Запуск приложения..."
        mvn compile exec:java
        ;;
    "build")
        echo "🔨 Сборка проекта..."
        mvn clean compile
        ;;
    "package")
        echo "📦 Создание JAR файла..."
        mvn clean package
        ;;
    "test")
        echo "🧪 Запуск тестов..."
        mvn test
        ;;
    "clean")
        echo "🧹 Очистка проекта..."
        mvn clean
        ;;
    "install")
        echo "📥 Установка зависимостей..."
        mvn clean install
        ;;
    "dev")
        echo "🔄 Запуск в режиме разработки с автоперезагрузкой..."
        echo "💡 Для остановки нажмите Ctrl+C"
        mvn compile exec:java
        ;;
    *)
        echo "📋 Доступные команды:"
        echo "  ./dev.sh start    - Запуск приложения"
        echo "  ./dev.sh build    - Сборка проекта"
        echo "  ./dev.sh package  - Создание JAR"
        echo "  ./dev.sh test     - Запуск тестов"
        echo "  ./dev.sh clean    - Очистка проекта"
        echo "  ./dev.sh install  - Установка зависимостей"
        echo "  ./dev.sh dev      - Режим разработки"
        echo ""
        echo "💡 Примеры использования:"
        echo "  ./dev.sh start"
        echo "  ./dev.sh build"
        echo "  ./dev.sh test"
        ;;
esac
