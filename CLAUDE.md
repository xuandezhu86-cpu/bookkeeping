# 悠然记账 — Product Document

> 跨平台个人记账桌面应用（Windows / macOS）

---

## 一、产品概述

**悠然记账** 是一款面向个人的跨平台记账桌面应用，帮助用户轻松记录每一笔花销，养成理财好习惯。

**核心价值：**
- 简单快捷的记录体验
- 清晰的二级分类管理
- 直观的消费数据分析

---

## 二、最终技术栈

### 前端技术栈

| 技术 | 用途 |
|------|------|
| **Vue 3** (Composition API) | 前端框架 |
| **TypeScript** | 类型安全 |
| **Vite** | 构建工具 |
| **Electron** | 跨平台桌面框架 |
| **Element Plus** | UI 组件库 |
| **Pinia** | 状态管理 |
| **Vue Router 4.x** | 前端路由 |
| **ECharts** | 数据可视化 |
| **Axios** | HTTP 请求 |
| **npm** | 包管理器（注意：不要用 pnpm，有 EPERM 冲突） |

### 后端技术栈

| 技术 | 用途 |
|------|------|
| **Spring Boot 3.4.5** | 后端框架 |
| **JDK 17** (LTS) | Java 运行环境 |
| **MyBatis-Plus 3.5.9** | ORM 持久层 |
| **MySQL 8.x** | 关系型数据库 |
| **Druid 1.2.24** | 数据库连接池 |
| **Spring Security** | 用户认证与授权 |
| **JWT (jjwt 0.12.6)** | 无状态 Token 认证 |
| **Swagger Annotations** | API 注解（仅 @Tag, @Operation, @Schema） |
| **Maven** | 项目构建 |
| **Lombok** | 简化 Java 代码 |

### 架构说明

微服务架构：Electron 桌面客户端 → API 网关 (port 8080) → 6 个独立微服务 → MySQL 数据库

| 微服务 | 端口 | 职责 |
|--------|------|------|
| Gateway (网关) | 8080 | 请求路由、CORS |
| Auth | 8081 | 注册、登录、JWT 签发 |
| Category | 8082 | 分类管理（预设+自定义） |
| Expense | 8083 | 消费记录 CRUD |
| Budget | 8084 | 预算设置 |
| Statistics | 8085 | 数据统计（只读分析） |

---

## 三、分类设计

### 一级大类 → 二级小类

| 一级大类 | 二级小类 |
|---------|---------|
| 🍜 餐饮饮食 | 早餐、午餐、晚餐、外卖、水果、零食、饮品 |
| 🚗 交通出行 | 公交地铁、打车、加油、停车、维修保养、过路费 |
| 🏠 居住物业 | 房租、水电费、物业费、暖气费、网费、维修 |
| 🛍️ 购物消费 | 服装鞋包、日用品、数码产品、美妆护肤、家居用品 |
| 🎮 娱乐休闲 | 电影演出、游戏充值、运动健身、旅游出行、KTV |
| 🏥 医疗健康 | 门诊挂号、药品、体检、牙科、住院 |
| 📚 教育学习 | 书籍、课程培训、文具、考试报名 |
| 🤝 人情社交 | 聚餐请客、礼物、红包、捐款、随礼 |
| 📱 通讯网络 | 话费、流量、宽带 |
| 📦 其他 | 其他支出 |

---

## 四、功能模块

### 4.1 用户模块
- 注册、登录、退出
- JWT Token 认证（无状态，localStorage 存储）
- 个人基本信息管理

### 4.2 记账模块（核心）
- 新增消费记录：金额、二级分类、日期、备注
- 编辑/删除消费记录
- 消费记录列表（分页、按日期排序）
- 按分类/日期筛选

### 4.3 分类管理模块
- 预设一级/二级分类（系统初始化数据）
- 用户可自定义分类（增删改）
- 分类树形展示

### 4.4 数据报表模块
- 月度支出趋势折线图
- 分类支出占比饼图
- 年度支出汇总
- 支出排行榜（按分类汇总）

### 4.5 设置模块
- 主题切换（亮色/暗色模式）
- 数据导出
- 预算设置

---

## 五、数据库设计

### 5.1 用户表 `sys_user`

