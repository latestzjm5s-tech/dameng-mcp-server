# 达梦数据库 MCP Server

基于 Spring Boot 的 MCP (Model Context Protocol) 服务器，为 AI 客户端提供达梦数据库的**只读**查询能力。

## 功能特性

- 通过 SSE (Server-Sent Events) 协议暴露 MCP 工具
- 仅支持只读查询，确保数据安全
- 支持断线自动重连（适用于 VPN 等不稳定网络环境）
- 查询结果限制 1000 行，防止内存溢出

## MCP 工具

| 工具名 | 描述 |
|--------|------|
| `executeQuery` | 执行只读 SELECT 查询（最多返回 1000 行） |
| `listTables` | 列出指定 schema 下的所有表 |
| `describeTable` | 获取表结构信息 |
| `listSchemas` | 列出所有数据库 schema |

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

```bash
# 使用环境变量配置数据库连接
export DB_URL=jdbc:dm://localhost:5236/DAMENG
export DB_USERNAME=SYSDBA
export DB_PASSWORD=yourpassword

./mvnw spring-boot:run
```

### Docker 部署

```bash
# 构建镜像
docker build -t dameng-mcp-server .

# 运行容器
docker run -p 8080:8080 \
  -e DB_URL=jdbc:dm://host:5236/DAMENG \
  -e DB_USERNAME=SYSDBA \
  -e DB_PASSWORD=yourpassword \
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

### 连接池配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `maximum-pool-size` | 10 | 最大连接数 |
| `minimum-idle` | 2 | 最小空闲连接 |
| `connection-timeout` | 60秒 | 连接超时时间 |
| `keepalive-time` | 120秒 | 保活探测间隔 |

### 断线重连

当数据库连接断开时（如 VPN 断线），会自动重试：

- 重试次数：3 次
- 重试间隔：2秒 → 4秒 → 8秒（指数退避）

## 安全机制

- 仅允许 SELECT 查询
- 禁止 INSERT/UPDATE/DELETE/DROP 操作
- 禁止 SQL 注释
- 禁止存储过程调用
- 查询结果限制 1000 行

## 项目结构

```
com.uniin.ioc.dameng/
├── DamengApplication.java          # 主入口
├── config/
│   └── DatabaseConfig.java         # JdbcTemplate 配置
├── service/
│   ├── DamengQueryService.java     # SQL 查询执行
│   └── DamengSchemaService.java    # Schema 操作
├── mcp/
│   └── DamengMcpTools.java         # MCP 工具定义
├── validator/
│   └── SqlValidator.java           # 只读 SQL 校验
└── exception/
    ├── InvalidSqlException.java
    └── QueryExecutionException.java
```

## 许可证

MIT License