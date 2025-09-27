#!/usr/bin/env bash
#
# Spendi - Project Management Utility
# Description: Build, run, stop, and manage the Spendi backend (Javalin).
# Author: chyvacheck (reworked by assistant)
# Version: 2.0.0
# Date: 2025-09-13
# Platforms: macOS, Debian/Ubuntu
#
# Commands:
#   build        - mvn clean package -DskipTests
#   test         - mvn test (unit tests only)
#   test:it      - mvn failsafe:integration-test failsafe:verify (integration tests only)
#   clean        - mvn clean
#   run          - start in background (with PID file)
#   run:fg       - start in foreground (dev)
#   stop         - graceful stop by PID (TERM -> wait -> KILL)
#   restart      - stop then run
#   reload       - alias to restart
#   status       - show status by PID/port
#   version      - print Java/Maven/App versions
#   doctor       - preflight checks (tools, ports, dirs)
#
# Notes:
# - App config & logs are handled by the application (.env and storage/logs).
# - This script will NOT redirect logs; background mode detaches stdout/stderr.
# - PID file lives at .runtime/spendi.pid relative to the project root.
# - Default port is 6070 (override with env PORT=XXXX before the command).
# - Requires Java 21+ and Maven available in PATH.
#

set -euo pipefail

# ----------------------------- Colors & Emojis -------------------------------
: "${NO_COLOR:=}"  # if set (non-empty), disables colors

if [[ -t 1 ]] && [[ -z "${NO_COLOR}" ]] && [[ "${TERM:-}" != "dumb" ]]; then
  RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; BLUE=$'\033[0;34m'; NC=$'\033[0m'
else
  RED=""; GREEN=""; YELLOW=""; BLUE=""; NC=""
fi

# Emojis (fallback to plain text when not supported is okay)
CHECK="✅"; CROSS="❌"; WARN="⚠️"; INFO="ℹ️"; ROCKET="🚀"; HAMMER="🔨"; POWER="🟢"; STOP="🛑"

# ------------------------------ Paths & Setup --------------------------------
# Resolve script directory (portable across macOS/Linux; no readlink -f)
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}"
RUNTIME_DIR="${PROJECT_ROOT}/.runtime"
PID_FILE="${RUNTIME_DIR}/spendi.pid"
TARGET_DIR="${PROJECT_ROOT}/target"
DEFAULT_PORT="${PORT:-6070}"

mkdir -p "${RUNTIME_DIR}"

# ------------------------------ Utilities ------------------------------------
die() { echo -e "${RED}${CROSS} $*${NC}" >&2; exit 1; }
note() { echo -e "${BLUE}${INFO} $*${NC}"; }
warn() { echo -e "${YELLOW}${WARN} $*${NC}"; }
ok()  { echo -e "${GREEN}${CHECK} $*${NC}"; }

have_cmd() { command -v "$1" >/dev/null 2>&1; }

require_tools() {
  local missing=0

  if ! have_cmd java; then
    warn "Java не найдена в PATH."
    missing=1
  fi
  if ! have_cmd mvn; then
    warn "Maven не найден в PATH."
    missing=1
  fi
  if (( missing )); then
    die "Пожалуйста, установите Java (21+) и Maven."
  fi

  # Java version check (needs 21+)
  local ver_line major
  ver_line="$(java -version 2>&1 | head -n1)"
  # Examples:
  #   openjdk version "21.0.2"  -> major=21
  #   openjdk version "23"      -> major=23
  #   java version "1.8.0_312"  -> major=1 (legacy)
  major="$(sed -nE 's/.*version "([0-9]+)(\.[0-9]+)?(\.[0-9]+)?.*/\1/p' <<<"$ver_line")"
  if [[ -z "${major}" ]]; then
    warn "Не удалось определить версию Java из строки: ${ver_line}"
  else
    if (( major < 21 )); then
      die "Требуется Java 21 или выше. Обнаружено: ${ver_line}"
    fi
  fi
}

find_jar() {
  # Pick the newest JAR in target/ (excluding test jars if present)
  if [[ ! -d "${TARGET_DIR}" ]]; then
    echo ""
    return 0
  fi
  local jar
  jar="$(ls -1t "${TARGET_DIR}"/*.jar 2>/dev/null | head -n1 || true)"
  echo "${jar}"
}

