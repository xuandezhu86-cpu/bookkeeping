#!/bin/bash
# 悠然记账 - 后端启动脚本 (Linux/Mac/Git Bash)
# 用法: ./start-backend.sh [all|service-name]

SERVICE=${1:-all}

export JAVA_HOME="c:/Users/33907/.jdks/ms-17.0.18"
export MAVEN_HOME="/c/Program Files/JetBrains/IntelliJ IDEA 2025.1/plugins/maven/lib/maven3"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"

# Service config
declare -A SERVICES
SERVICES["gateway-service"]=8080
SERVICES["auth-service"]=8081
SERVICES["category-service"]=8082
SERVICES["expense-service"]=8083
SERVICES["budget-service"]=8084
SERVICES["statistics-service"]=8085

declare -A MAIN_CLASSES
MAIN_CLASSES["gateway-service"]="com.youran.bookkeeping.gateway.GatewayApplication"
MAIN_CLASSES["auth-service"]="com.youran.bookkeeping.auth.AuthApplication"
MAIN_CLASSES["category-service"]="com.youran.bookkeeping.category.CategoryApplication"
MAIN_CLASSES["expense-service"]="com.youran.bookkeeping.expense.ExpenseApplication"
MAIN_CLASSES["budget-service"]="com.youran.bookkeeping.budget.BudgetApplication"
MAIN_CLASSES["statistics-service"]="com.youran.bookkeeping.statistics.StatisticsApplication"

# Compile all modules
cd "$BACKEND_DIR"
echo "[1/2] Compiling multi-module project..."
mvn compile -q -o 2>/dev/null || mvn compile -q || { echo "[!] Compile failed!"; exit 1; }

# Generate classpath files
for svc in "${!SERVICES[@]}"; do
    cpfile="$BACKEND_DIR/$svc/cp.txt"
    if [ ! -f "$cpfile" ]; then
        echo "  Generating classpath for $svc..."
        (cd "$BACKEND_DIR/$svc" && mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q -o 2>/dev/null || \
         mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q)
    fi
done

start_service() {
    local name=$1
    local port=${SERVICES[$name]}
    local main=${MAIN_CLASSES[$name]}
    local dir="$BACKEND_DIR/$name"
    local cpfile="$dir/cp.txt"

    if [ ! -f "$cpfile" ]; then
        echo "[!] No cp.txt for $name, skipping..."
        return
    fi

    local CP=$(cat "$cpfile" | tr -d '\n\r')
    local CLASSPATH="$dir/target/classes;$BACKEND_DIR/common/target/classes;$CP"

    echo "[*] Starting $name on port $port..."
    "$JAVA_HOME/bin/java" -cp "$CLASSPATH" "$main" --server.port="$port" > "$dir/server.log" 2>&1 &
    echo "  PID: $! - log: $dir/server.log"
    sleep 2
}

if [ "$SERVICE" = "all" ]; then
    echo "[2/2] Starting all 6 microservices..."
    for svc in "${!SERVICES[@]}"; do
        start_service "$svc"
    done
    echo ""
    echo "[*] All services started!"
    for svc in "${!SERVICES[@]}"; do
        printf "    %-18s http://localhost:%d\n" "$svc" "${SERVICES[$svc]}"
    done
elif [ -n "${SERVICES[$SERVICE]}" ]; then
    echo "[2/2] Starting $SERVICE..."
    start_service "$SERVICE"
else
    echo "[!] Unknown service: $SERVICE"
    echo "    Available: all, ${!SERVICES[@]}"
fi
