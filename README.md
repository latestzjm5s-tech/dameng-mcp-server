# Dameng CLI

达梦数据库命令行查询工具。纯 Java，单文件 3.5MB，输出 JSON。

也提供 GraalVM Native Image 构建的原生二进制，无需安装 Java。

## 下载

从 [Releases](https://github.com/latestzjm5s-tech/dameng-mcp-server/releases) 下载对应平台的文件：

| 文件 | 平台 |
|------|------|
| `dameng-cli-linux-x64` | Linux x64 |
| `dameng-cli-macos-arm64` | macOS Apple Silicon |
| `dameng-cli-windows-x64.exe` | Windows x64 |

## 配置

在可执行文件同目录下创建 `dameng-cli.conf`：

```properties
url=jdbc:dm://192.168.1.100:5236/DAMENG
username=SYSDBA
password=你的密码
```

也可通过环境变量配置（优先级高于配置文件）：

| 环境变量 | 配置文件键 | 默认值 |
|----------|-----------|--------|
| `DB_URL` | `url` | `jdbc:dm://localhost:5236/DAMENG` |
| `DB_USERNAME` | `username` | `SYSDBA` |
| `DB_PASSWORD` | `password` | `SYSDBA` |

## 使用

```bash
# 查询（默认只允许 SELECT/WITH/SHOW/DESC/EXPLAIN）
./dameng-cli "SELECT * FROM my_table LIMIT 10"

# 指定 schema
./dameng-cli --schema MY_SCHEMA "SELECT * FROM my_table"

# 执行写操作，需加 --danger
./dameng-cli --danger "INSERT INTO my_table(name) VALUES('test')"
```

JAR 方式运行（需要 Java 8+）：

```bash
java -jar dameng-cli.jar "SELECT 1"
```

## 输出格式

所有输出为 JSON，写入 stdout。成功 exit code = 0，失败 exit code = 1。

```json
{
  "sql": "SELECT 1 AS num",
  "resultCount": 1,
  "results": [{
    "index": 0,
    "type": "resultSet",
    "rowCount": 1,
    "rows": [{"num": 1}]
  }]
}
```

错误时：

```json
{
  "sql": "...",
  "error": "错误信息"
}
```

## 从源码构建

```bash
# 构建 JAR（需要 Java 8+ 和 Maven）
mvn clean package

# 产物
target/dameng-cli-1.0.0.jar
```

GitHub Actions 会在打 tag 时自动构建三平台 native image 并发布 Release。

## 许可证

MIT License