pid_is_alive() {
  local pid="$1"
  if [[ -z "${pid}" ]]; then return 1; fi
  if kill -0 "${pid}" 2>/dev/null; then
    return 0
  else
    return 1
  fi
}

read_pid() {
  [[ -f "${PID_FILE}" ]] || { echo ""; return 1; }
  local pid
  pid="$(cat "${PID_FILE}" 2>/dev/null || true)"
  if [[ -z "${pid}" ]]; then
    rm -f "${PID_FILE}"
    echo ""
    return 1
  fi
  echo "${pid}"
  return 0
}

port_in_use() {
  local port="$1"
  # Try lsof (macOS, often installed on Linux too)
  if have_cmd lsof; then
    if lsof -iTCP:"${port}" -sTCP:LISTEN -Pn >/dev/null 2>&1; then return 0; else return 1; fi
  fi
  # Try ss (Linux)
  if have_cmd ss; then
    if ss -ltn "( sport = :${port} )" | tail -n +2 | grep -q .; then return 0; else return 1; fi
  fi
  # Try netstat (fallback)
  if have_cmd netstat; then
    if netstat -ltn 2>/dev/null | awk '{print $4}' | grep -E "[:.]${port}\$" -q; then return 0; else return 1; fi
  fi
  # If no tool available, we can't reliably detect
  warn "Не удалось проверить занятость порта ${port} (нет lsof/ss/netstat). Продолжаем на свой риск."
  return 1
}

ensure_port_free() {
  local port="${1:-$DEFAULT_PORT}"
  if port_in_use "${port}"; then
    die "Порт ${port} уже занят. Запуск отменён."
  fi
}

write_pid() {
  local pid="$1"
  echo "${pid}" > "${PID_FILE}"
}

clear_pid_if_stale() {
  if [[ -f "${PID_FILE}" ]]; then
    local pid
    pid="$(cat "${PID_FILE}" 2>/dev/null || true)"
    if [[ -n "${pid}" && ! $(kill -0 "${pid}" 2>/dev/null && echo alive) ]]; then
      warn "Найден битый PID-файл (${PID_FILE}). Удаляю..."
      rm -f "${PID_FILE}"
      return 0
    fi
  fi
  return 0
}

# ------------------------------ Commands -------------------------------------
cmd_build() {
  require_tools
  note "${HAMMER} Сборка проекта (skip tests)..."
  (cd "${PROJECT_ROOT}" && mvn clean package -DskipTests)
  ok "Проект успешно собран."
}

cmd_test() {
  require_tools
  note "Запуск тестов..."
  (cd "${PROJECT_ROOT}" && mvn test)
  ok "Unit-тесты выполнены."
}

cmd_test_it() {
  require_tools
  note "Запуск интеграционных тестов (Failsafe)..."
  (cd "${PROJECT_ROOT}" && mvn -DtrimStackTrace=false test-compile failsafe:integration-test failsafe:verify)
  ok "Интеграционные тесты выполнены."
}

cmd_clean() {
  require_tools
  note "Очистка артефактов..."
  (cd "${PROJECT_ROOT}" && mvn clean)
  ok "Готово."
}

cmd_status() {
  clear_pid_if_stale
  local pid
  pid="$(read_pid || true)"
  if [[ -n "${pid}" ]] && pid_is_alive "${pid}"; then
    ok "${POWER} Приложение запущено. PID: ${pid}"
    echo "PID-файл: ${PID_FILE}"
    echo "Порт (ожидаемый): ${DEFAULT_PORT}"
    return 0
  fi
  if [[ -f "${PID_FILE}" ]]; then
    warn "Процесс не найден, но PID-файл существовал. PID-файл удалён."
    rm -f "${PID_FILE}"
  fi
  echo -e "${YELLOW}${WARN} Приложение не запущено.${NC}"
  return 3
}

