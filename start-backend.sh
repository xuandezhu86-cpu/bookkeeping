#!/bin/bash
# 悠然记账 - 后端启动脚本
# 用法: ./start-backend.sh [port]

PORT=${1:-8080}

export JAVA_HOME="c:/Users/33907/.jdks/ms-17.0.18"
export MAVEN_HOME="/c/Program Files/JetBrains/IntelliJ IDEA 2025.1/plugins/maven/lib/maven3"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

cd "$(dirname "$0")/backend"

# 编译 (offline 优先)
echo "[1/2] Compiling..."
mvn compile -q -o 2>/dev/null || mvn compile -q || { echo "[!] Compile failed!"; exit 1; }

# 生成 classpath
if [ ! -f cp.txt ]; then
  echo "Generating classpath..."
  mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q -o 2>/dev/null || \
  mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q
fi

# 启动
echo "[2/2] Starting server on port $PORT..."
echo "Press Ctrl+C to stop."
CP=$(cat cp.txt)
exec "$JAVA_HOME/bin/java" -cp "target/classes;$CP" com.youran.bookkeeping.YouranApplication --server.port=$PORT
