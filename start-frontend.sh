#!/bin/bash
# 悠然记账 - 前端启动脚本
# 用法: ./start-frontend.sh [dev|electron]

MODE=${1:-dev}

cd "$(dirname "$0")/frontend"

# 安装依赖
if [ ! -d "node_modules" ]; then
  echo "[*] Installing dependencies..."
  pnpm install || { echo "[!] pnpm install failed"; exit 1; }
fi

if [ "$MODE" = "electron" ]; then
  echo "Starting Electron app..."
  pnpm electron:dev
else
  echo "Starting Vite dev server..."
  echo "Open http://localhost:5173 in your browser."
  pnpm dev
fi
