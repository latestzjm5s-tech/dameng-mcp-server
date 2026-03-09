# 达梦数据库 MCP Server

基于 Spring Boot 的 MCP (Model Context Protocol) 服务器，为 AI 客户端提供达梦数据库的查询和写入能力。

## 功能特性

- 通过 SSE (Server-Sent Events) 协议暴露 MCP 工具
- 支持执行任意 SQL 语句（查询、DML、DDL、存储过程调用等）
- SSE 心跳保活机制，防止长时间空闲连接断开
- **支持 ARM64 Mac (Apple Silicon) Docker 环境运行**

## MCP 工具

| 工具名 | 描述 |
|--------|------|
| `executeQuery` | 执行任意 SQL，返回结构化结果（结果集/影响行数） |
| `executeMutation` | 与 `executeQuery` 等价，保留用于兼容旧客户端 |

## 环境要求

- Java 17+
- 达梦数据库

## 快速开始

### 编译

> **注意**：需要 Java 17+ 环境，请确保 `JAVA_HOME` 指向正确的 JDK 版本。

```bash
# 检查 Java 版本
java -version

# 编译项目
./mvnw clean package -DskipTests
```

### 运行

**Linux / macOS：**

```bash
# 使用环境变量配置数据库连接
export DB_URL=jdbc:dm://localhost:5236/DAMENG
export DB_USERNAME=SYSDBA
export DB_PASSWORD=yourpassword

./mvnw spring-boot:run
```

**Windows (CMD)：**

```cmd
set DB_URL=jdbc:dm://localhost:5236/DAMENG
set DB_USERNAME=SYSDBA
set DB_PASSWORD=yourpassword

mvnw.cmd spring-boot:run
```

**Windows (PowerShell)：**

```powershell
$env:DB_URL="jdbc:dm://localhost:5236/DAMENG"
$env:DB_USERNAME="SYSDBA"
$env:DB_PASSWORD="yourpassword"

.\mvnw.cmd spring-boot:run
```

### Docker 部署

> **亮点**：基于 `amazoncorretto:17-alpine` 镜像，原生支持 ARM64 架构，可在 Apple Silicon Mac (M1/M2/M3/M4) 上流畅运行。

```bash
# 构建镜像
docker build -t dameng-mcp-server .

# 运行容器
docker run -d -p 8080:8080 \
  -e DB_URL=jdbc:dm://host:5236/DAMENG \
  -e DB_USERNAME=SYSDBA \
  -e DB_PASSWORD=yourpassword \
  --name dameng-mcp \
  dameng-mcp-server
```

### Claude Code 配置

服务启动后，在 Claude Code 中添加 MCP 服务：

```bash
claude mcp add --transport sse dameng-db http://localhost:8080/sse
```

添加成功后，Claude Code 即可使用达梦数据库查询工具。

## 配置说明

### 数据库连接

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DB_URL` | `jdbc:dm://localhost:5236/DAMENG` | JDBC 连接地址 |
| `DB_USERNAME` | `SYSDBA` | 数据库用户名 |
| `DB_PASSWORD` | `SYSDBA` | 数据库密码 |

### 连接策略

- 每次请求创建新连接（无连接池）
- 自动添加连接超时参数：`connectTimeout=5000&socketTimeout=10000`
- 查询超时：10 秒

### SSE 保活机制

- `keep-alive-interval`: 30 秒（MCP Server 定期发送心跳）
- `tomcat.keep-alive-timeout`: 5 分钟

## 安全机制

### SQL 执行工具

- 允许执行任意 SQL 语句
- 返回结构化执行结果，可能包含一个或多个结果集或更新计数
- 仅保留空 SQL 拦截，其他语句类型不做限制

## 项目结构

```
com.uniin.ioc.dameng/
├── DamengApplication.java          # 主入口
├── config/
│   └── DatabaseConfig.java         # DataSource 和 JdbcTemplate 配置
├── service/
│   ├── DamengQueryService.java     # 通用 SQL 执行
│   └── DamengMutationService.java  # 兼容旧工具名的 SQL 执行入口
├── mcp/
│   └── DamengMcpTools.java         # MCP 工具定义
├── validator/
│   └── SqlValidator.java           # SQL 基础校验（仅校验非空）
└── exception/
    ├── InvalidSqlException.java
    └── QueryExecutionException.java
```

## 许可证

MIT License