```sql
CREATE TABLE sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50) NOT NULL UNIQUE,
  password    VARCHAR(255) NOT NULL,
  nickname    VARCHAR(50),
  email       VARCHAR(100),
  avatar      VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 5.2 分类表 `category`

```sql
CREATE TABLE category (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(50) NOT NULL,
  icon        VARCHAR(50),
  parent_id   BIGINT DEFAULT NULL COMMENT 'NULL=一级分类, 非NULL=二级分类',
  sort_order  INT DEFAULT 0 COMMENT '排序号',
  user_id     BIGINT DEFAULT NULL COMMENT 'NULL=系统预设, 非NULL=用户自定义',
  is_deleted  TINYINT(1) DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (parent_id) REFERENCES category(id),
  FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

### 5.3 消费记录表 `expense_record`

```sql
CREATE TABLE expense_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  category_id   BIGINT NOT NULL,
  amount        DECIMAL(10, 2) NOT NULL COMMENT '人民币金额',
  record_date   DATE NOT NULL COMMENT '消费日期',
  note          VARCHAR(255) COMMENT '备注',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (category_id) REFERENCES category(id)
);
```

### 5.4 预算表 `budget`

```sql
CREATE TABLE budget (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  category_id BIGINT COMMENT 'NULL=总预算',
  month       VARCHAR(7) NOT NULL COMMENT '如: 2026-07',
  amount      DECIMAL(10, 2) NOT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (category_id) REFERENCES category(id)
);
```

### 5.5 初始化数据

参考 `init.sql`，包含 10 个一级分类、47 个二级分类的预设数据。

---

## 六、项目目录结构

```
youran-bookkeeping/
├── frontend/                       # Vue 3 + Electron
│   ├── electron/                   # Electron 主进程
│   │   └── main.ts
│   ├── src/
│   │   ├── api/                    # Axios API 封装
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── layouts/                # 布局组件
│   │   ├── router/                 # Vue Router
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── views/                  # 页面
│   │   │   ├── auth/               # 登录/注册
│   │   │   ├── dashboard/          # 首页概览
│   │   │   ├── records/            # 消费记录
│   │   │   ├── categories/         # 分类管理
│   │   │   ├── reports/            # 数据报表
│   │   │   └── settings/           # 设置
│   │   ├── types/                  # TypeScript 类型定义
│   │   ├── utils/                  # 工具函数
│   │   ├── App.vue
│   │   └── main.ts
│   ├── index.html
│   ├── vite.config.ts
│   ├── electron-builder.json
│   ├── tsconfig.json
│   └── package.json
│
├── backend/                        # Spring Boot 3 — 微服务多模块项目
│   ├── pom.xml                     # Maven 父工程 (packaging=pom)
│   ├── common/                     # 公共库 (Result, JwtUtil, etc.)
│   ├── gateway-service/            # API 网关 (port 8080)
│   ├── auth-service/               # 认证服务 (port 8081)
│   ├── category-service/           # 分类服务 (port 8082)
│   ├── expense-service/            # 消费记录 (port 8083)
│   ├── budget-service/             # 预算服务 (port 8084)
│   └── statistics-service/         # 统计服务 (port 8085)
│
├── start-backend.bat               # Windows 后端启动
├── start-backend.ps1               # PowerShell 后端启动（被 .bat 调用）
├── start-frontend.bat              # Windows 前端启动
├── start-backend.sh                # Linux/Mac 后端启动
├── start-frontend.sh               # Linux/Mac 前端启动
└── CLAUDE.md                       # 产品文档
```

---

## 七、开发规范

### Git 提交规范
- `feat:` — 新功能
- `fix:` — 修复 bug
- `docs:` — 文档修改
- `style:` — 代码格式（不影响功能）
- `refactor:` — 重构
- `perf:` — 性能优化
- `chore:` — 构建/工具链相关

### Git 管理规范

#### 仓库状态
- 仓库已初始化，首次提交：`c3bec2f feat: 初始化悠然记账项目`
- 仓库路径：项目根目录（本地 Git，无远程仓库）

#### .gitignore 策略
根目录 `.gitignore` 覆盖以下内容：

| 路径 | 说明 |
|------|------|
| `backend/**/target/` | Maven 编译输出（所有子模块） |
| `**/server.log` | 运行时日志 |
| `**/cp.txt` | Maven 自动生成的 classpath |
| `frontend/node_modules/` | npm 依赖 |
| `frontend/dist/` | 前端构建产物 |
| `.claude/` | Claude Code 会话历史 |
| `*.log` | 日志文件 |
| `.idea/`、`.vscode/` | IDE 配置 |

**不忽略的文件**：`frontend/package-lock.json`、`frontend/.gitignore`、`init.sql`

#### 分支策略
- `main` — 主分支，保持稳定可发布
- `dev` — 开发分支（按需创建），功能开发在此进行

#### 推荐工作流
1. 从 `main` 创建 `dev` 分支：`git checkout -b dev`
2. 在 `dev` 上进行开发，小步提交、频繁提交
3. 开发完成后合并回 `main`：`git checkout main && git merge dev`
4. 打标签发布：`git tag v1.0.0`

#### 注意事项
- **不要提交构建产物**：提交前运行 `git status` 确认没有 target/、dist/、node_modules/ 等目录
- **不要混用包管理器**：前端统一使用 npm（不要用 pnpm），`package-lock.json` 需提交到仓库
- **提交信息中文/英文均可**：建议保持统一风格

### 命名规范
- **前端**：camelCase（变量/函数），PascalCase（组件），kebab-case（文件）
- **后端**：camelCase（Java），小写+连字符（配置文件）
- **数据库**：snake_case（表名/字段名）

### API 规范
- RESTful 风格
- 统一响应格式：`{ code: 200, message: "success", data: {} }`
- 分页请求：`{ page, size }` → `{ records, total, pages }`

---

## 八、启动与构建

### 环境要求

| 工具 | 版本 | 位置 |
|------|------|------|
| JDK | 17 (LTS) | `c:\Users\33907\.jdks\ms-17.0.18` |
| Maven | 与 IntelliJ 2025.1 捆绑 | `C:\Program Files\JetBrains\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3` |
| Node.js | v24.11.1 | PATH |
| npm | 与 Node.js 捆绑 | PATH |
| MySQL | 8.x | localhost:3306，用户 root，密码 root |

### Windows 启动（推荐）

```bash
# 1. 启动全部后端微服务（双击 start-backend.bat 或命令行执行）
start-backend.bat all

# 2. 启动单个服务（如 auth-service）
start-backend.bat auth-service

# 3. 手动分步启动
cd backend
mvn compile -q -o                                         # 编译全部模块
start-backend.ps1 -Service all                             # 启动全部服务

# 4. 启动前端（双击 start-frontend.bat 或命令行执行）
cd frontend
npm run dev
```

### 启动说明

- **start-backend.bat**：设置 JDK 17 和 Maven 路径，编译全部模块，然后调用 `start-backend.ps1` 启动指定的微服务。支持参数 `all`（默认，启动6个服务）或单个服务名。
- **start-backend.ps1**：读取各服务的 `cp.txt` classpath，自动清理旧进程，启动 Java 服务。支持 `-Service` 参数。
- **start-frontend.bat**：检测 `node_modules`，用 `npm run dev` 启动 Vite 开发服务器。
- 注意：**不要用 pnpm**，之前安装 Electron 时混用了 npm 和 pnpm，pnpm 的 EPERM 错误无法解决。
- 注意：编译时需要先手动安装 common JAR 到本地 Maven 仓库（一次性的）：
  ```bash
  cd backend/common
  jar cf target/common-1.0.0.jar -C target/classes .
  mkdir -p ~/.m2/repository/com/youran/common/1.0.0/
  cp target/common-1.0.0.jar ~/.m2/repository/com/youran/common/1.0.0/
  ```

### 端口映射

| 服务 | 端口 | 模块名 |
|------|------|--------|
| Gateway API 网关 | 8080 | gateway-service |
| Auth 认证服务 | 8081 | auth-service |
| Category 分类服务 | 8082 | category-service |
| Expense 消费记录 | 8083 | expense-service |
| Budget 预算服务 | 8084 | budget-service |
| Statistics 统计服务 | 8085 | statistics-service |
| Vite 前端开发 | 5173 | - |
| MySQL | 3306 | - |

### 构建打包

```bash
cd frontend
npm run build:win    # Windows 打包
npm run build:mac    # macOS 打包
```

---

## 九、API 说明

所有 API 统一通过网关访问：`http://localhost:8080/api`。网关根据路径前缀将请求路由到对应微服务。

| 路径前缀 | 目标服务 | 端口 |
|---------|---------|------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/categories/**` | category-service | 8082 |
| `/api/records/**` | expense-service | 8083 |
| `/api/budgets/**` | budget-service | 8084 |
| `/api/statistics/**` | statistics-service | 8085 |

各服务也可通过独立端口直接访问（用于开发和调试）。

### 无需认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 注册 `{username, password, nickname}` |
| POST | `/auth/login` | 登录 `{username, password}` |

### 需要 JWT Token（请求头 `Authorization: Bearer <token>`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/categories/tree` | 分类树（含预设 + 用户自定义） |
| GET | `/categories/parent` | 一级分类列表 |
| GET | `/categories/children/{parentId}` | 二级分类列表 |
| POST | `/categories` | 新增分类 |
| PUT | `/categories/{id}` | 修改分类 |
| DELETE | `/categories/{id}` | 删除分类 |
| GET | `/records/page?page=1&size=10` | 分页查询消费记录 |
| POST | `/records` | 新增消费记录 |
| PUT | `/records/{id}` | 修改消费记录 |
| DELETE | `/records/{id}` | 删除消费记录 |
| GET | `/statistics/monthly?year=2026&month=7` | 月度统计 |
| GET | `/statistics/category?year=2026&month=7` | 分类占比 |
| GET | `/statistics/trend?year=2026` | 年度趋势 |
| GET | `/statistics/ranking?year=2026&month=7` | 支出排行榜 |
| GET | `/budgets` | 预算列表 |
| POST | `/budgets` | 新增/修改预算 |
| DELETE | `/budgets/{id}` | 删除预算 |

---

## 十、已知问题和注意事项

### 1. Knife4j / SpringDoc 已移除
SpringDoc 2.6.0 调用了 `ControllerAdviceBean(Object)` 构造方法，该方法在 Spring Framework 6.2（Spring Boot 3.4.5 捆绑）中已被删除。SpringDoc 3.0.0 需要 Spring Boot 4.x 和 Jackson 3.x，不兼容。Maven Central 不可达，无法下载兼容版本。

**解决方案**：从 pom.xml 移除了 Knife4j 和 SpringDoc，仅保留 Swagger 注解 (`swagger-annotations-jakarta`) 用于代码标注。无交互式 API 文档，直接用 curl 或前端测试。

### 2. Maven Central 不可达
此环境无法连接 Maven Central，构建必须使用离线模式：
```bash
mvn compile -q -o
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q -o
```

### 3. MyBatis-Plus 3.5.9 兼容性
- `PaginationInnerInterceptor` 已移除 → 手动分页（`COUNT + LIMIT/OFFSET`）
- `LambdaQueryWrapper.<T>query()` 已移除 → 改用 `Wrappers.<T>lambdaQuery()`
- `MetaObjectHandler` 移动到 `com.baomidou.mybatisplus.core.handlers.MetaObjectHandler`

### 4. 包管理器冲突
Electron 和 electron-builder 最初用 `npm` 安装，导致 pnpm 的 `.pnpm` store 无法正确建立符号链接。**永远使用 npm**，不要混用包管理器。

### 5. Windows 控制台 Quick Edit 模式
双击 `start-backend.bat` 后，**不要点击 CMD 窗口**。Windows 控制台的"快速编辑模式"会暂停前台进程（Java）。如果误点击导致后端无响应，按 Enter 键恢复。

### 6. 微服务架构注意事项
- **common 模块需手动安装**：由于 Maven Central 不可达，`mvn install` 无法运行。common 模块的 JAR 需手动安装到本地仓库（见启动说明）。
- **启动顺序无依赖**：各服务间无 HTTP 调用，可任意顺序启动。
- **共享数据库**：6 个服务共享同一个 MySQL 数据库 `youran_bookkeeping`，每个服务只操作自己的表。
- **JWT 密钥一致性**：所有服务必须配置相同的 `jwt.secret`（已在 application.yml 中统一）。
- **统计服务只读**：statistics-service 从 expense_record 和 category 表读取数据，不执行写操作。
- **调试方式**：可单独启动单个服务验证，如 `start-backend.bat auth-service`。

---

## 十一、验证方式

### 后端验证
```bash
# 1. 编译
cd backend && mvn compile -q -o

# 2. 启动所有服务
cd .. && ./start-backend.ps1 -Service all

# 3. 测试认证端点（通过网关）
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'

# 4. 测试单个服务直接访问
curl http://localhost:8082/api/categories/tree -H "Authorization: Bearer <token>"
```

### 其他验证
1. **前端验证**：`npm run dev` 启动 Vite 开发服务器，浏览器查看页面
2. **Electron 验证**：`npm run electron:dev` 启动桌面窗口
3. **全链路验证**：前端记账 → 调后端 API（经网关路由）→ 读写 MySQL → 报表展示
4. **跨平台验证**：Windows 和 macOS 上分别打包测试
