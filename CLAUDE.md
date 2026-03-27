# Dameng CLI

达梦数据库命令行查询工具。纯 Java，无框架，3.5MB。给 Claude Code / Codex 搭配 skill 用。

## Build

```bash
mvn clean package
```

产物: `target/dameng-cli-1.0.0.jar` (3.5MB)

## Usage

```bash
java -jar dameng-cli.jar "SELECT * FROM SYSDBA.MY_TABLE"
java -jar dameng-cli.jar --schema MYSCHEMA "SELECT 1"
java -jar dameng-cli.jar --danger "DELETE FROM my_table WHERE id=1"
```

## Config

配置文件 `dameng-cli.conf`（放在 JAR/二进制同目录）：

```properties
url=jdbc:dm://localhost:5236/DAMENG
username=SYSDBA
password=SYSDBA
```

优先级：环境变量 > 配置文件 > 默认值

| 环境变量 | 配置文件键 | 默认值 |
|---|---|---|
| `DB_URL` | `url` | `jdbc:dm://localhost:5236/DAMENG` |
| `DB_USERNAME` | `username` | `SYSDBA` |
| `DB_PASSWORD` | `password` | `SYSDBA` |

## Release

打 tag 自动构建三平台 native image：

```bash
git tag v1.x.x && git push origin v1.x.x
```
