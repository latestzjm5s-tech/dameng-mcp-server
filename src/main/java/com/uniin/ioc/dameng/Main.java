package com.uniin.ioc.dameng;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Main {

    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        if (args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])) {
            printUsage();
            System.exit(0);
        }

        String schema = null;
        boolean danger = false;
        int argIdx = 0;

        while (argIdx < args.length) {
            if ("--schema".equals(args[argIdx]) && argIdx + 1 < args.length) {
                schema = args[++argIdx];
                argIdx++;
            } else if ("--danger".equals(args[argIdx])) {
                danger = true;
                argIdx++;
            } else {
                break;
            }
        }

        String sql = join(args, argIdx);
        if (sql.trim().isEmpty()) {
            printUsage();
            System.exit(0);
        }

        if (!danger && !isSelect(sql)) {
            System.out.println(errorJson(sql, new Exception("Only SELECT is allowed. Use --danger to execute non-SELECT statements.")));
            System.exit(1);
        }

        Properties conf = loadConf();
        String url = resolve("DB_URL", "url", conf, "jdbc:dm://localhost:5236/DAMENG");
        String user = resolve("DB_USERNAME", "username", conf, "SYSDBA");
        String pass = resolve("DB_PASSWORD", "password", conf, "SYSDBA");

        if (!url.contains("socketTimeout")) {
            url += (url.contains("?") ? "&" : "?") + "socketTimeout=10000&connectTimeout=5000";
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Map<String, Object> result = execute(conn, sql, schema);
            System.out.println(JSON.writeValueAsString(result));
        } catch (SQLException e) {
            System.out.println(errorJson(sql, e));
            System.exit(1);
        } catch (Exception e) {
            System.out.println(errorJson(sql, e));
            System.exit(1);
        }
    }

    private static Map<String, Object> execute(Connection conn, String sql, String schema) throws SQLException {
        if (schema != null && !schema.trim().isEmpty()) {
            try (Statement s = conn.createStatement()) {
                s.execute("SET SCHEMA " + schema.toUpperCase());
            }
        }

        try (Statement stmt = conn.createStatement()) {
            List<Map<String, Object>> results = new ArrayList<>();
            boolean hasResultSet = stmt.execute(sql);
            int idx = 0;

            while (true) {
                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        List<Map<String, Object>> rows = extractRows(rs);
                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("index", idx++);
                        r.put("type", "resultSet");
                        r.put("rowCount", rows.size());
                        r.put("rows", rows);
                        results.add(r);
                    }
                } else {
                    int count = stmt.getUpdateCount();
                    if (count == -1) break;
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("index", idx++);
                    r.put("type", "updateCount");
                    r.put("affectedRows", count);
                    results.add(r);
                }
                hasResultSet = stmt.getMoreResults();
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sql", sql);
            if (schema != null && !schema.trim().isEmpty()) {
                response.put("schema", schema.toUpperCase());
            }
            response.put("resultCount", results.size());
            response.put("results", results);
            return response;
        }
    }

    private static List<Map<String, Object>> extractRows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    private static String errorJson(String sql, Exception e) {
        try {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("sql", sql);
            err.put("error", e.getMessage());
            return JSON.writeValueAsString(err);
        } catch (Exception ignored) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private static boolean isSelect(String sql) {
        String trimmed = sql.trim().toUpperCase();
        return trimmed.startsWith("SELECT") || trimmed.startsWith("WITH") || trimmed.startsWith("SHOW") || trimmed.startsWith("DESC") || trimmed.startsWith("EXPLAIN");
    }

    /**
     * Load dameng-cli.conf from the same directory as the JAR file.
     */
    private static Properties loadConf() {
        Properties props = new Properties();
        try {
            String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarDir = new File(jarPath).getParentFile();
            File confFile = new File(jarDir, "dameng-cli.conf");
            if (confFile.exists()) {
                FileInputStream fis = new FileInputStream(confFile);
                try {
                    props.load(fis);
                } finally {
                    fis.close();
                }
            }
        } catch (Exception ignored) {
        }
        return props;
    }

    /**
     * Resolve config value: env var > conf file > default.
     */
    private static String resolve(String envKey, String confKey, Properties conf, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) return envVal;
        String confVal = conf.getProperty(confKey);
        if (confVal != null && !confVal.trim().isEmpty()) return confVal;
        return defaultValue;
    }

    private static String env(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    private static String join(String[] args, int from) {
        StringJoiner sj = new StringJoiner(" ");
        for (int i = from; i < args.length; i++) sj.add(args[i]);
        return sj.toString();
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar dameng-cli.jar [--schema SCHEMA] [--danger] SQL");
        System.err.println();
        System.err.println("Options:");
        System.err.println("  --schema SCHEMA  Set schema before executing SQL");
        System.err.println("  --danger         Allow non-SELECT statements (INSERT/UPDATE/DELETE/DDL)");
        System.err.println();
        System.err.println("Config: place dameng-cli.conf next to the JAR file:");
        System.err.println("  url=jdbc:dm://localhost:5236/DAMENG");
        System.err.println("  username=SYSDBA");
        System.err.println("  password=SYSDBA");
        System.err.println();
        System.err.println("Env vars (override conf): DB_URL, DB_USERNAME, DB_PASSWORD");
    }
}
