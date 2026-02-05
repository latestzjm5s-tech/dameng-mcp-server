package com.uniin.ioc.dameng.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 使用 DriverManagerDataSource 替代连接池
     * 每次请求都创建新连接，避免连接失效问题
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        // 添加连接超时参数到 URL
        String urlWithTimeout = url;
        if (!url.contains("socketTimeout")) {
            urlWithTimeout = url + (url.contains("?") ? "&" : "?")
                + "socketTimeout=10000&connectTimeout=5000";
        }
        dataSource.setUrl(urlWithTimeout);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(10); // 10 seconds timeout
        return template;
    }
}