cmd_run_fg() {
  require_tools
  clear_pid_if_stale
  ensure_port_free "${DEFAULT_PORT}"

  local jar
  jar="$(find_jar)"
  if [[ -z "${jar}" ]]; then
    warn "Не найден JAR в target/. Выполняю сборку..."
    cmd_build
    jar="$(find_jar)"
    [[ -n "${jar}" ]] || die "После сборки JAR всё равно не найден."
  fi

  note "${ROCKET} Запуск в foreground (dev). Порт: ${DEFAULT_PORT}"
  note "Ctrl+C для остановки. PID-файл не создаётся."

  # Forward signals to child
  trap 'echo; warn "Перехвачен Ctrl+C/SIGINT. Останавливаю приложение..."; kill -TERM $child 2>/dev/null || true; wait $child 2>/dev/null || true; exit 130' INT
  trap 'echo; warn "Получен SIGTERM. Останавливаю приложение..."; kill -TERM $child 2>/dev/null || true; wait $child 2>/dev/null || true; exit 143' TERM

  (cd "${PROJECT_ROOT}" && exec java -jar "${jar}") &
  local child=$!
  wait "$child"
}

cmd_run_bg() {
  require_tools
  clear_pid_if_stale
  if [[ -f "${PID_FILE}" ]]; then
    local oldpid
    oldpid="$(cat "${PID_FILE}" 2>/dev/null || true)"
    if [[ -n "${oldpid}" ]] && pid_is_alive "${oldpid}"; then
      die "Приложение уже запущено (PID ${oldpid})."
    else
      warn "Удаляю устаревший PID-файл."
      rm -f "${PID_FILE}"
    fi
  fi

  ensure_port_free "${DEFAULT_PORT}"

  local jar
  jar="$(find_jar)"
  if [[ -z "${jar}" ]]; then
    warn "Не найден JAR в target/. Выполняю сборку..."
    cmd_build
    jar="$(find_jar)"
    [[ -n "${jar}" ]] || die "После сборки JAR всё равно не найден."
  fi

  note "${ROCKET} Запуск в background. Порт: ${DEFAULT_PORT}"

  # ВАЖНО: запускаем в ТЕКУЩЕМ SHELL, чтобы $! был доступен.
  cd "${PROJECT_ROOT}" || die "Не удалось перейти в ${PROJECT_ROOT}"
  nohup java -jar "${jar}" >/dev/null 2>&1 &
  local pid=$!

  # Небольшая пауза и проверка, что процесс жив
  sleep 0.5
  if ! pid_is_alive "${pid}"; then
    # Fallback: попытка найти по имени JAR
    local jarname found
    jarname="$(basename "${jar}")"
    found="$(pgrep -f "java .*${jarname}" | head -n1 || true)"
    if [[ -n "${found}" ]] && pid_is_alive "${found}"; then
      pid="${found}"
    fi
  fi

  if pid_is_alive "${pid}"; then
    write_pid "${pid}"
    ok "Приложение запущено. PID: ${pid}"
    echo "PID-файл: ${PID_FILE}"
  else
    die "Не удалось запустить приложение (PID не обнаружен)."
  fi
}

cmd_stop() {
  clear_pid_if_stale
  local pid
  pid="$(read_pid || true)"
  if [[ -z "${pid}" ]]; then
    warn "Приложение не запущено (PID-файл отсутствует)."
    return 3
  fi
  if ! pid_is_alive "${pid}"; then
    warn "Процесс PID ${pid} не найден. Удаляю PID-файл."
    rm -f "${PID_FILE}"
    return 3
  fi

  note "${STOP} Остановка PID ${pid} (TERM)..."
  kill -TERM "${pid}" 2>/dev/null || true

  local waited=0 timeout=10
  while pid_is_alive "${pid}" && (( waited < timeout )); do
    sleep 1
    (( waited++ ))
  done

  if pid_is_alive "${pid}"; then
    warn "Процесс не завершился за ${timeout}с. Принудительная остановка (KILL)."
    kill -KILL "${pid}" 2>/dev/null || true
    sleep 0.5
  fi

  rm -f "${PID_FILE}"
  ok "Приложение остановлено."
}

cmd_restart() {
  cmd_stop || true
  # Короткая пауза, чтобы ОС освободила порт
  sleep 0.5
  cmd_run_bg
}

