#!/bin/bash

set -euo pipefail

# === Конфигурация ===
SERVER="root@bpmn.evteev.ru"
REMOTE_BASE="/opt/bpmn"

# === Java-модули: локальная_папка:папка_на_удалённом_сервере ===
JAVA_MODULES=(
  "dialog-service:dialog-service"
  "voice-to-text-service:voice-to-text-service"
)

# === Node.js модуль ===
BPMN_RENDER_SERVICE="bpmn-render-service"
BPMN_RENDER_SERVICE_FILES=(
  "Dockerfile"
  "index.js"
  "package.json"
  "render.js"
  "renderSvgToPdf.js"
  "swagger.yaml"
)

# === Сборка Java-модулей ===
echo "Сборка всех Java-модулей..."
mvn clean package # -DskipTests

# === Деплой Java-модулей ===
for module_def in "${JAVA_MODULES[@]}"; do
  IFS=":" read -r local_module remote_module <<< "$module_def"

  local_jar="$local_module/target/${local_module}-0.0.1-SNAPSHOT.jar"
  remote_dir="$REMOTE_BASE/$remote_module"
  remote_target_dir="$remote_dir/target"

  echo "Создаём директории: $remote_target_dir"
  ssh "$SERVER" "mkdir -p '$remote_target_dir'"

  echo "Копируем JAR → $remote_target_dir"
  scp "$local_jar" "$SERVER:$remote_target_dir/"

  echo "Копируем Dockerfile → $remote_dir"
  ssh "$SERVER" "mkdir -p '$remote_dir'"
  scp "$local_module/Dockerfile" "$SERVER:$remote_dir/"
done

# === Деплой Node.js модуля ===
REMOTE_NODE_DIR="$REMOTE_BASE/$BPMN_RENDER_SERVICE"
echo "Создаём директорию Node-модуля: $REMOTE_NODE_DIR"
ssh "$SERVER" "mkdir -p '$REMOTE_NODE_DIR'"

echo "Копируем файлы $BPMN_RENDER_SERVICE → $REMOTE_NODE_DIR"
for file in "${BPMN_RENDER_SERVICE_FILES[@]}"; do
  echo "- $file"
  scp "$BPMN_RENDER_SERVICE/$file" "$SERVER:$REMOTE_NODE_DIR/$file"
done

# === Копирование docker-compose.yml в корень ===
echo "Копируем docker-compose.yml and .env → $REMOTE_BASE"
scp docker-compose.yml "$SERVER:$REMOTE_BASE/"
scp .env "$SERVER:$REMOTE_BASE/"

# === Перезапуск docker-compose ===
echo "Перезапуск docker-compose..."
ssh "$SERVER" "cd '$REMOTE_BASE' && docker-compose down && docker-compose up --build -d"

echo "Деплой завершён успешно"