cmd_version() {
  if have_cmd java; then
    echo "Java: $(java -version 2>&1 | head -n1)"
  else
    echo "Java: (не найдена)"
  fi
  if have_cmd mvn; then
    echo "Maven: $(mvn -v 2>/dev/null | head -n1)"
  else
    echo "Maven: (не найден)"
  fi
  # Try to read app version from Maven
  if have_cmd mvn; then
    local app_ver
    app_ver="$(cd "${PROJECT_ROOT}" && mvn -q -DforceStdout help:evaluate -Dexpression=project.version 2>/dev/null || true)"
    if [[ -n "${app_ver}" ]]; then
      echo "App version (pom.xml): ${app_ver}"
    fi
  fi
}

cmd_doctor() {
  local rc=0
  echo "=== Doctor ==="
  if have_cmd java; then ok "Java найдена"; else warn "Java не найдена"; rc=1; fi
  if have_cmd mvn; then ok "Maven найден"; else warn "Maven не найден"; rc=1; fi

  # Java version check
  if have_cmd java; then
    local ver_line major
    ver_line="$(java -version 2>&1 | head -n1)"
    major="$(sed -nE 's/.*version "([0-9]+).*/\1/p' <<<"$ver_line")"
    if [[ -n "${major}" && "${major}" -ge 21 ]]; then
      ok "Java версия подходит: ${ver_line}"
    else
      warn "Java версия недостаточна: ${ver_line} (нужно 21+)"; rc=1
    fi
  fi

  # Port check
  if port_in_use "${DEFAULT_PORT}"; then
    warn "Порт ${DEFAULT_PORT} уже занят."
  else
    ok "Порт ${DEFAULT_PORT} свободен."
  fi

  # Runtime dir
  if [[ -w "${RUNTIME_DIR}" ]]; then ok "Каталог runtime доступен: ${RUNTIME_DIR}"; else warn "Нет прав на ${RUNTIME_DIR}"; rc=1; fi

  exit "${rc}"
}

usage() {
  cat <<EOF
${BLUE}Spendi - утилита управления${NC}

Использование: $(basename "$0") <команда>

Команды:
  ${GREEN}build${NC}       Сборка (mvn clean package -DskipTests)
  ${GREEN}test${NC}        Запуск тестов (mvn test)
  ${GREEN}test:it${NC}     Интеграционные тесты (mvn failsafe:integration-test failsafe:verify)
  ${GREEN}clean${NC}       Очистка (mvn clean)
  ${GREEN}run${NC}         Запуск в фоне (PID-файл: .runtime/spendi.pid)
  ${GREEN}run:fg${NC}      Запуск в консоли (для разработки)
  ${GREEN}stop${NC}        Остановка процесса по PID (TERM -> KILL)
  ${GREEN}restart${NC}     Перезапуск (stop -> run)
  ${GREEN}reload${NC}      Алиас к restart
  ${GREEN}status${NC}      Статус (PID/порт)
  ${GREEN}version${NC}     Версии Java/Maven/Приложения
  ${GREEN}doctor${NC}      Пред-проверка окружения

Переменные окружения:
  PORT      Порт приложения (по умолчанию 6070)

Примеры:
  PORT=6070 ./spendi.sh run
  ./spendi.sh run:fg
  ./spendi.sh status
  ./spendi.sh stop
EOF
}

# ------------------------------ Dispatcher -----------------------------------
cmd="${1:-}"
case "${cmd}" in
  build)    shift; cmd_build "$@";;
  test)     shift; cmd_test "$@";;
  test:it)  shift; cmd_test_it "$@";;
  clean)    shift; cmd_clean "$@";;
  run)      shift; cmd_run_bg "$@";;
  run:fg)   shift; cmd_run_fg "$@";;
  stop)     shift; cmd_stop "$@";;
  restart)  shift; cmd_restart "$@";;
  reload)   shift; cmd_restart "$@";;
  status)   shift; cmd_status "$@";;
  version)  shift; cmd_version "$@";;
  doctor)   shift; cmd_doctor "$@";;
  ""|"help"|"-h"|"--help") usage;;
  *) die "Неизвестная команда: ${cmd}. См. 'help'.";;
esac
